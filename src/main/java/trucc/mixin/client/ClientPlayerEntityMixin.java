package trucc.mixin.client;

import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import trucc.item.GlovesItem;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Shadow public Input input;

    @Shadow @Final public ClientPlayNetworkHandler networkHandler;

    @Redirect(
        method = "tickMovement()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z")
    )
    private boolean isUsingItem(ClientPlayerEntity clientPlayerEntity) {
        // don't slow down for gloves
        return clientPlayerEntity.isUsingItem() && !(clientPlayerEntity.getActiveItem().getItem() instanceof GlovesItem);
    }

    @Inject(
        method = "tickMovement()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasJumpingMount()Z"),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void cableJump(CallbackInfo ci, boolean prevJump) {
        ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
        boolean jump = this.input.jumping;

        if (!prevJump && jump) {
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(self, Mode.START_RIDING_JUMP));
        }
    }
}
