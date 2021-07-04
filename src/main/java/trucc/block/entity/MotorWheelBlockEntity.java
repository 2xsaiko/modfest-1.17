package trucc.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import trucc.Trucc;
import trucc.client.render.WheelRenderer;
import trucc.world.CableTracker;

public class MotorWheelBlockEntity extends ZiplineHolderBlockEntity implements CableDriver {
    private float speed;
    private final List<BlockPos> loop = new ArrayList<>();
    private int findTimeout = 0;

    public MotorWheelBlockEntity(BlockPos pos, BlockState state) {
        super(Trucc.getInstance().blockEntityTypes.motorWheel, pos, state);
    }

    @Override
    public boolean link(BlockPos pos, @Nullable PlayerEntity actor) {
        if (this.getOtherAnchors().size() > 1) {
            return false;
        }

        if (super.link(pos, actor)) {
            CableTracker.get(this.world).updateSegmentsFrom(this.pos);
            return true;
        } else {
            return false;
        }
    }

    public void tickFind() {
        if (this.findTimeout > 0) {
            this.findTimeout -= 1;
            return;
        }

        if (!this.loop.isEmpty()) {
            return;
        }

        if (this.getOtherAnchors().size() != 2) {
            this.findTimeout = 20;
            return;
        }

        List<BlockPos> list = new ArrayList<>();
        list.add(this.pos);
        BlockPos prev = this.pos;
        BlockPos current = this.getOtherAnchors().iterator().next();
        Set<BlockPos> cset = Set.of(prev, current);
        CableTracker ct = CableTracker.get(this.world);
        int steps = 200;

        while (!current.equals(this.pos) && steps > 0) {
            list.add(current);
            Collection<Set<BlockPos>> cableEndsInBlock = ct.getCableEndsInBlock(current);

            if (cableEndsInBlock.size() != 2) {
                this.findTimeout = 20;
                return;
            }

            boolean foundCur = false;
            Set<BlockPos> newSet = null;

            for (Set<BlockPos> set : cableEndsInBlock) {
                if (set.equals(cset)) {
                    foundCur = true;
                } else {
                    newSet = set;
                }
            }

            if (!foundCur || newSet == null) {
                this.findTimeout = 20;
                return;
            }

            Iterator<BlockPos> iterator = newSet.iterator();
            BlockPos next = iterator.next();

            if (next.equals(current)) {
                next = iterator.next();
            }

            prev = current;
            current = next;
            cset = newSet;
            steps -= 1;
        }

        List<BlockPos> loop = ct.addLoop(list, this);

        if (loop != null) {
            this.loop.clear();
            this.loop.addAll(loop);
        }
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        this.markDirty();
    }

    @Override
    public void notifyRemove(Set<BlockPos> set) {
        this.loop.clear();
    }

    @Override
    public void markRemoved() {
        super.markRemoved();

        World world = this.getWorld();

        if (world != null && world.isClient() && world instanceof ClientWorld cw) {
            WheelRenderer.get(cw).remove(this.getPos());
        }
    }

    @Override
    public void cancelRemoval() {
        World world = this.getWorld();

        if (world != null && world.isClient() && world instanceof ClientWorld cw) {
            WheelRenderer.get(cw).add(this.getPos());
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.speed = nbt.getFloat("speed");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putFloat("speed", this.speed);
        return super.writeNbt(nbt);
    }
}
