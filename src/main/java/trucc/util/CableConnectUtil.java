package trucc.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.WeakHashMap;

import trucc.Trucc;
import trucc.block.entity.ZiplineHolderBlockEntity;

public class CableConnectUtil {
    private static final WeakHashMap<PlayerEntity, BlockPos> queuedLinkActions = new WeakHashMap<>();

    public static boolean onConnectUse(World world, BlockPos pos, PlayerEntity player, Hand hand) {
        if (player.getStackInHand(hand).getItem() != Trucc.getInstance().items.steelCable) {
            return false;
        }

        if (world.isClient()) {
            return true;
        }

        BlockPos first = queuedLinkActions.remove(player);

        if (first == null) {
            queuedLinkActions.put(player, pos);
            player.sendMessage(new TranslatableText("trucc.link.start"), true);
            return true;
        } else {
            BlockEntity be = world.getBlockEntity(pos);

            if (be instanceof ZiplineHolderBlockEntity anchor) {
                if (anchor.link(first, player)) {
                    player.sendMessage(new TranslatableText("trucc.link.success"), true);
                } else {
                    player.sendMessage(new TranslatableText("trucc.link.failure"), true);
                }
            } else {
                player.sendMessage(new TranslatableText("trucc.link.failure"), true);
            }

            return true;
        }
    }
}
