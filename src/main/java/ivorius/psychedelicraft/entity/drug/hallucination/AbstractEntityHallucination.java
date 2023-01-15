/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;

import ivorius.psychedelicraft.client.render.PassThroughVertexConsumer;

public abstract class AbstractEntityHallucination extends Hallucination {

    protected Entity entity;

    protected int maxAge;

    protected float[] color;

    protected float scale;

    public AbstractEntityHallucination(PlayerEntity player) {
        super(player);
    }

    @Override
    public int getMaxHallucinations() {
        return UNLIMITED;
    }

    @Override
    public boolean isDead() {
        return age >= maxAge;
    }

    @Override
    public void update() {
        super.update();

        entity.prevX = entity.getX();
        entity.prevY = entity.getY();
        entity.prevZ = entity.getZ();

        entity.prevYaw = entity.getYaw();
        entity.prevPitch = entity.getPitch();

        animateEntity();

        if (entity instanceof LivingEntity living) {
            living.updateLimbs(living, false);
        }
    }

    protected abstract void animateEntity();

    private float dAlpha;
    private final PassThroughVertexConsumer.Parameters colourSpace = new PassThroughVertexConsumer.Parameters().color((parent, r, g, b, a) -> {
        parent.color(color[0], color[1], color[2], dAlpha);
    });

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, Camera camera, float tickDelta, float dAlpha) {
        this.dAlpha = Math.min(1,
                MathHelper.sin(
                        (float) Math.min(age, maxAge - 2) / (float) (maxAge - 2)
                        * MathHelper.PI) * 18) * dAlpha;

        if (this.dAlpha <= 0) {
            return;
        }

        Vec3d cameraPos = camera.getPos();

        double x = MathHelper.lerp(tickDelta, entity.prevX, entity.getX()) - cameraPos.x;
        double y = MathHelper.lerp(tickDelta, entity.prevY, entity.getY()) - cameraPos.y;
        double z = MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ()) - cameraPos.z;
        float pitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch(tickDelta));
        float yaw = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw(tickDelta));

        entity.lastRenderX = entity.getX();
        entity.lastRenderY = entity.getY();
        entity.lastRenderZ = entity.getZ();

        matrices.push();
        matrices.translate(x, y, z);
        matrices.scale(scale, scale, scale);
        matrices.translate(-x, -y, -z);

        renderModel(matrices, layer -> {
            var dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();

            var renderer = dispatcher.getRenderer(entity);
            layer = RenderLayer.getEntityTranslucent(renderer.getTexture(entity));

            return PassThroughVertexConsumer.of(
                    vertices.getBuffer(layer),
                    colourSpace
            );
        }, x, y, z, pitch, yaw, tickDelta);
        matrices.pop();
    }

    protected void renderModel(MatrixStack matrices, VertexConsumerProvider vertices, double x, double y, double z, float pitch, float yaw, float tickDelta) {
        var dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        dispatcher.render(
                entity,
                x, y, z, yaw, tickDelta, matrices,
                vertices,
                dispatcher.getLight(entity, tickDelta)
        );
    }
}