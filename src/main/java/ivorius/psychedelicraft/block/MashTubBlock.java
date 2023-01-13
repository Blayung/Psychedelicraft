/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import ivorius.psychedelicraft.screen.PSScreenHandlers;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;

/**
 * Created by lukas on 27.10.14.
 * Updated by Sollace on 12 Jan 2023
 */
public class MashTubBlock extends BlockWithFluid<MashTubBlockEntity> {
    public static final int SIZE = 15;
    public static final int BORDER_SIZE = 1;
    public static final int HEIGHT = 16;

    public static final BooleanProperty MASTER = BooleanProperty.of("master");

    private static final VoxelShape SHAPE = VoxelShapes.union(
            createShape(-8, -0.5F, -8, 32, 16,  1),
            createShape(-8, -0.5F, 23, 32, 16,  1),
            createShape(23, -0.5F, -8,  1, 16, 32),
            createShape(-8, -0.5F, -8,  1, 16, 32),
            createShape(-8, -0.5F, -8, 32,  1, 32)
    );

    private static VoxelShape createShape(double x, double y, double z, double width, double height, double depth) {
        return Block.createCuboidShape(x, y, z, x + width, y + height, z + depth);
    }

    public MashTubBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(MASTER)) {
            return SHAPE;
        }
        BlockPos center = getBlockEntityPos(world, state, pos);
        if (center.equals(pos)) {
            return VoxelShapes.fullCube();
        }
        return SHAPE.offset(center.getX() - pos.getX(), 0, center.getZ() - pos.getZ());
    }

    @Override
    protected BlockEntityType<MashTubBlockEntity> getBlockEntityType() {
        return PSBlockEntities.MASH_TUB;
    }

    @Override
    protected ScreenHandlerType<FluidContraptionScreenHandler<MashTubBlockEntity>> getScreenHandlerType() {
        return PSScreenHandlers.MASH_TUB;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(MASTER, true);
    }

    @Override
    protected ActionResult onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, MashTubBlockEntity blockEntity) {
        if (!blockEntity.solidContents.isEmpty()) {
            Block.dropStack(world, pos, blockEntity.solidContents);
            blockEntity.solidContents = ItemStack.EMPTY;
            blockEntity.markDirty();
            if (!world.isClient) {
                ((ServerWorld)world).getChunkManager().markForUpdate(pos);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return getPlacementPosition(world, state, pos, false).isPresent();
    }

    private Optional<BlockPos> getPlacementPosition(WorldView world, BlockState state, BlockPos pos, boolean premitOverlap) {
        return BlockPos.streamOutwards(pos, 1, 0, 1)
                .filter(center -> BlockPos.streamOutwards(center, 1, 0, 1).allMatch(p -> {
                    BlockState s = world.getBlockState(p);
                    return world.isAir(p) || s.isReplaceable() || (premitOverlap && s.isOf(this));
                }))
                .findFirst()
                .map(p -> p.toImmutable());
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        getPlacementPosition(world, state, pos, true).ifPresent(center -> {
            BlockPos.iterateOutwards(center, 1, 0, 1).forEach(p -> {
                world.setBlockState(p, getDefaultState().with(MASTER, false));
            });
            world.setBlockState(center, getDefaultState().with(MASTER, true));
        });

        super.onPlaced(world, pos, state, placer, stack);
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        BlockPos center = getBlockEntityPos(world, state, pos);
        if (center.equals(pos) && !state.get(MASTER)) {
            return;
        }
        BlockPos.iterateOutwards(center, 1, 0, 1).forEach(p -> {
            if (world.getBlockState(p).isOf(this)) {
                world.setBlockState(p, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            }
        });
    }

    @Override
    public BlockPos getBlockEntityPos(BlockView world, BlockState state, BlockPos pos) {
        return BlockPos.findClosest(pos, 1, 0, p -> {
            BlockState s = world.getBlockState(p);
            return s.isOf(this) && s.get(MASTER);
         }).orElse(pos);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (!state.get(MASTER)) {
            return null;
        }
        return super.createBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MASTER);
    }
}