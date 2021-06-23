package trucc.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
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
        if (!this.world.isClient() && (!this.hasPassengers() || this.p1 == null || this.p2 == null || segments.isEmpty())) {
            this.discard();
            return;
        }

        this.stopRiding();

        float baseGravity = -0.05f;
        this.move(MovementType.SELF, new Vec3d(0, baseGravity, 0));

        Vec3d pos = this.getPos();
        Vec3d velocity = this.getVelocity().add(0, baseGravity, 0);
        pos = pos.add(velocity);

        Vec3d start = null;
        Vec3d end = null;
        Vec3d last = null;

        for (Vec3 segment : segments) {
            Vec3d seg = segment.toVec3d();

            if (last != null) {
                double dot = last.subtract(pos).dotProduct(seg.subtract(pos));

                if (dot < 0) {
                    start = last;
                    end = seg;
                    break;
                }
            }

            last = seg;
        }

        if (start == null) {
            this.discard();
            return;
        }

        Vec3d relEnd = end.subtract(start).normalize();
        Vec3d relPos = pos.subtract(start);
        Vec3d posOnCable = relEnd.multiply(relEnd.dotProduct(relPos)).add(start);
        velocity = velocity.multiply(0.5).add(velocity.multiply(velocity.dotProduct(relEnd)).multiply(0.5));
        velocity.multiply(0.98);

        this.setPosition(pos);
        this.setVelocity(velocity);
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
