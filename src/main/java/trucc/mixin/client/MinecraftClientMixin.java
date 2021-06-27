package trucc.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import trucc.client.RoadCameraHandler;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(
            method = "doAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void doAttack(
            CallbackInfo ci
    ) {
        if (RoadCameraHandler.isCamera) {
            ci.cancel();
        }
    }

    @Inject(
            method = "doItemUse",
            at = @At("HEAD"),
            cancellable = true
    )
    private void doUse(
            CallbackInfo ci
    ) {
        if (RoadCameraHandler.isCamera) {
            ci.cancel();
        }
    }

    @Inject(
            method = "doItemPick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void doItemPick(
            CallbackInfo ci
    ) {
        if (RoadCameraHandler.isCamera) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "openScreen(Lnet/minecraft/client/gui/screen/Screen;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;lockCursor()V")
    )
    private void fixLockCursor(Mouse mouse) {
        if (RoadCameraHandler.isCamera) {
            mouse.unlockCursor();
        } else {
            mouse.lockCursor();
        }
    }
}
