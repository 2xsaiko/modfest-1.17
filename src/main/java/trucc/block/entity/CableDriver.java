package trucc.block.entity;

import net.minecraft.util.math.BlockPos;

import java.util.Set;

public interface CableDriver {
    float getSpeed();

    default void notifyRemove(Set<BlockPos> set) {
    }
}
