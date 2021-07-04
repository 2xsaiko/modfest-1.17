package trucc.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import trucc.Trucc;

public class ZiplineAnchorBlockEntity extends ZiplineHolderBlockEntity {
    public ZiplineAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(Trucc.getInstance().blockEntityTypes.ziplineAnchor, pos, state);
    }
}
