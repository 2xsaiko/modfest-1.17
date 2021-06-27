package trucc.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import com.mojang.datafixers.util.Pair;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import trucc.Trucc;
import trucc.block.entity.MotorWheelBlockEntity;
import trucc.util.CableConnectUtil;

import net.dblsaiko.qcommon.croco.Vec2;
import net.dblsaiko.qcommon.croco.Vec3;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MotorWheelBlock extends HorizontalFacingBlock implements BlockEntityProvider, BlockCablePlacement {
    private static final Map<Direction, VoxelShape> SHAPES;

    public MotorWheelBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (CableConnectUtil.onConnectUse(world, pos, player, hand)) {
            return ActionResult.SUCCESS;
        }

        BlockEntity e = world.getBlockEntity(pos);

        if (player.getStackInHand(hand).isEmpty() && e instanceof MotorWheelBlockEntity entity) {
            boolean reverse = player.isSneaking();

            float speed = entity.getSpeed();

            if (reverse) {
                speed -= 0.5f;
            } else {
                speed += 0.5f;
            }

            speed = min(max(-20.0f, speed), 20.0f);

            entity.setSpeed(speed);
            player.sendMessage(new LiteralText("speed %f".formatted(speed)), true);
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    @Nullable
    public Vec3 getWireOffset(World world, BlockPos pos, BlockState state, BlockPos other) {
        return null;
    }

    @Override
    public Vec3 getDependentWireOffset(World world, BlockPos pos, BlockState state, BlockPos other, Vec3 otherOffset) {
        Vec3 offset = Vec3.from(state.get(FACING).getVector()).mul(4 / 16f);
        Vec3 center = new Vec3(0.0f, 2 / 16f, 0.0f);
        Pair<Vec3, Vec3> results = calculateWireTouchPoint3d(6 / 16f, center.add(offset), Vec3.from(other.subtract(pos)).add(otherOffset));

        Vec3 result = results.getFirst();
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof MotorWheelBlockEntity be) {
            Set<BlockPos> otherAnchors = be.getOtherAnchors();
            if (otherAnchors.size() == 2) {
                BlockPos pos3 = null;

                for (BlockPos otherAnchor : otherAnchors) {
                    if (!otherAnchor.equals(other)) {
                        pos3 = otherAnchor;
                        break;
                    }
                }

                if (pos3 != null) {
                    Vec2 vPos = new Vec2(pos.getX(), pos.getZ());
                    Vec2 vOther = new Vec2(other.getX(), other.getZ());
                    Vec2 vPos3 = new Vec2(pos3.getX(), pos3.getZ());
                    Vec2 forward = vOther.sub(vPos).add(vOther.sub(vPos3)).getNormalized();
                    Vec2 right = new Vec2(-forward.y, forward.x);

                    if (right.dot(vOther.sub(vPos)) > 0) {
                        result = results.getSecond();
                    }
                }
            }
        }

        return result;
    }

    private static Pair<Vec3, Vec3> calculateWireTouchPoint3d(float wheelRadius, Vec3 wheelCenter, Vec3 wireStart) {
        Vec2 wheelCenterProj = new Vec2(wheelCenter.x, wheelCenter.z);
        Vec2 wireStartProj = new Vec2(wireStart.x, wireStart.z);
        return calculateWireTouchPoint(wheelRadius, wheelCenterProj, wireStartProj)
            .mapFirst(vec2 -> new Vec3(vec2.x, wheelCenter.y, vec2.y))
            .mapSecond(vec2 -> new Vec3(vec2.x, wheelCenter.y, vec2.y));
    }

    private static Pair<Vec2, Vec2> calculateWireTouchPoint(float wheelRadius, Vec2 wheelCenter, Vec2 wireStart) {
        Vec2 center = wireStart.add(wheelCenter).div(2);
        Vec2 sub = center.sub(wheelCenter);
        float distSq = sub.getLengthSq();
        float dist = sub.getLength();
        float dist1 = (wheelRadius * wheelRadius) / (2 * dist);
        float skew = MathHelper.sqrt(wheelRadius * wheelRadius - dist1 * dist1);

        Vec2 dir = sub.getNormalized();
        Vec2 right = new Vec2(-dir.y, dir.x);

        Vec2 result1 = wheelCenter.add(dir.mul(dist1)).add(right.mul(skew));
        Vec2 result2 = wheelCenter.add(dir.mul(dist1)).add(right.mul(-skew));
        return Pair.of(result1, result2);
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (type == Trucc.getInstance().blockEntityTypes.motorWheel) {
            return (world1, pos, state1, blockEntity) -> this.tick(world1, pos, state1, (MotorWheelBlockEntity) blockEntity);
        } else {
            return null;
        }
    }

    private void tick(World world, BlockPos blockPos, BlockState blockState, MotorWheelBlockEntity be) {
        be.tickFind();
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
