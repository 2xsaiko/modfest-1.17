package trucc.mixin.client;

import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import trucc.client.RoadCameraHandler;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Inject(
            method = "scrollInHotbar",
            at = @At("HEAD"),
            cancellable = true
    )
    private void scrollInHotbar(
            double scrollAmount,
            CallbackInfo ci
    ) {
        if (RoadCameraHandler.isCamera) {
            ci.cancel();
        }
    }
}
