package trucc.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import trucc.client.RoadCameraHandler;
import trucc.client.render.TransformationManager;
import trucc.item.GlovesItem;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public World world;

    @Shadow public abstract String getEntityName();

    @Inject(
            method = "changeLookDirection",
            at = @At("HEAD"),
            cancellable = true
    )
    private void changeLookDirection(
            double cursorDeltaX, double cursorDeltaY, CallbackInfo ci
    ) {
        if (world.isClient() && this.getEntityName().equals(MinecraftClient.getInstance().player.getEntityName()) && RoadCameraHandler.isCamera) {
            ci.cancel();
        }
    }
}
