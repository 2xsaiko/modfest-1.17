package trucc.init;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

import trucc.block.entity.MotorWheelBlockEntity;
import trucc.block.entity.ZiplineAnchorBlockEntity;

import static trucc.Trucc.MOD_ID;

public class BlockEntityTypes {
    public final BlockEntityType<ZiplineAnchorBlockEntity> ziplineAnchor;
    public final BlockEntityType<MotorWheelBlockEntity> motorWheel;

    public BlockEntityTypes(Blocks blocks) {
        this.ziplineAnchor = FabricBlockEntityTypeBuilder.create(ZiplineAnchorBlockEntity::new, blocks.ziplineAnchor).build();
        this.motorWheel = FabricBlockEntityTypeBuilder.create(MotorWheelBlockEntity::new, blocks.motorWheel).build();
    }

    public void register() {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "zipline_anchor"), this.ziplineAnchor);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "motor_wheel"), this.motorWheel);
    }
}
