package trucc.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;
import trucc.block.entity.MotorWheelBlockEntity;

public class MotorWheelBlock extends Block implements BlockEntityProvider {
    public MotorWheelBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MotorWheelBlockEntity(pos, state);
    }
}
