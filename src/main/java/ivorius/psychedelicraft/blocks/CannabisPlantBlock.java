/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;

public class CannabisPlantBlock extends PlantBlock implements Fertilizable {
    public static final IntProperty AGE = Properties.AGE_15;
    public static final int MAX_AGE = 15;
    public static final int MATURATION_AGE = 11;

    private static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 16, 14);

    public CannabisPlantBlock(Settings settings) {
        super(settings.ticksRandomly().nonOpaque());
        setDefaultState(getDefaultState().with(AGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AGE);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    protected int getMaxAge(BlockState state) {
        return MAX_AGE;
    }

    protected int getMaxHeight() {
        return 3;
    }

    protected float getRandomGrothChance() {
        return 0.12F;
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.FARMLAND) || floor.isOf(this);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getLightLevel(pos.up()) >= 9 && random.nextFloat() < getRandomGrothChance()) {
            if (isFertilizable(world, pos, state, false)) {
                applyGrowth(world, random, pos, state, false);
            }
        }
    }

/*
    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune)
    {
        ArrayList<ItemStack> drops = new ArrayList<>();

        int countB = world.rand.nextInt(meta / 6 + 1);
        for (int i = 0; i < countB; i++)
            drops.add(new ItemStack(PSItems.cannabisBuds, 1, 0));

        int countL = world.rand.nextInt(meta / 5 + 1) + meta / 6;
        for (int i = 0; i < countL; i++)
            drops.add(new ItemStack(PSItems.cannabisLeaf, 1, 0));

        int countS = meta / 8;
        for (int i = 0; i < countS; i++)
            drops.add(new ItemStack(PSItems.cannabisSeeds, 1, 0));

        return drops;
    }

/*
    @Override
    public IIcon getIcon(int side, int meta)
    {
        if (meta < 4)
            return super.getIcon(side, meta);
        else if (meta < 8)
            return textures[0];
        else if (meta < 12)
            return textures[1];
        else if (meta < 16)
            return textures[2];

        return super.getIcon(side, meta);
    }
*/
    public void applyGrowth(World world, Random random, BlockPos pos, BlockState state, boolean bonemeal) {
        int number = bonemeal ? random.nextInt(4) + 1 : 1;

        for (int i = 0; i < number; i++) {
            final int age = state.get(CropBlock.AGE);
            final boolean freeOver = world.isAir(pos.up()) && getPlantSize(world, pos) < getMaxHeight();

            if ((age < getMaxAge(state) && freeOver) || (!freeOver && age < MATURATION_AGE)) {
                state = state.cycle(CropBlock.AGE);
                world.setBlockState(pos, state, Block.NOTIFY_ALL);
            } else if (world.isAir(pos.up()) && freeOver && age == getMaxAge(state)) {
                world.setBlockState(pos.up(), getDefaultState(), Block.NOTIFY_ALL);
            }
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean client) {
        final boolean freeOver = getPlantSize(world, pos) < getMaxHeight();
        final int age = state.get(AGE);
        return (age < getMaxAge(state) && freeOver)
            || (!freeOver && age < MATURATION_AGE)
            || (world.isAir(pos.up()) && freeOver && age == getMaxAge(state));
    }

    protected int getPlantSize(WorldView world, BlockPos pos) {
        int plantSize = 1;
        while (world.getBlockState(pos.down(plantSize)).isOf(this)) {
            ++plantSize;
        }
        return plantSize;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        applyGrowth(world, random, pos, state, true);
    }
}