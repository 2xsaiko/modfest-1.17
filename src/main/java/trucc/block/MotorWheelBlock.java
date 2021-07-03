package trucc.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import trucc.block.entity.MotorWheelBlockEntity;

public class MotorWheelBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    private static final Map<Direction, VoxelShape> SHAPES;

    public MotorWheelBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return SHAPES.get(state.get(FACING));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MotorWheelBlockEntity(pos, state);
    }

    static {
        VoxelShape top = VoxelShapes.cuboid(0 / 16.0, 12 / 16.0, 0 / 16.0, 16 / 16.0, 16 / 16.0, 16 / 16.0);
        VoxelShape bottom = VoxelShapes.cuboid(0 / 16.0, 0 / 16.0, 0 / 16.0, 16 / 16.0, 8 / 16.0, 16 / 16.0);
        VoxelShape base = VoxelShapes.union(top, bottom);

        EnumMap<Direction, VoxelShape> m = new EnumMap<>(Direction.class);

        m.put(Direction.NORTH, VoxelShapes.union(base, VoxelShapes.cuboid(0 / 16.0, 8 / 16.0, 12 / 16.0, 16 / 16.0, 12 / 16.0, 16 / 16.0)));
        m.put(Direction.SOUTH, VoxelShapes.union(base, VoxelShapes.cuboid(0 / 16.0, 8 / 16.0, 0 / 16.0, 16 / 16.0, 12 / 16.0, 4 / 16.0)));
        m.put(Direction.WEST, VoxelShapes.union(base, VoxelShapes.cuboid(12 / 16.0, 8 / 16.0, 0 / 16.0, 16 / 16.0, 12 / 16.0, 16 / 16.0)));
        m.put(Direction.EAST, VoxelShapes.union(base, VoxelShapes.cuboid(0 / 16.0, 8 / 16.0, 0 / 16.0, 4 / 16.0, 12 / 16.0, 16 / 16.0)));

        SHAPES = Collections.unmodifiableMap(m);
    }
}
