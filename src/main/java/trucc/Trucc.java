package trucc;

import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.Objects;

import trucc.client.RoadCameraHandler;
import trucc.init.BlockEntityTypes;
import trucc.init.Blocks;
import trucc.init.EntityTypes;
import trucc.init.Items;
import trucc.world.CableTracker;

public class Trucc {
    public static final String MOD_ID = "trucc";

    private static Trucc INSTANCE;

    public final ItemGroup itemGroup = FabricItemGroupBuilder.build(new Identifier(MOD_ID, "all"), () -> this.items.steelCable.getDefaultStack());
    public final Blocks blocks = new Blocks();
    public final Items items = new Items(this.itemGroup, this.blocks);
    public final BlockEntityTypes blockEntityTypes = new BlockEntityTypes(this.blocks);
    public final EntityTypes entityTypes = new EntityTypes();

    public static void initialize() {
        Trucc instance = new Trucc();

        instance.blocks.register();
        instance.items.register();
        instance.blockEntityTypes.register();
        instance.entityTypes.register();

        INSTANCE = instance;

        ServerTickEvents.END_WORLD_TICK.register(world -> CableTracker.get(world).tick());
        ClientTickEvents.END_WORLD_TICK.register(world -> CableTracker.get(world).tick());
        ClientTickEvents.END_WORLD_TICK.register(world -> RoadCameraHandler.tick());
    }

    public static Trucc getInstance() {
        return Objects.requireNonNull(INSTANCE);
    }
}
