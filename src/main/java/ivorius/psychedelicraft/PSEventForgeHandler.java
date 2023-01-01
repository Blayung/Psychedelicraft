/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import ivorius.psychedelicraft.blocks.PSBlocks;
import ivorius.psychedelicraft.client.sound.MovingSoundDrug;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import ivorius.psychedelicraft.fluids.FluidAlcohol;
import ivorius.psychedelicraft.fluids.FluidWithIconSymbolRegistering;
import ivorius.psychedelicraft.fluids.PSFluids;
import ivorius.psychedelicraft.items.PSItems;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

/**
 * Created by lukas on 18.02.14.
 */
@Deprecated(forRemoval = true)
public class PSEventForgeHandler
{
    public static boolean containsAlcohol(FluidStack fluidStack, FluidAlcohol fluid, Boolean distilled, int minMatured)
    {
        return fluidStack != null
                && fluidStack.getFluid() == fluid
                && (distilled == null || (fluid.getDistillation(fluidStack) > 0) == distilled)
                && fluid.getMaturation(fluidStack) >= minMatured;
    }

    //@SubscribeEvent
    public void onServerChat(ServerChatEvent event)
    {
        if (PSConfig.distortOutgoingMessages)
        {
            Object[] args = event.component.getFormatArgs();

            if (args.length >= 2 && args[1] instanceof ChatComponentText)
            {
                DrugProperties drugProperties = DrugProperties.getDrugProperties(event.player);

                if (drugProperties != null)
                {
                    String message = event.message;
                    String modified = drugProperties.messageDistorter.distortOutgoingMessage(drugProperties, event.player, event.player.getRNG(), message);
                    if (!modified.equals(message))
                        args[1] = ForgeHooks.newChatWithLinks(modified); // See NetHandlerPlayServer
                }
            }
            else
            {
                Psychedelicraft.logger.warn("Failed distorting outgoing text message! Args: " + Arrays.toString(args));
            }
        }
    }

//    @SubscribeEvent
//    public void onClientChatReceived(ClientChatReceivedEvent event)
//    {
//        // Doesn't work, but is not used yet anyway
//        if (PSConfig.distortIncomingMessages && event.message instanceof ChatComponentText)
//        {
//            ChatComponentText text = (ChatComponentText) event.message;
//
//            EntityLivingBase renderEntity = Minecraft.getMinecraft().renderViewEntity;
//            DrugProperties drugProperties = DrugProperties.getDrugProperties(renderEntity);
//
//            if (drugProperties != null)
//            {
//                String message = text.getUnformattedTextForChat();
//                drugProperties.receiveChatMessage(renderEntity, message);
//                String modified = drugProperties.messageDistorter.distortIncomingMessage(drugProperties, renderEntity, renderEntity.getRNG(), message);
//
//                event.message = new ChatComponentText(modified);
//            }
//        }
//    }

    //@SubscribeEvent
    public void onPlayerSleep(PlayerSleepInBedEvent event)
    {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(event.entityLiving);

        if (drugProperties != null)
        {
            EntityPlayer.EnumStatus status = drugProperties.getDrugSleepStatus();

            if (status != null)
                event.result = status;
        }
    }

    //@SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(event.entity); // Initialize drug helper

        if (event.world.isRemote && drugProperties != null)
            initializeMovingSoundDrug(event.entity, drugProperties);
    }

    //@SubscribeEvent
    public void onEntityConstruction(EntityEvent.EntityConstructing event)
    {
        if (event.entity instanceof EntityPlayer)
            DrugProperties.initInEntity(event.entity);
    }

    //@SubscribeEvent
    public void getBreakSpeed(PlayerEvent.BreakSpeed event)
    {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(event.entity);

        if (drugProperties != null)
        {
            event.newSpeed = event.newSpeed * drugProperties.getDigSpeedModifier(event.entityLiving);
        }
    }

    //@SubscribeEvent
    public void wakeUpPlayer(PlayerWakeUpEvent event)
    {
        if (!event.wakeImmediatly)
        {
            DrugProperties drugProperties = DrugProperties.getDrugProperties(event.entityPlayer);

            if (drugProperties != null)
                drugProperties.wakeUp(event.entityPlayer);
        }
    }
}