package ivorius.psychedelicraft.block;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;

public class GlassTubeBlock extends BlockWithEntity implements PipeInsertable {
    public static final MapCodec<GlassTubeBlock> CODEC = createCodec(GlassTubeBlock::new);

    public static final EnumProperty<IODirection> IN = EnumProperty.of("in", IODirection.class);
    public static final EnumProperty<IODirection> OUT = EnumProperty.of("out", IODirection.class);

    protected GlassTubeBlock(Settings settings) {
        super(settings);
        setDefaultState(stateManager.getDefaultState()
                .with(IN, IODirection.NONE)
                .with(OUT, IODirection.NONE)
        );
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(IN, OUT);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        IODirection placedDir = IODirection.LOOKUP.get(ctx.getSide());
        BlockState state = super.getPlacementState(ctx);
        IODirection out = getValidConnections(ctx.getWorld(), ctx.getBlockPos(), state, IN, placedDir).findFirst().orElse(placedDir);
        IODirection in = getValidConnections(ctx.getWorld(), ctx.getBlockPos(), state.with(OUT, out), OUT, out).findFirst().orElse(out.getOpposite());
        return setDirection(state, in, out);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return setDirection(state,
                getConnectionStateForNeighborUpdate(pos, state, direction, neighborPos, neighborState, world, IN),
                getConnectionStateForNeighborUpdate(pos, state, direction, neighborPos, neighborState, world, OUT)
        );
    }

    private IODirection getConnectionStateForNeighborUpdate(BlockPos pos, BlockState state, Direction direction, BlockPos neighborPos, BlockState neighbor, WorldAccess world, EnumProperty<IODirection> property) {
        IODirection current = state.get(property);

        if (PipeInsertable.canConnectWith(world, state, pos, neighbor, neighborPos, direction, property == IN)) {
            if (current == IODirection.NONE || !PipeInsertable.canConnectWith(world, state, pos, world.getBlockState(pos.offset(current.direction)), pos.offset(current.direction), current.direction, property == IN)) {
                return IODirection.LOOKUP.get(direction);
            }
        } else if (current.direction == direction) {
            return IODirection.NONE;
        }
        return current;
    }

    @Override
    public boolean acceptsConnectionFrom(WorldAccess world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return neighborState.isOf(this) && (state.get(input ? OUT : IN).direction == direction);
    }

    static EnumProperty<IODirection> getInverseProperty(EnumProperty<IODirection> property) {
        return property == IN ? OUT : IN;
    }

    public BlockState setDirection(BlockState state, IODirection in, IODirection out) {
        if (in != IODirection.NONE || out != IODirection.NONE) {
            in = in == IODirection.NONE ? out.getOpposite() : in;
            out = out == IODirection.NONE || in == out ? in.getOpposite() : out;
        }
        return state.with(IN, in).with(OUT, out);
    }

    private static Stream<IODirection> getValidConnections(WorldAccess world, BlockPos pos, BlockState state, EnumProperty<IODirection> property, IODirection exclude) {
        return IODirection.LOOKUP.entrySet().stream().filter(pair -> {
            BlockPos nieghborPos = pos.offset(pair.getKey());
            BlockState neighborState = world.getBlockState(nieghborPos);
            var ss = state;
            IODirection dir = neighborState.getOrEmpty(property).orElse(null);
            if (dir == null && PipeInsertable.canConnectWith(world, state, pos, neighborState, nieghborPos, pair.getKey(), property == OUT)) {
                return true;
            }

            return (dir == pair.getValue().getOpposite() || dir == IODirection.NONE) && (exclude != IODirection.NONE && dir != exclude);
        }).map(Map.Entry::getValue);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.getBlockEntity(pos, PSBlockEntities.GLASS_TUBING).ifPresent(data -> {
            if (data.tank.getContents().amount() > 0 && state.get(OUT)
                    .getDirection()
                    .map(direction -> PipeInsertable.tryInsert(world, pos.offset(direction), direction, data.tank)).orElse(SPILL_STATUS) == SPILL_STATUS) {
                var fluid = data.tank.getContents().fluid();
                Direction outDirection = state.get(OUT).direction;
                Vector3f outVec = outDirection == null ? new Vector3f() : outDirection.getUnitVector();
                world.spawnParticles(
                        fluid.getPhysical().isOf(Fluids.WATER) ? ParticleTypes.DRIPPING_WATER
                            : fluid.getPhysical().isOf(Fluids.LAVA) ? ParticleTypes.DRIPPING_LAVA
                            : new FluidParticleEffect(PSParticles.DRIPPING_FLUID, fluid),
                        pos.getX() + 0.5 + outVec.x * 0.5,
                        pos.getY() + 0.5 + outVec.y * 0.5 - 0.2,
                        pos.getZ() + 0.5 + outVec.z * 0.5, 1, 0, 0, 0, 0);
                data.tank.drain(3);
            }
            if (data.tank.getContents().amount() > 0) {
                world.scheduleBlockTick(pos, this, 3);
            }
        });
    }


    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new Data(pos, state);
    }

    enum IODirection implements StringIdentifiable {
        NONE(null),
        UP(Direction.UP),
        DOWN(Direction.DOWN),
        NORTH(Direction.NORTH),
        SOUTH(Direction.SOUTH),
        EAST(Direction.EAST),
        WEST(Direction.WEST);

        static final Map<Direction, IODirection> LOOKUP = Arrays.stream(values())
                .filter(i -> i.getDirection().isPresent())
                .collect(Collectors.toMap(i -> i.getDirection().get(), Function.identity()));

        private final Direction direction;
        private final String name = name().toLowerCase(Locale.ROOT);

        IODirection(@Nullable Direction direction) {
            this.direction = direction;
        }

        public IODirection getOpposite() {
            return direction == null ? this : LOOKUP.getOrDefault(direction.getOpposite(), NONE);
        }

        public Optional<Direction> getDirection() {
            return Optional.ofNullable(direction);
        }

        @Override
        public String asString() {
            return name;
        }
    }

    @Override
    public int tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, ItemFluids fluids) {
        if (state.get(IN).direction == direction.getOpposite()) {
            return world.getBlockEntity(pos, PSBlockEntities.GLASS_TUBING).map(data -> {
                return data.tank.deposit(fluids);
            }).orElse(0);
        }

        return SPILL_STATUS;
    }

    public static class Data extends BlockEntity implements Resovoir.ChangeListener {
        private final Resovoir tank = new Resovoir(3, this);

        public Data(BlockPos pos, BlockState state) {
            super(PSBlockEntities.GLASS_TUBING, pos, state);
        }

        @Override
        public void onLevelChange(Resovoir tank, int change) {
            if (tank.getAmount() > 0 && this.getWorld() instanceof ServerWorld sw) {
                sw.scheduleBlockTick(getPos(), getCachedState().getBlock(), 3);
            }
            markDirty();
        }

        @Override
        protected void writeNbt(NbtCompound nbt, WrapperLookup lookup) {
            nbt.put("tank", tank.toNbt(lookup));
        }

        @Override
        protected void readNbt(NbtCompound nbt, WrapperLookup lookup) {
            tank.fromNbt(nbt.getCompound("tank"), lookup);
        }
    }
}
