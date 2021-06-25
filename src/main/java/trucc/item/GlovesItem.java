package trucc.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

import trucc.Trucc;
import trucc.entity.CableTravelerEntity;
import trucc.world.CableTracker;

public class GlovesItem extends Item {
    public GlovesItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient()) {
            return;
        }

        EntityType<CableTravelerEntity> cableTraveler = Trucc.getInstance().entityTypes.cableTraveler;
        Entity currentVehicle = user.getVehicle();

        if (currentVehicle == null || currentVehicle.getType() != cableTraveler) {
            if (!CableTracker.get(world).getCablesInBlock(new BlockPos(user.getEyePos())).isEmpty()) {
                CableTravelerEntity ent = Objects.requireNonNull(cableTraveler.create(world));
                ent.setPosition(user.getEyePos());
                world.spawnEntity(ent);
                ent.setVelocity(user.getVelocity());
                user.startRiding(ent);
            }
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }
}
