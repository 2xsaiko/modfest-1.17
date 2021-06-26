package trucc.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import trucc.client.RoadCameraHandler;
import trucc.client.render.TransformationManager;
import trucc.item.GlovesItem;

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
}
