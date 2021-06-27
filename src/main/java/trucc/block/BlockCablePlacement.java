package trucc.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.dblsaiko.qcommon.croco.Vec3;

public interface BlockCablePlacement {
    @Nullable
    Vec3 getWireOffset(World world, BlockPos pos, BlockState state, BlockPos other);

    default Vec3 getDependentWireOffset(World world, BlockPos pos, BlockState state, BlockPos other, Vec3 otherOffset) {
        return Objects.requireNonNull(this.getWireOffset(world, pos, state, other));
    }
}
