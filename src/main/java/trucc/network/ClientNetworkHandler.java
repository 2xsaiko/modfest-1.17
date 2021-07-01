package trucc.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class ClientNetworkHandler {
    public void handleCableTravelerDismount(
        MinecraftClient client,
        ClientPlayNetworkHandler handler,
        CableTravelerDismountPacket packet,
        PacketSender responseSender
    ) {
        client.execute(() -> {
            ClientPlayerEntity player = client.player;
            assert player != null;

            Vec3d xyz = packet.xyz();
            Vec3d velocity = packet.velocity();

            player.dismountVehicle();
            player.updatePosition(xyz.x, xyz.y, xyz.z);
            player.setVelocity(velocity.x, velocity.y, velocity.z);

            responseSender.sendPacket(new TeleportConfirmC2SPacket(packet.teleportId()));
            responseSender.sendPacket(new PlayerMoveC2SPacket.Full(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYaw(),
                player.getPitch(),
                false
            ));
        });
    }
}
