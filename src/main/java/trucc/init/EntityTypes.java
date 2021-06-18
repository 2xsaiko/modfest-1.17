package trucc.init;

import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;

import trucc.entity.TruckEntity;

import static trucc.Trucc.MOD_ID;

public class EntityTypes {
    public final EntityType<TruckEntity> truck = FabricEntityTypeBuilder.create().entityFactory(TruckEntity::new).build();

    public void register() {
        Registry.register(Registry.ENTITY_TYPE, new Identifier(MOD_ID, "truck"), this.truck);
    }
}
