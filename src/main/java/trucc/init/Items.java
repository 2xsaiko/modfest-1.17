package trucc.init;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static trucc.Trucc.MOD_ID;

public class Items {
    public final BlockItem zipline;

    public Items(Blocks blocks) {
        this.zipline = new BlockItem(blocks.zipline, new Item.Settings());
    }

    public void register() {
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "zipline"), this.zipline);
    }
}
