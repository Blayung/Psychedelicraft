/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.fluid.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Created by Sollace on Jan 1 2023
 */
public class DrinkableItem extends Item implements FluidContainerItem {
    public static final int FLUID_PER_DRINKING = FluidHelper.MILLIBUCKETS_PER_LITER / 4;

    private final int capacity;

    private final int consumptionVolume;
    private final ConsumableFluid.ConsumptionType consumptionType;

    public DrinkableItem(Settings settings, int capacity, int consumptionVolume, ConsumableFluid.ConsumptionType consumptionType) {
        super(settings.maxCount(1));
        this.capacity = capacity;
        this.consumptionVolume = Math.min(capacity, consumptionVolume);
        this.consumptionType = consumptionType;
    }

    @Override
    public int getMaxCapacity() {
        return capacity;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return getFluid(stack).isEmpty() ? UseAction.NONE
                : consumptionType == ConsumableFluid.ConsumptionType.DRINK
                ? UseAction.DRINK
                : UseAction.BOW;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        ConsumableFluid.consume(stack, entity, consumptionVolume, !(entity instanceof PlayerEntity p && p.isCreative()), consumptionType);
        return stack;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (ConsumableFluid.canConsume(stack, player, consumptionVolume, consumptionType)) {
            player.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }
        return super.use(world, player, hand);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return DEFAULT_MAX_USE_TIME;
    }

    @Override
    public Text getName(ItemStack stack) {
        SimpleFluid fluid = getFluid(stack);

        if (!fluid.isEmpty()) {
            return Text.translatable(getTranslationKey() + ".filled", fluid.getName(stack));
        }

        return super.getName(stack);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return !getFluid(stack).isEmpty();
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return (int)(ITEM_BAR_STEPS * getFillPercentage(stack));
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return MathHelper.hsvToRgb(getFillPercentage(stack) / 3F, 1, 1);
    }
}