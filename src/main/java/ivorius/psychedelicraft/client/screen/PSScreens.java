package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import ivorius.psychedelicraft.screen.PSScreenHandlers;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.HandledScreens.Provider;

/**
 * @author Sollace
 * @since 13 Jan 2023
 */
public interface PSScreens {
    static void bootstrap() {
        HandledScreens.register(PSScreenHandlers.DRYING_TABLE, DryingTableScreen::new);
        HandledScreens.register(PSScreenHandlers.BARREL, BarrelScreen::new);
        HandledScreens.register(PSScreenHandlers.DISTILLERY, DistilleryScreen::new);
        HandledScreens.register(PSScreenHandlers.FLASK, (Provider<FluidContraptionScreenHandler<FlaskBlockEntity>, FlaskScreen<FlaskBlockEntity>>)((h, i, t) -> new FlaskScreen<>(h, i, t)));
        HandledScreens.register(PSScreenHandlers.MASH_TUB, MushTubScreen::new);
    }
}
