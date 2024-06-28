package ivorius.psychedelicraft.fluid;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChemicalExtractFluid extends DrugFluid implements Processable {
    public static final Attribute<Integer> DISTILLATION = Attribute.ofInt("distillation", 0, 2);

    final Settings settings;
    private final DrugType<?> drug;

    private final DrugFluid purifiedForm;

    public ChemicalExtractFluid(Identifier id, Settings settings, DrugType<?> drug, DrugFluid purifiedForm) {
        super(id, settings.drinkable());
        this.settings = settings;
        this.drug = drug;
        this.purifiedForm = purifiedForm;
    }

    @Override
    protected void getDrugInfluencesPerLiter(ItemFluids stack, Consumer<DrugInfluence> consumer) {
        super.getDrugInfluencesPerLiter(stack, consumer);

        consumer.accept(new DrugInfluence(drug, DrugInfluence.DelayType.IMMEDIATE, 0.03, 0, Math.pow(96F, DISTILLATION.get(stack))));
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type) {
        int distillation = DISTILLATION.get(tank.getContents());
        if (type == ProcessType.REACT) {
            return Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoFlowerExtract().ticksPerDistillation() * (1 + distillation);
        }

        return UNCONVERTABLE;
    }

    @Override
    public void process(Resovoir tank, ProcessType type, ByProductConsumer output) {
        if (type == ProcessType.REACT) {
            if (DISTILLATION.get(tank.getContents()) < 2) {
                output.accept(DISTILLATION.cycle(tank.drain(2)));
            } else {
                tank.drain(2);
                output.accept(purifiedForm.getDefaultStack(1));
            }
        }
    }

    @Override
    public <T> Stream<T> getProcessStages(ProcessType type, ProcessStageConsumer<T> consumer) {
        if (type == ProcessType.REACT) {
            int distillRate = Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoFlowerExtract().ticksPerDistillation();
            return Stream.concat(DISTILLATION.steps().map(step -> {
                return consumer.accept(distillRate * (1 + step.getLeft()),
                        1,
                        stack -> DISTILLATION.set(stack, step.getLeft()),
                        stack -> DISTILLATION.set(stack, step.getRight())
                );
            }), Stream.of(consumer.accept(distillRate * 3, 1, from -> DISTILLATION.set(from, 3).ofAmount(2), to -> purifiedForm.getDefaultStack(1))));
        }

        return Stream.empty();
    }

    @Override
    public Text getName(ItemFluids stack) {
        int distillation = DISTILLATION.get(stack);
        return Text.translatable(getTranslationKey() + ".distilled." + distillation, distillation);
    }

    @Override
    public int getColor(ItemFluids stack) {
        return MathUtils.mixColors(
            super.getColor(stack),
            0xFFFFFFFF,
            DISTILLATION.get(stack) / 16F
        );
    }

    @Override
    public int getHash(ItemFluids stack) {
        return Objects.hash(this, DISTILLATION.get(stack));
    }
}
