package trucc.mixin;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import trucc.entity.CableTravelerEntity;
import trucc.ext.ServerPlayNetworkHandlerExt;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin implements ServerPlayNetworkHandlerExt {
    @Shadow public ServerPlayerEntity player;

    @Shadow @Nullable private Vec3d requestedTeleportPos;

    @Shadow private int requestedTeleportId;

    @Inject(
        method = "onClientCommand(Lnet/minecraft/network/packet/c2s/play/ClientCommandC2SPacket;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/ClientCommandC2SPacket;getMode()Lnet/minecraft/network/packet/c2s/play/ClientCommandC2SPacket$Mode;"),
        cancellable = true
    )
    private void onClientCommand(ClientCommandC2SPacket packet, CallbackInfo ci) {
        if (packet.getMode() == Mode.START_RIDING_JUMP) {
            if (this.player.getVehicle() instanceof CableTravelerEntity e) {
                e.startJumping();
            }

            ci.cancel();
        }
    }

    @Override
    public int requestTeleportId(Vec3d pos) {
        this.requestedTeleportPos = pos;
        this.requestedTeleportId += 1;

        if (this.requestedTeleportId == Integer.MAX_VALUE) {
            this.requestedTeleportId = 0;
        }

        return this.requestedTeleportId;
    }
}
