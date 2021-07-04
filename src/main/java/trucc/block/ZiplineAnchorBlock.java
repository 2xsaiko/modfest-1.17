package trucc.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import trucc.block.entity.ZiplineAnchorBlockEntity;
import trucc.util.CableConnectUtil;

import net.dblsaiko.qcommon.croco.Vec3;

public class ZiplineAnchorBlock extends Block implements BlockEntityProvider, BlockCablePlacement {
    private static final Map<Direction, VoxelShape> SHAPES;
    private static final Map<Direction, VoxelShape> OUTLINE_SHAPES;

    private final WeakHashMap<PlayerEntity, BlockPos> queuedLinkActions = new WeakHashMap<>();

    public ZiplineAnchorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (CableConnectUtil.onConnectUse(world, pos, player, hand)) {
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public Vec3 getWireOffset(World world, BlockPos pos, BlockState state, BlockPos other) {
        return Vec3.from(state.get(Properties.FACING).getOpposite().getVector()).mul(10 / 32f);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getSide();
        BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(direction.getOpposite()));

        if (blockState.isOf(this) && blockState.get(Properties.FACING) == direction) {
            return this.getDefaultState().with(Properties.FACING, direction.getOpposite());
        } else {
            return this.getDefaultState().with(Properties.FACING, direction);
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPES.get(state.get(Properties.FACING));
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return SHAPES.get(state.get(Properties.FACING));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.FACING);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ZiplineAnchorBlockEntity(pos, state);
    }

    static {
        VoxelShape template = VoxelShapes.union(
            VoxelShapes.cuboid(7 / 16.0, 0 / 16.0, 6 / 16.0, 9 / 16.0, 1 / 16.0, 10 / 16.0),
            VoxelShapes.cuboid(7 / 16.0, 1 / 16.0, 7 / 16.0, 9 / 16.0, 2 / 16.0, 9 / 16.0),
            VoxelShapes.cuboid(7 / 16.0, 4 / 16.0, 7 / 16.0, 9 / 16.0, 5 / 16.0, 9 / 16.0),
            VoxelShapes.cuboid(7 / 16.0, 2 / 16.0, 6 / 16.0, 9 / 16.0, 4 / 16.0, 7 / 16.0),
            VoxelShapes.cuboid(7 / 16.0, 2 / 16.0, 9 / 16.0, 9 / 16.0, 4 / 16.0, 10 / 16.0)
        );

        VoxelShape outlineTemplate = VoxelShapes.cuboid(template.getBoundingBox());

        SHAPES = Collections.unmodifiableMap(Arrays.stream(Direction.values())
            .collect(Collectors.toMap(
                d1 -> d1,
                d1 -> rotate(template, d1),
                (a1, b1) -> a1,
                () -> new EnumMap<>(Direction.class))
            )
        );
        OUTLINE_SHAPES = Collections.unmodifiableMap(Arrays.stream(Direction.values())
            .collect(Collectors.toMap(
                d -> d,
                d -> rotate(outlineTemplate, d),
                (a, b) -> a,
                () -> new EnumMap<>(Direction.class))
            )
        );
    }

    private static VoxelShape rotate(VoxelShape in, Direction d) {
        return in.getBoundingBoxes()
            .stream()
            .map(el -> rotate(el, d))
            .map(VoxelShapes::cuboid)
            .reduce(VoxelShapes.empty(), VoxelShapes::union);
    }

    private static Box rotate(Box in, Direction d) {
        return switch (d) {
            case DOWN -> new Box(in.maxX, 1.0 - in.maxY, 1.0 - in.minZ, in.minX, 1.0 - in.minY, 1.0 - in.maxZ);
            case UP -> in;
            case NORTH -> new Box(in.maxX, in.maxZ, 1.0 - in.minY, in.minX, in.minZ, 1.0 - in.maxY);
            case SOUTH -> new Box(in.maxX, 1.0 - in.maxZ, in.minY, in.minX, 1.0 - in.minZ, in.maxY);
            case WEST -> new Box(1.0 - in.maxY, in.maxZ, in.minX, 1.0 - in.minY, in.minZ, in.maxX);
            case EAST -> new Box(in.maxY, 1.0 - in.maxZ, in.minX, in.minY, 1.0 - in.minZ, in.maxX);
        };
    }
}
