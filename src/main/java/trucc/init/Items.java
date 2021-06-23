package trucc.init;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import trucc.item.GlovesItem;

import static trucc.Trucc.MOD_ID;

public class Items {
    public final BlockItem ziplineAnchor;
    public final Item steelCable;
    public final Item gloves;

    public Items(ItemGroup group, Blocks blocks) {
        Settings settings = new Settings().group(group);

        this.ziplineAnchor = new BlockItem(blocks.ziplineAnchor, settings);
        this.steelCable = new Item(settings);
        this.gloves = new GlovesItem(settings);
    }

    public void register() {
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "zipline_anchor"), this.ziplineAnchor);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "steel_cable"), this.steelCable);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gloves"), this.gloves);
    }
}
