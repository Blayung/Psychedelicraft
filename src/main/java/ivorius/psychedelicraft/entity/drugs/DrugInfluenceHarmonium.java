/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drugs;

import ivorius.psychedelicraft.entity.drugs.effects.HarmoniumDrug;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 10.03.14.
 */
public class DrugInfluenceHarmonium extends DrugInfluence {
    public float[] color;

    public DrugInfluenceHarmonium(String drugName, int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluence, float[] color) {
        super(drugName, delay, influenceSpeed, influenceSpeedPlus, maxInfluence);
        this.color = color;
    }

    public DrugInfluenceHarmonium() {
        super();
        color = new float[3];
    }

    @Override
    public void addToDrug(DrugProperties drugProperties, double value) {
        super.addToDrug(drugProperties, value);

        Drug drug = drugProperties.getDrug(getDrugName());

        if (drug instanceof HarmoniumDrug) {
            HarmoniumDrug harmonium = (HarmoniumDrug) drug;

            double inf = value + (1.0f - value) * (1.0f - harmonium.getActiveValue());
            harmonium.currentColor[0] = (float) MathHelper.lerp(inf, harmonium.currentColor[0], color[0]);
            harmonium.currentColor[1] = (float) MathHelper.lerp(inf, harmonium.currentColor[1], color[1]);
            harmonium.currentColor[2] = (float) MathHelper.lerp(inf, harmonium.currentColor[2], color[2]);
        }
    }

    @Override
    public void fromNbt(NbtCompound compound) {
        super.fromNbt(compound);
        color[0] = compound.getFloat("color[0]");
        color[1] = compound.getFloat("color[1]");
        color[2] = compound.getFloat("color[2]");
    }

    @Override
    public void toNbt(NbtCompound compound) {
        super.toNbt(compound);
        compound.putFloat("color[0]", color[0]);
        compound.putFloat("color[1]", color[1]);
        compound.putFloat("color[2]", color[2]);
    }
}