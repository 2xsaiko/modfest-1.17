package trucc.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;

import trucc.entity.CableTravelerEntity;
import trucc.entity.RoadCameraEntity;
import trucc.entity.TruckEntity;

import static trucc.Trucc.MOD_ID;

public class EntityTypes {
    public final EntityType<RoadCameraEntity> roadCameraEntity = FabricEntityTypeBuilder.<RoadCameraEntity>create().entityFactory(RoadCameraEntity::new).build();
    public final EntityType<TruckEntity> truck = FabricEntityTypeBuilder.create().entityFactory(TruckEntity::new).build();
    public final EntityType<CableTravelerEntity> cableTraveler = FabricEntityTypeBuilder.create().entityFactory(CableTravelerEntity::new).dimensions(EntityDimensions.fixed(0.1f, 0.1f)).build();

    public void register() {
        Registry.register(Registry.ENTITY_TYPE, new Identifier(MOD_ID, "truck"), this.truck);
        Registry.register(Registry.ENTITY_TYPE, new Identifier(MOD_ID, "cable_traveler"), this.cableTraveler);
    }
}
