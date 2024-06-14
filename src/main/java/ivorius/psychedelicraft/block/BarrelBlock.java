/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import ivorius.psychedelicraft.block.entity.BarrelBlockEntity;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import ivorius.psychedelicraft.screen.PSScreenHandlers;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BarrelBlock extends BlockWithFluid<BarrelBlockEntity> {
    public static final MapCodec<BarrelBlock> CODEC = createCodec(BarrelBlock::new);
    public static final int MAX_TAP_AMOUNT = FluidVolumes.BUCKET;
    public static final DirectionProperty FACING = Properties.HOPPER_FACING;
    public static final BooleanProperty TAPPED = BooleanProperty.of("tapped");

    private static final Map<Axis, VoxelShape> STANDING_SHAPES = Map.of(
        Axis.X, VoxelShapes.union(
            Block.createCuboidShape(0, 5, 2, 16, 13, 14),
            Block.createCuboidShape(0, 3, 4, 16, 15, 12)
        ),
        Axis.Y, VoxelShapes.union(
            Block.createCuboidShape(2, 0, 4, 14, 16, 12),
            Block.createCuboidShape(4, 0, 2, 12, 16, 14)
        ),
        Axis.Z, VoxelShapes.union(
            Block.createCuboidShape(2, 5, 0, 14, 13, 16),
            Block.createCuboidShape(4, 3, 0, 12, 15, 16)
        )
    );

    public BarrelBlock(Settings settings) {
        super(settings.nonOpaque());
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(TAPPED, true));
    }

    @Override
    protected MapCodec<? extends BarrelBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, TAPPED);
    }

    @Override
    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return STANDING_SHAPES.get(state.get(FACING).getAxis());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction side = ctx.getPlayerLookDirection().getOpposite();
        return getDefaultState().with(FACING, side.getAxis() == Axis.Y ? Direction.DOWN : side);
    }

    @Override
    protected ItemActionResult onInteractWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BarrelBlockEntity blockEntity) {
        int capacity = FluidCapacity.get(stack);
        if (!state.get(TAPPED) || state.get(FACING).getAxis() == Axis.Y || capacity == 0) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (ItemFluids.of(stack).amount() < capacity) {
            Resovoir tank = blockEntity.getTankOnSide(Direction.DOWN);
            if (tank.getContents().amount() > 0 && tank.getContents().fluid().isSuitableContainer(stack)) {
                if (!world.isClient) {

                    ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copyWithCount(1));

                    if (tank.withdraw(t, MAX_TAP_AMOUNT) > 0) {
                        if (stack.getCount() > 1) {
                            stack.decrement(1);
                            player.getInventory().offerOrDrop(t.toItemStack());
                        } else {
                            player.setStackInHand(hand, t.toItemStack());
                        }
                    }

                    blockEntity.timeLeftTapOpen = 20;
                    blockEntity.markForUpdate();
                }

                return ItemActionResult.SUCCESS;
            }
        }

        return ItemActionResult.FAIL;
    }

    @Override
    protected BlockEntityType<BarrelBlockEntity> getBlockEntityType() {
        return PSBlockEntities.BARREL;
    }

    @Override
    protected ScreenHandlerType<FluidContraptionScreenHandler<BarrelBlockEntity>> getScreenHandlerType() {
        return PSScreenHandlers.BARREL;
    }

    @Override
    @Nullable
    public <Q extends BlockEntity> BlockEntityTicker<Q> getTicker(World world, BlockState state, BlockEntityType<Q> type) {
        return world.isClient ? validateTicker(type, getBlockEntityType(), (w, p, s, entity) -> entity.tickAnimations()) : super.getTicker(world, state, type);
    }
}
