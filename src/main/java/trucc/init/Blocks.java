package trucc.init;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import trucc.block.MotorWheelBlock;
import trucc.block.ZiplineAnchorBlock;

import static trucc.Trucc.MOD_ID;

public class Blocks {
    public final ZiplineAnchorBlock ziplineAnchor = new ZiplineAnchorBlock(AbstractBlock.Settings.of(Material.METAL, MapColor.GRAY));
    public final MotorWheelBlock motorWheel = new MotorWheelBlock(AbstractBlock.Settings.of(Material.METAL, MapColor.GRAY));

    public void register() {
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "zipline_anchor"), this.ziplineAnchor);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "motor_wheel"), this.motorWheel);
    }
}
