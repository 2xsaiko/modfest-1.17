package trucc.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

public record CableTravelerDismountPacket(int teleportId, Vec3d xyz, Vec3d velocity) {
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.teleportId);
        buf.writeDouble(this.xyz.x);
        buf.writeDouble(this.xyz.y);
        buf.writeDouble(this.xyz.z);
        buf.writeDouble(this.velocity.x);
        buf.writeDouble(this.velocity.y);
        buf.writeDouble(this.velocity.z);
    }

    public static CableTravelerDismountPacket read(PacketByteBuf buf) {
        int teleportId = buf.readVarInt();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        double vx = buf.readDouble();
        double vy = buf.readDouble();
        double vz = buf.readDouble();
        return new CableTravelerDismountPacket(teleportId, new Vec3d(x, y, z), new Vec3d(vx, vy, vz));
    }
}
