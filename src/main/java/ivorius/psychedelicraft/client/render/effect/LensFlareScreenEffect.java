/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;
import org.joml.*;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.PsycheMatrixHelper;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.lang.Math;

/**
 * Created by lukas on 26.02.14.
 * Updated by Sollace on 15 Jan 2023
 */
public class LensFlareScreenEffect implements ScreenEffect {
    private static final float SUN_RADIANS = 5F * MathHelper.RADIANS_PER_DEGREE;
    private static final float SUN_WIDTH = 20;

    private static final float SUN_DISTANCE = 120;

    private static final float[] FLARE_SIZES = {
            0.15f, 0.24f, 0.12f, 0.036f, 0.06f,
            0.048f, 0.006f, 0.012f, 0.5f, 0.09f,
            0.036f, 0.09f, 0.06f, 0.05f, 0.6f
    };
    private static final float[] FLARE_INFLUENCES = {
            -1.3f, -2.0f, 0.2f, 0.4f, 0.25f,
            -0.25f, -0.7f, -1.0f, 1.0f, 1.4f,
            -1.31f, -1.2f, -1.5f, -1.55f, -3.0f
    };
    private static final Identifier[] FLARES = IntStream.range(0, FLARE_SIZES.length)
            .mapToObj(i -> Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "flare" + i + ".png"))
            .toArray(Identifier[]::new);

    private static final Identifier BLINDNESS_OVERLAY = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "sun_blindness.png");

    private float actualSunAlpha = 0;

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Override
    public boolean shouldApply(float ticks) {
        return getIntensity() > 0;
    }

    @Override
    public void update(float tickDelta) {
        World world = client.world;
        Entity renderEntity = client.getCameraEntity();

        if (renderEntity != null && world != null) {
            float sunRadians = world.getSkyAngleRadians(tickDelta);
            Vector2f angleBegin = PsycheMatrixHelper.fromPolar(sunRadians + SUN_RADIANS, SUN_DISTANCE);
            Vector2f angleEnd = PsycheMatrixHelper.fromPolar(sunRadians - SUN_RADIANS, SUN_DISTANCE);

            float newSunAlpha = (1 - world.getRainGradient(tickDelta)) * (
                      (checkIntersection(world, renderEntity, tickDelta, new Vec3d(-angleBegin.x, angleBegin.y, -SUN_WIDTH)) ? 0.25F : 0)
                    + (checkIntersection(world, renderEntity, tickDelta, new Vec3d(-angleBegin.x, angleBegin.y, SUN_WIDTH)) ? 0.25F : 0)
                    + (checkIntersection(world, renderEntity, tickDelta, new Vec3d(-angleEnd.x, angleEnd.y, -SUN_WIDTH)) ? 0.25F : 0)
                    + (checkIntersection(world, renderEntity, tickDelta, new Vec3d(-angleEnd.x, angleEnd.y, SUN_WIDTH)) ? 0.25F : 0)
            );
            actualSunAlpha = Math.min(1, MathUtils.nearValue(actualSunAlpha, newSunAlpha, 0.1f, 0.01f));
        }
    }

    private boolean checkIntersection(World world, Entity entity, float tickDelta, Vec3d offset) {
        Vec3d start = entity.getCameraPosVec(tickDelta);
        return world.raycast(new RaycastContext(start, start.add(offset),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.ANY, entity))
                .getType() == Type.MISS;
    }

    protected float getIntensity() {
        return PsychedelicraftClient.getConfig().visual.sunFlareIntensity;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int screenWidth, int screenHeight, float tickDelta, @Nullable PingPong pingPong) {
        if (pingPong != null) {
            pingPong.pingPong();
            ScreenEffect.drawScreen(screenWidth, screenHeight);
        }

        if (actualSunAlpha <= 0) {
            return;
        }

        World world = client.world;
        Entity renderEntity = client.getCameraEntity();

        float genSize = screenWidth > screenHeight ? screenWidth : screenHeight;
        float sunRadians = world.getSkyAngleRadians(tickDelta);

        Vector3f sunPositionOnScreen = PsycheMatrixHelper.projectPointCurrentView(new Vector3f(
                -MathHelper.sin(sunRadians) * 120,
                MathHelper.cos(sunRadians) * 120,
                0
        ));

        Vector3f normSunPos = sunPositionOnScreen.normalize(new Vector3f());

        if (sunPositionOnScreen.z <= 0) {
            return;
        }

        float xDist = normSunPos.x * screenWidth;
        float yDist = normSunPos.y * screenHeight;

        int colorValue = world.getBiome(renderEntity.getBlockPos()).value().getFogColor();
        int fogRed = NativeImage.getRed(colorValue);
        int fogGreen = NativeImage.getGreen(colorValue);
        int fogBlue = NativeImage.getBlue(colorValue);

        int zIndex = -0;
        float alpha = Math.min(1, sunPositionOnScreen.z);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ONE, DstFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float screenCenterX = screenWidth * 0.5f;
        float screenCenterY = screenHeight * 0.5f;

        for (int i = 0; i < FLARE_SIZES.length; i++) {
            float flareSizeHalf = FLARE_SIZES[i] * genSize * 0.5f;
            float flareCenterX = screenCenterX + xDist * FLARE_INFLUENCES[i];
            float flareCenterY = screenCenterY + yDist * FLARE_INFLUENCES[i];

            RenderSystem.setShaderColor(fogRed - 0.1F, fogGreen - 0.1F, fogBlue - 0.1F, (alpha * i == 8 ? 1F : 0.5F) * actualSunAlpha * getIntensity());
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setShaderTexture(0, FLARES[i]);

            buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            buffer.vertex(positionMatrix, flareCenterX - flareSizeHalf, flareCenterY + flareSizeHalf, zIndex).texture(0, 1).next();
            buffer.vertex(positionMatrix, flareCenterX + flareSizeHalf, flareCenterY + flareSizeHalf, zIndex).texture(1, 1).next();
            buffer.vertex(positionMatrix, flareCenterX + flareSizeHalf, flareCenterY - flareSizeHalf, zIndex).texture(1, 0).next();
            buffer.vertex(positionMatrix, flareCenterX - flareSizeHalf, flareCenterY - flareSizeHalf, zIndex).texture(0, 0).next();
            tessellator.draw();
        }

        // Looks weird because of a hard edge... :|
        float genDist = 1 - (normSunPos.x * normSunPos.x + normSunPos.y * normSunPos.y);
        float blendingSize = (genDist - 0.1F) * getIntensity() * 250F * genSize;

        if (blendingSize > 0) {
            float blendingSizeHalf = blendingSize * 0.5F;
            float blendCenterX = screenCenterX + xDist;
            float blendCenterY = screenCenterY + yDist;
            float blendAlpha = Math.min(1, blendingSize / genSize / 150F);

            RenderSystem.setShaderColor(fogRed - 0.1F, fogGreen - 0.1F, fogBlue - 0.1F, blendAlpha * actualSunAlpha);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ONE, DstFactor.ZERO);
            RenderSystem.setShaderTexture(0, BLINDNESS_OVERLAY);
            buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            buffer.vertex(positionMatrix, blendCenterX - blendingSizeHalf, blendCenterY + blendingSizeHalf, zIndex).texture(0, 1).next();
            buffer.vertex(positionMatrix, blendCenterX + blendingSizeHalf, blendCenterY + blendingSizeHalf, zIndex).texture(1, 1).next();
            buffer.vertex(positionMatrix, blendCenterX + blendingSizeHalf, blendCenterY - blendingSizeHalf, zIndex).texture(1, 0).next();
            buffer.vertex(positionMatrix, blendCenterX - blendingSizeHalf, blendCenterY - blendingSizeHalf, zIndex).texture(0, 0).next();
            tessellator.draw();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    @Override
    public void close() { }
}