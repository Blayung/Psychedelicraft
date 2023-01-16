/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.legacy.program.ShaderSimpleEffects;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperSimpleEffects extends ShaderWrapper<ShaderSimpleEffects> {
    public WrapperSimpleEffects(String utils) {
        super(new ShaderSimpleEffects(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderSimpleEffects.frag"), utils);
    }

    @Override
    public void setShaderValues(float tickDelta, int ticks, @Nullable Framebuffer buffer) {
        DrugProperties drugProperties = DrugProperties.of(MinecraftClient.getInstance().player);

        if (drugProperties != null) {
            shaderInstance.quickColorRotation = drugProperties.getHallucinations().getQuickColorRotation(tickDelta);
            shaderInstance.slowColorRotation = drugProperties.getHallucinations().getSlowColorRotation(tickDelta);
            shaderInstance.desaturation = drugProperties.getHallucinations().getDesaturation(tickDelta);
            shaderInstance.colorIntensification = drugProperties.getHallucinations().getColorIntensification(tickDelta);
        } else {
            shaderInstance.slowColorRotation = 0;
            shaderInstance.quickColorRotation = 0;
            shaderInstance.desaturation = 0;
            shaderInstance.colorIntensification = 0;
        }
    }
}