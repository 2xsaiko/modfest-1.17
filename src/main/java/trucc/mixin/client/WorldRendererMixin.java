package trucc.mixin.client;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import trucc.client.render.TransformationManager;

/**
 * Adapted from Immersive Portals
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(
        method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FDDD)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void renderClouds(
            MatrixStack matrices, Matrix4f matrix4f, float f, double d, double e, double g, CallbackInfo cir
    ) {
        if (TransformationManager.isIsometricView) {
            cir.cancel();
        }
    }
}