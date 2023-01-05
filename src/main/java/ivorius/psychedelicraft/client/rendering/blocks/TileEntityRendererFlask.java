/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.client.rendering.FluidBoxRenderer;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 25.10.14.
 */
public class TileEntityRendererFlask extends TileEntitySpecialRenderer
{
    public static IModelCustom modelFlask = AdvancedModelLoader.loadModel(new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathModels + "flask.obj"));

    private IModelCustom model;
    private ResourceLocation texture;

    public TileEntityRendererFlask()
    {
        model = modelFlask;
        texture = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "flask.png");
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks)
    {
        FlaskBlockEntity flask = (FlaskBlockEntity) tileEntity;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5f, y + 0.502f, z + 0.5f);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, -.5f, 0f);
        GL11.glColor3f(1f, 1f, 1f);
        this.bindTexture(texture);
        model.renderAll();
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_CULL_FACE);

        FluidStack fluidStack = flask.containedFluid();
        if (fluidStack != null)
        {
            float fluidHeight = 2.8f * IvMathHelper.clamp(0.0f, (float) fluidStack.amount / (float) flask.tankCapacity(), 1.0f);

            FluidBoxRenderer fluidBoxRenderer = new FluidBoxRenderer(1.0f / 16.0f);
            fluidBoxRenderer.prepare(fluidStack);

            fluidBoxRenderer.renderFluid(-1.9f, -8.0f, -3.9f, 3.8f, fluidHeight, 0.9f, ForgeDirection.NORTH, ForgeDirection.UP);
            fluidBoxRenderer.renderFluid(-1.9f, -8.0f, 3.0f, 3.8f, fluidHeight, 0.9f, ForgeDirection.SOUTH, ForgeDirection.UP);
            fluidBoxRenderer.renderFluid(-3.9f, -8.0f, -1.9f, 0.9f, fluidHeight, 3.8f, ForgeDirection.WEST, ForgeDirection.UP);
            fluidBoxRenderer.renderFluid(3.0f, -8.0f, -1.9f, 0.9f, fluidHeight, 3.8f, ForgeDirection.EAST, ForgeDirection.UP);
            fluidBoxRenderer.renderFluid(-3.0f, -8.0f, -3.0f, 6.0f, fluidHeight, 6.0f, ForgeDirection.UP);

            fluidBoxRenderer.cleanUp();
        }

        GL11.glPopMatrix();
    }
}
