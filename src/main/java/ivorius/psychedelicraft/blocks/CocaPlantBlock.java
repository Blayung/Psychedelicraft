/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class CocaPlantBlock extends CannabisPlantBlock {
    public CocaPlantBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected int getMaxAge(BlockState state) {
        return 12;
    }

    @Override
    protected float getRandomGrothChance() {
        return 0.1F;
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.FARMLAND) || floor.isOf(this) || floor.isOf(Blocks.DIRT) || floor.isOf(Blocks.GRASS_BLOCK);
    }
/*
    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune)
    {
        ArrayList<ItemStack> drops = new ArrayList<>();

        int countL = world.rand.nextInt(meta / 3 + 1) + meta / 5;
        for (int i = 0; i < countL; i++)
            drops.add(new ItemStack(PSItems.cocaLeaf, 1, 0));

        int countS = meta / 8;
        for (int i = 0; i < countS; i++)
            drops.add(new ItemStack(PSItems.cocaSeeds, 1, 0));

        return drops;
    }
*/
}