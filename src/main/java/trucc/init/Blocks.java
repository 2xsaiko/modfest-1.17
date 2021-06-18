package trucc.init;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import trucc.block.ZiplineBlock;

import static trucc.Trucc.MOD_ID;

public class Blocks {
    public final ZiplineBlock zipline = new ZiplineBlock(AbstractBlock.Settings.of(Material.METAL, MapColor.GRAY));

    public void register() {
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "zipline"), this.zipline);
    }
}
