package ivorius.psychedelicraft.recipe;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;

public record FluidModifyingResult(Map<String, Modification> attributes, ItemStack result) {
    public static final Codec<FluidModifyingResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Modification.CODEC).optionalFieldOf("attributes", Map.of()).forGetter(FluidModifyingResult::attributes),
            ItemStack.VALIDATED_CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(FluidModifyingResult::result)
        ).apply(instance, FluidModifyingResult::new));
    public static final PacketCodec<RegistryByteBuf, FluidModifyingResult> PACKET_CODEC = PacketCodec.ofStatic(
            (a, b) -> b.write(a),
            buffer -> new FluidModifyingResult(buffer.readMap(PacketByteBuf::readString, FluidModifyingResult.Modification::new), ItemStack.PACKET_CODEC.decode(buffer))
    );

    public ItemStack applyTo(ItemStack input) {
        ItemStack stack = result.isEmpty() ? input.copyWithCount(
                result.getItem() == Items.AIR ? 1 : result.getCount()
            ) : result.copy();
        ItemFluids fluids = ItemFluids.of(input);
        Map<String, Integer> attributes = new HashMap<>(fluids.attributes());
        this.attributes.forEach((key, modder) -> {
            attributes.put(key, modder.applyAsInt(attributes.getOrDefault(key, 0).intValue()));
        });
        return ItemFluids.set(stack, ItemFluids.create(fluids.fluid(), fluids.amount(), attributes));
    }

    @Deprecated
    private void write(RegistryByteBuf buffer) {
        buffer.writeMap(attributes, PacketByteBuf::writeString, (b, c) -> c.write(b));
        ItemStack.PACKET_CODEC.encode(buffer, result);
    }

    interface Op {
        int apply(int a, int b);
    }

    public enum Ops implements Op, StringIdentifiable {
        SET((a, b) -> b),
        ADD((a, b) -> a + b),
        SUBTRACT((a, b) -> a - b),
        MULTIPLY((a, b) -> a * b),
        DIVIDE((a, b) -> a / b);
        private static final Codec<Ops> CODEC = StringIdentifiable.createCodec(Ops::values);

        private final String name;
        private final Op operation;

        Ops(Op operation) {
            this.name = name().toLowerCase(Locale.ROOT);
            this.operation = operation;
        }

        @Override
        public int apply(int a, int b) {
            return operation.apply(a, b);
        }

        @Override
        public String asString() {
            return name;
        }
    }

    public record Modification(int value, Ops type) implements Int2IntFunction {
        private static final Codec<Modification> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("value").forGetter(Modification::value),
                Ops.CODEC.optionalFieldOf("type", Ops.ADD).forGetter(Modification::type)
        ).apply(instance, Modification::new));

        Modification(PacketByteBuf buffer) {
            this(buffer.readVarInt(), buffer.readEnumConstant(Ops.class));
        }

        @Override
        public int get(int v) {
            return type.operation.apply(v, value);
        }

        public void write(PacketByteBuf buffer) {
            buffer.writeVarInt(value);
            buffer.writeEnumConstant(type);
        }
    }
}
