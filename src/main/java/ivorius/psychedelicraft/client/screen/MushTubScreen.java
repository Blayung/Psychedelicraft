package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 13.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class MushTubScreen extends FluidContraptionScreen<MashTubBlockEntity> {
    public static final Identifier BACKGROUND = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "container_woodenVat.png");

    public MushTubScreen(FluidContraptionScreenHandler<MashTubBlockEntity> handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected Identifier getBackgroundTexture() {
        return BACKGROUND;
    }

    @Override
    protected void drawAdditionalInfo(MatrixStack matrices, int baseX, int baseY) {
        int timeLeftFermenting = handler.getBlockEntity().getProgress(24);
        if (timeLeftFermenting < 24) {
            drawTexture(matrices, baseX + 23, baseY + 14, 176, 0, 24 - timeLeftFermenting, 17);
        }
    }

    @Override
    protected List<Text> getAdditionalTankText() {
        return handler.getBlockEntity().isActive()
                ? Arrays.asList(Text.translatable("fluid.status.fermenting").formatted(Formatting.GREEN))
                : List.of();
    }
}