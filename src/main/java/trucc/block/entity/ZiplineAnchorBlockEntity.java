package trucc.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import trucc.Trucc;
import trucc.client.render.CableRenderer;

public class ZiplineAnchorBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    // too lazy to hand serialize these lmao
    private static final Codec<Set<BlockPos>> OTHER_ANCHORS_CODEC = Codec.list(BlockPos.CODEC).xmap(HashSet::new, ArrayList::new);

    private Set<BlockPos> otherAnchors = new HashSet<>();

    public ZiplineAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(Trucc.getInstance().blockEntityTypes.ziplineAnchor, pos, state);
    }

    public boolean link(BlockPos pos, @Nullable PlayerEntity actor) {
        Trucc trucc = Trucc.getInstance();
        World world = this.world;

        if (world == null) {
            return false;
        }

        if (this.otherAnchors.contains(pos)) {
            return true;
        }

        if (!world.getBlockState(pos).isOf(trucc.blocks.ziplineAnchor)) {
            return false;
        }

        // TODO: collide check

        return this.doLink(pos);
    }

    private boolean doLink(BlockPos pos) {
        World world = Objects.requireNonNull(this.world);

        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (!(blockEntity instanceof ZiplineAnchorBlockEntity anchor)) {
            return false;
        }

        this.otherAnchors.add(pos);
        anchor.otherAnchors.add(this.pos);
        this.markDirty();
        this.sync();
        anchor.markDirty();
        anchor.sync();
        return true;
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        World world = this.world;

        if (world != null && this.world.isClient() && this.world instanceof ClientWorld cw) {
            CableRenderer cr = CableRenderer.get(cw);
            cr.removeAll(this.pos);
        }
    }

    @Override
    public void cancelRemoval() {
        super.cancelRemoval();
        World world = this.world;

        if (world != null && this.world.isClient() && this.world instanceof ClientWorld cw) {
            CableRenderer cr = CableRenderer.get(cw);

            for (BlockPos other : this.otherAnchors) {
                cr.add(this.pos, other);
            }
        }
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
        nbt.put("other_anchors", OTHER_ANCHORS_CODEC.encode(this.otherAnchors, NbtOps.INSTANCE, new NbtList()).getOrThrow(false, s -> {
        }));

        return super.writeNbt(nbt);
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        Set<BlockPos> newAnchors = OTHER_ANCHORS_CODEC
            .decode(NbtOps.INSTANCE, tag.get("other_anchors"))
            .result()
            .map(Pair::getFirst)
            .orElseGet(HashSet::new);

        World world = this.world;

        if (world != null && this.world.isClient() && this.world instanceof ClientWorld cw) {
            CableRenderer cr = CableRenderer.get(cw);

            newAnchors.stream().filter(el -> !this.otherAnchors.contains(el))
                .forEach(el -> cr.add(this.pos, el));
            this.otherAnchors.stream().filter(el -> !newAnchors.contains(el))
                .forEach(el -> cr.remove(this.pos, el));
        }

        this.otherAnchors = newAnchors;
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        tag.put("other_anchors", OTHER_ANCHORS_CODEC.encode(this.otherAnchors, NbtOps.INSTANCE, new NbtList()).getOrThrow(false, s -> {
        }));

        return tag;
    }
}
