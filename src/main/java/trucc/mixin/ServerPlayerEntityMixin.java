package trucc.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import trucc.entity.CableTravelerEntity;
import trucc.ext.ServerPlayNetworkHandlerExt;
import trucc.network.CableTravelerDismountPacket;
import trucc.util.NetworkUtil;

import static trucc.Trucc.id;

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
            int teleportId = ServerPlayNetworkHandlerExt.from(this.networkHandler)
                .requestTeleportId(self.getPos());

            // TODO: make this not suck
            NetworkUtil.send(
                new CableTravelerDismountPacket(teleportId, self.getPos(), self.getVelocity()),
                self,
                id("dismount"),
                CableTravelerDismountPacket::write
            );
            ci.cancel();
        }
    }
}
