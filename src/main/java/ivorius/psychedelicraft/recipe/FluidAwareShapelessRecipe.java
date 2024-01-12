/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.*;

import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;

public class FluidAwareShapelessRecipe extends ShapelessRecipe {

    private final ItemStack output;
    private final DefaultedList<OptionalFluidIngredient> ingredients;
    private final List<OptionalFluidIngredient> fluidRestrictions;

    public FluidAwareShapelessRecipe(Identifier id, String group, CraftingRecipeCategory category, ItemStack output,
            DefaultedList<OptionalFluidIngredient> input) {
        super(id, group, category, output,
                // parent expects regular ingredients but we don't actually use them
                input.stream()
                .map(i -> i.receptical().orElse(Ingredient.EMPTY))
                .collect(Collectors.toCollection(DefaultedList::of))
        );
        this.output = output;
        this.ingredients = input;
        this.fluidRestrictions = ingredients.stream().filter(i -> i.fluid().filter(f -> f.level() > 0).isPresent()).toList();
    }

    public DefaultedList<OptionalFluidIngredient> getFluidAwareIngredients() {
        return ingredients;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.SHAPELESS_FLUID;
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        List<OptionalFluidIngredient> unmatchedInputs = new ArrayList<>(ingredients);
        return RecipeUtils.stacks(inventory)
                    .filter(stack -> unmatchedInputs.stream()
                        .filter(ingredient -> ingredient.test(stack))
                        .findFirst()
                        .map(unmatchedInputs::remove)
                        .orElse(false)).count() == ingredients.size() && unmatchedInputs.isEmpty();
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory inventory) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            Item item = stack.getItem();

            defaultedList.set(i, item.hasRecipeRemainder()
                ? new ItemStack(item.getRecipeRemainder())
                : fluidRestrictions.stream()
                        .filter(t -> t.test(stack))
                        .findFirst()
                        .flatMap(OptionalFluidIngredient::fluid)
                        .map(fluid -> MutableFluidContainer.of(stack).decrement(fluid.level()))
                        .map(MutableFluidContainer::asStack)
                        .orElse(ItemStack.EMPTY)
            );
        }
        return defaultedList;
    }

    static class Serializer implements RecipeSerializer<FluidAwareShapelessRecipe> {
        @SuppressWarnings("deprecation")
        @Override
        public FluidAwareShapelessRecipe read(Identifier id, JsonObject json) {
            return new FluidAwareShapelessRecipe(id,
                    JsonHelper.getString(json, "group", ""),
                    CraftingRecipeCategory.CODEC.byId(JsonHelper.getString(json, "category", null), CraftingRecipeCategory.MISC),
                    ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result")),
                    RecipeUtils.checkLength(OptionalFluidIngredient.fromJsonArray(JsonHelper.getArray(json, "ingredients")))
            );
        }

        @Override
        public FluidAwareShapelessRecipe read(Identifier id, PacketByteBuf buffer) {
            return new FluidAwareShapelessRecipe(id,
                    buffer.readString(),
                    buffer.readEnumConstant(CraftingRecipeCategory.class),
                    buffer.readItemStack(),
                    buffer.readCollection(DefaultedList::ofSize, OptionalFluidIngredient::new)
            );
        }

        @Override
        public void write(PacketByteBuf buffer, FluidAwareShapelessRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            buffer.writeItemStack(recipe.output);
            buffer.writeCollection(recipe.ingredients, (b, c) -> c.write(b));
        }
    }
}
