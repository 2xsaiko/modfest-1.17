package trucc.mixin;

import net.minecraft.client.network.ClientPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import trucc.item.GlovesItem;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Redirect(
        method = "tickMovement()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z")
    )
    private boolean isUsingItem(ClientPlayerEntity clientPlayerEntity) {
        // don't slow down for gloves
        return clientPlayerEntity.isUsingItem() && !(clientPlayerEntity.getActiveItem().getItem() instanceof GlovesItem);
    }
}
