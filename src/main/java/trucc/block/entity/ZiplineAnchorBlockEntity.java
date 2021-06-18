package trucc.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import trucc.Trucc;

public class ZiplineAnchorBlockEntity extends BlockEntity {
    // too lazy to hand serialize these lmao
    private static final Codec<Set<BlockPos>> OTHER_ANCHORS_CODEC = Codec.list(BlockPos.CODEC).xmap(HashSet::new, ArrayList::new);

    // Each zipline anchor can be connected to two other ziplines
    private Set<BlockPos> otherAnchors = new HashSet<>();

    public ZiplineAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(Trucc.getInstance().blockEntityTypes.ziplineAnchor, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.otherAnchors = OTHER_ANCHORS_CODEC
            .decode(NbtOps.INSTANCE, nbt.getCompound("other_anchors"))
            .result()
            .map(Pair::getFirst)
            .orElseGet(HashSet::new);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("other_anchors", OTHER_ANCHORS_CODEC.encode(this.otherAnchors, NbtOps.INSTANCE, new NbtList()).getOrThrow(false, s -> {}));

        return super.writeNbt(nbt);
    }
}
