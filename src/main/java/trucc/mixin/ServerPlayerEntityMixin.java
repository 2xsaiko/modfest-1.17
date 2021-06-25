package trucc.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket.Flag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import trucc.entity.CableTravelerEntity;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Shadow public ServerPlayNetworkHandler networkHandler;

    @Inject(
        method = "stopRiding()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;requestTeleportAndDismount(DDDFF)V"),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void handleDismountTeleport(CallbackInfo ci, Entity entity, Entity entity2) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        if (entity instanceof CableTravelerEntity) {
            this.networkHandler.requestTeleport(self.getX(), self.getY(), self.getZ(), self.getYaw(), self.getPitch(), Flag.getFlags(-1), true);
            ci.cancel();
        }
    }
}
