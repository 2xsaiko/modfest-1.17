package trucc.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

import trucc.Trucc;

public class MotorWheelBlockEntity extends BlockEntity {
    public MotorWheelBlockEntity(BlockPos pos, BlockState state) {
        super(Trucc.getInstance().blockEntityTypes.motorWheel, pos, state);
    }
}
