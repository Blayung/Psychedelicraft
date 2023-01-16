/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * @author Sollace
 * @since 15 Jan 2023
 */
public class EnvironmentalScreenEffect implements ScreenEffect {
    private static final Identifier HURT_OVERLAY = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "hurt_overlay.png");

    private float experiencedHealth = 5F;

    private int timeScreenWet;
    private boolean wasInWater;
    private boolean wasInRain;

    private float currentHeat;

    public float getHeatDistortion() {
        return wasInWater ? 0 : MathHelper.clamp(((currentHeat - 1) * 0.0015f), 0, 0.01F);
    }

    public float getWaterDistortion() {
        return wasInWater ? 0.025F : 0;
    }

    public float getWaterScreenDistortion() {
        return timeScreenWet > 0 && !wasInWater ? Math.min(1, timeScreenWet / 80F) : 0;
    }

    @Override
    public void update(float tickDelta) {

        PlayerEntity entity = MinecraftClient.getInstance().player;

        if (PsychedelicraftClient.getConfig().visual.hurtOverlayEnabled) {
            experiencedHealth = MathUtils.nearValue(experiencedHealth, entity.getHealth(), 0.01f, 0.01f);
        }

        wasInWater = entity.world.getFluidState(new BlockPos(entity.getEyePos())).isIn(FluidTags.WATER);
        // TODO: (Sollace) The year is 2023. Can the client handle rain? I think it can now
        //wasInRain = player.worldObj.getRainStrength(1.0f) > 0.0f && player.worldObj.getPrecipitationHeight(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY)) <= player.posY; //Client can't handle rain

        if (PsychedelicraftClient.getConfig().visual.waterOverlayEnabled) {
            timeScreenWet--;

            if (wasInWater) {
                timeScreenWet += 20;
            }
            if (wasInRain) {
                timeScreenWet += 4;
            }

            timeScreenWet = MathHelper.clamp(timeScreenWet, 0, 100);
        }

        BlockPos pos = entity.getBlockPos();
        float newHeat = entity.world.getBiome(pos).value().getTemperature();

        this.currentHeat = MathUtils.nearValue(currentHeat, newHeat, 0.01f, 0.01f);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity entity = client.player;

        if (PsychedelicraftClient.getConfig().visual.hurtOverlayEnabled && entity.hurtTime > 0 || experiencedHealth < 5) {
            float p1 = (float) entity.hurtTime / (float) entity.maxHurtTime;
            float p2 = (5 - experiencedHealth) / 6F;

            float p = p1 > 0 ? p1 : p2 > 0 ? p2 : 0;
            DrugRenderer.drawOverlay(matrices, p, screenWidth, screenHeight, HURT_OVERLAY, 0, 0, 1, 1, (int) ((1 - p) * 40));
        }

        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    @Override
    public void close() {

    }

}