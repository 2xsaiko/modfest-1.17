package trucc.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import trucc.entity.CableTravelerEntity;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @SuppressWarnings("ConstantConditions")
    @Inject(
        method = "onDismounted(Lnet/minecraft/entity/Entity;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;requestTeleportAndDismount(DDD)V"),
        cancellable = true
    )
    private void onDismount(Entity vehicle, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity && vehicle instanceof CableTravelerEntity) {
            ci.cancel();
        }
    }
}
