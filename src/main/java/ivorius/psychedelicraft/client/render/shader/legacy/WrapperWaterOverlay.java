/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.shader.legacy.program.ShaderDistortionMap;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperWaterOverlay extends ShaderWrapper<ShaderDistortionMap> {
    public Identifier waterDropletsDistortionTexture = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "waterDistortion.png");

    public WrapperWaterOverlay(String utils) {
        super(new ShaderDistortionMap(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderDistortionMap.frag"), utils);
    }

    @Override
    public void setShaderValues(float tickDelta, int ticks, @Nullable Framebuffer buffer) {
        DrugProperties drugProperties = DrugProperties.of(MinecraftClient.getInstance().player);

        if (drugProperties != null && PsychedelicraftClient.getConfig().visual.waterOverlayEnabled) {
            float waterScreenDistortion = DrugRenderer.INSTANCE.getEnvironmentalEffects().getWaterScreenDistortion();
            shaderInstance.strength = waterScreenDistortion * 0.2F;
            shaderInstance.alpha = waterScreenDistortion;
            shaderInstance.noiseTextureIndex0 = GLStateProxy.getTextureId(waterDropletsDistortionTexture);
            shaderInstance.noiseTextureIndex1 = GLStateProxy.getTextureId(waterDropletsDistortionTexture);
            shaderInstance.texTranslation0 = new float[]{0, ticks * 0.005F};
            shaderInstance.texTranslation1 = new float[]{0.5F, ticks * 0.007F};
        } else {
            shaderInstance.strength = 0;
        }
    }
}