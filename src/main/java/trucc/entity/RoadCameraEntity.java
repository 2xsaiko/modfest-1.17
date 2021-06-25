package trucc.entity;

import net.dblsaiko.qcommon.croco.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import trucc.Trucc;
import trucc.world.CableTracker;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RoadCameraEntity extends Entity {

    public RoadCameraEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public RoadCameraEntity(World world, Entity baseEntity) {
        this(Trucc.getInstance().entityTypes.roadCameraEntity, world);

        setPosition(baseEntity.getX(), baseEntity.getY() + baseEntity.getEyeHeight(EntityPose.STANDING), baseEntity.getZ());
        prevX = baseEntity.getX();
        prevY = baseEntity.getY() + baseEntity.getEyeHeight(EntityPose.STANDING);
        prevZ = baseEntity.getZ();

        setYaw(45);
        setPitch(33);
        prevYaw = 45;
        prevPitch = 33;
    }

    @Override
    public void tick() {
        this.attemptTickInVoid();
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
