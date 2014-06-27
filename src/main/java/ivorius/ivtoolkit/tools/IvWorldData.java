/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.tools;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lukas on 24.05.14.
 */
public class IvWorldData
{
    public IvBlockCollection blockCollection;
    public List<TileEntity> tileEntities;
    public List<Entity> entities;

    public IvWorldData(IvBlockCollection blockCollection, List<TileEntity> tileEntities, List<Entity> entities)
    {
        this.blockCollection = blockCollection;
        this.tileEntities = tileEntities;
        this.entities = entities;
    }

    public IvWorldData(World world, BlockArea blockArea, boolean captureEntities)
    {
        int[] size = blockArea.areaSize();
        blockCollection = new IvBlockCollection(size[0], size[1], size[2]);

        tileEntities = new ArrayList<>();
        for (BlockCoord worldCoord : blockArea)
        {
            BlockCoord dataCoord = worldCoord.subtract(blockArea.getLowerCorner());

            blockCollection.setBlock(dataCoord, world.getBlock(worldCoord.x, worldCoord.y, worldCoord.z));
            blockCollection.setMetadata(dataCoord, (byte) world.getBlockMetadata(worldCoord.x, worldCoord.y, worldCoord.z));

            TileEntity tileEntity = world.getTileEntity(worldCoord.x, worldCoord.y, worldCoord.z);
            if (tileEntity != null)
            {
                tileEntities.add(tileEntity);
            }
        }

        if (captureEntities)
        {
            entities = world.getEntitiesWithinAABBExcludingEntity(null, blockArea.asAxisAlignedBB());
            Iterator<Entity> entityIterator = entities.iterator();
            while (entityIterator.hasNext())
            {
                Entity entity = entityIterator.next();

                if (entity instanceof EntityPlayer)
                {
                    entityIterator.remove();
                }
            }
        }
        else
        {
            entities = Collections.emptyList();
        }
    }

    public IvWorldData(NBTTagCompound compound, World world)
    {
        blockCollection = new IvBlockCollection(compound.getCompoundTag("blockCollection"));

        NBTTagList teList = compound.getTagList("tileEntities", Constants.NBT.TAG_COMPOUND);
        tileEntities = new ArrayList<>(teList.tagCount());
        for (int i = 0; i < teList.tagCount(); i++)
        {
            TileEntity tileEntity = TileEntity.createAndLoadEntity(teList.getCompoundTagAt(i));

            tileEntities.add(tileEntity);
        }

        if (world != null)
        {
            NBTTagList entityList = compound.getTagList("entities", Constants.NBT.TAG_COMPOUND);
            entities = new ArrayList<>(entityList.tagCount());
            for (int i = 0; i < entityList.tagCount(); i++)
            {
                Entity entity = EntityList.createEntityFromNBT(entityList.getCompoundTagAt(i), world);

                entities.add(entity);
            }
        }
    }

    public NBTTagCompound createTagCompound(BlockCoord referenceCoord)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setTag("blockCollection", blockCollection.createTagCompound());

        NBTTagList teList = new NBTTagList();
        for (TileEntity tileEntity : tileEntities)
        {
            NBTTagCompound teCompound = new NBTTagCompound();
            tileEntity.xCoord -= referenceCoord.x;
            tileEntity.yCoord -= referenceCoord.y;
            tileEntity.zCoord -= referenceCoord.z;
            tileEntity.writeToNBT(teCompound);
            tileEntity.xCoord += referenceCoord.x;
            tileEntity.yCoord += referenceCoord.y;
            tileEntity.zCoord += referenceCoord.z;

            teList.appendTag(teCompound);
        }
        compound.setTag("tileEntities", teList);

        NBTTagList entityList = new NBTTagList();
        for (Entity entity : entities)
        {
            NBTTagCompound teCompound = new NBTTagCompound();
            entity.posX -= referenceCoord.x;
            entity.posY -= referenceCoord.y;
            entity.posZ -= referenceCoord.z;
            entity.writeToNBTOptional(teCompound);
            entity.posX += referenceCoord.x;
            entity.posY += referenceCoord.y;
            entity.posZ += referenceCoord.z;

            entityList.appendTag(teCompound);
        }
        compound.setTag("entities", entityList);

        return compound;
    }
}
