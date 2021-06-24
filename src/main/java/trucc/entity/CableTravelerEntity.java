package trucc.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import trucc.world.CableTracker;

import net.dblsaiko.qcommon.croco.Vec3;

public class CableTravelerEntity extends Entity {
    private final CableTracker ct;

    private BlockPos p1;
    private BlockPos p2;

    public CableTravelerEntity(EntityType<?> type, World world) {
        super(type, world);
        this.ct = CableTracker.get(world);
    }

    @Override
    public void tick() {
        this.attemptTickInVoid();

        if (this.p1 == null || this.p2 == null) {
            Collection<Set<BlockPos>> cablesInBlock = this.ct.getCablesInBlock(this.getBlockPos());
            Iterator<Set<BlockPos>> iterator = cablesInBlock.iterator();

            if (iterator.hasNext()) {
                Iterator<BlockPos> iterator1 = iterator.next().iterator();
                this.p1 = iterator1.next();
                this.p2 = iterator1.next();
            }
        }

        List<Vec3> segments = this.ct.getSegments(this.p1, this.p2);

        // having this only makes sense with a passenger
        if (!this.hasPassengers() || this.p1 == null || this.p2 == null || segments.isEmpty()) {
            if (!this.world.isClient()) {
                this.discard();
            }

            return;
        }

        Vec3d dir = segments.get(segments.size() - 1).sub(segments.get(0)).toVec3d();

        this.stopRiding();

        float baseGravity = -0.05f;

        Vec3d pos = this.getPos();
        Vec3d velocity = this.getVelocity().add(0, baseGravity, 0);
        pos = pos.add(velocity);

        Vec3d start = null;
        Vec3d end = null;
        Vec3d last = null;

        for (Vec3 segment : segments) {
            Vec3d seg = segment.toVec3d();

            if (last != null) {
                double s1 = last.subtract(pos).dotProduct(dir);
                double s2 = seg.subtract(pos).dotProduct(dir);

                if (Math.signum(s1) != Math.signum(s2)) {
                    start = last;
                    end = seg;
                    break;
                }
            }

            last = seg;
        }

        if (start == null) {
            if (!this.world.isClient()) {
                this.discard();
            }

            return;
        }

        Vec3d relEnd = end.subtract(start).normalize();
        Vec3d relPos = pos.subtract(start);
        Vec3d posOnCable = projectOntoLine(start, relEnd, pos);
        Vec3d right = relEnd.crossProduct(new Vec3d(0,1,0)).normalize();
        Vec3d up = right.crossProduct(relEnd);

        velocity = projectInto(velocity, relEnd).multiply(0.99);

        this.setPosition(posOnCable);
        this.setVelocity(velocity);
    }

    private static Vec3d projectOntoLine(Vec3d linePt, Vec3d lineDir, Vec3d pt) {
        return linePt.add(projectInto(pt.subtract(linePt), lineDir));
    }

    private static Vec3d projectInto(Vec3d d, Vec3d dir) {
        return dir.multiply(d.dotProduct(dir));
    }

    private static Vec3d reflect(Vec3d normal, Vec3d vec) {
        return vec.subtract(projectInto(vec, normal).multiply(2));
    }

    @Override
    public double getMountedHeightOffset() {
        Entity primaryPassenger = this.getPrimaryPassenger();

        if (primaryPassenger == null) {
            return super.getMountedHeightOffset();
        }

        return -primaryPassenger.getStandingEyeHeight() - this.getHeight();
    }

    @Nullable
    @Override
    public Entity getPrimaryPassenger() {
        return this.getFirstPassenger();
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("x1")) {
            this.p1 = new BlockPos(nbt.getInt("x1"), nbt.getInt("y1"), nbt.getInt("z1"));
            this.p2 = new BlockPos(nbt.getInt("x2"), nbt.getInt("y2"), nbt.getInt("z2"));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (this.p1 != null && this.p2 != null) {
            nbt.putInt("x1", this.p1.getX());
            nbt.putInt("y1", this.p1.getY());
            nbt.putInt("z1", this.p1.getZ());
            nbt.putInt("x2", this.p2.getX());
            nbt.putInt("y2", this.p2.getY());
            nbt.putInt("z2", this.p2.getZ());
        }
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
