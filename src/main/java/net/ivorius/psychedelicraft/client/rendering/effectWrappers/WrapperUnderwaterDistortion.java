/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package net.ivorius.psychedelicraft.client.rendering.effectWrappers;

import net.ivorius.psychedelicraft.Psychedelicraft;
import net.ivorius.psychedelicraft.client.rendering.shaders.DrugShaderHelper;
import net.ivorius.psychedelicraft.client.rendering.shaders.ShaderHeatDistortions;
import net.ivorius.psychedelicraft.entities.DrugHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperUnderwaterDistortion extends ShaderWrapper<ShaderHeatDistortions>
{
    public ResourceLocation heatDistortionNoiseTexture;

    public WrapperUnderwaterDistortion(String utils)
    {
        super(new ShaderHeatDistortions(Psychedelicraft.logger), getRL("shaderBasic.vert"), getRL("shaderHeatDistortion.frag"), utils);

        heatDistortionNoiseTexture = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "heatDistortionNoise.png");
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks)
    {
        DrugHelper drugHelper = DrugHelper.getDrugHelper(Minecraft.getMinecraft().renderViewEntity);

        if (drugHelper != null)
        {
            float waterDistortion = DrugShaderHelper.doWaterDistortion ? drugHelper.drugRenderer.getCurrentWaterDistortion() : 0.0f;

            shaderInstance.depthTextureIndex = DrugShaderHelper.depthBuffer.getDepthTextureIndex();
            shaderInstance.noiseTextureIndex = DrugShaderHelper.getTextureIndex(heatDistortionNoiseTexture);

            shaderInstance.strength = waterDistortion;
            shaderInstance.wobbleSpeed = 0.03f;
        }
        else
        {
            shaderInstance.strength = 0.0f;
        }
    }

    @Override
    public void update()
    {

    }

    @Override
    public boolean wantsDepthBuffer()
    {
        return true;
    }
}