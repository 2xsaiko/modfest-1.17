package trucc.mixin.client;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import trucc.client.RoadCameraHandler;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Redirect(
        method = "onMouseButton",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;lockCursor()V")
    )
    private void onMouseButton(Mouse mouse) {
        if (!RoadCameraHandler.isCamera) {
            mouse.lockCursor();
        }
    }

    @Inject(
            method = "onMouseScroll",
            at = @At("HEAD"),
            cancellable = true
    )
    private void scrollInHotbar(
            long window, double horizontal, double vertical, CallbackInfo ci
    ) {
        if (RoadCameraHandler.isCamera) {
            RoadCameraHandler.scrollDelta = vertical;
        }
    }
}
