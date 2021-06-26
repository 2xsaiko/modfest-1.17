package trucc.client;

import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import trucc.Trucc;
import trucc.client.init.KeyBindings;
import trucc.client.render.CableRenderer;
import trucc.client.render.entity.CableTravelerRenderer;
import trucc.client.render.entity.TruckRenderer;

import java.util.Objects;

public class TruccClient {
    public final KeyBindings keyBindings = new KeyBindings();

    private static TruccClient INSTANCE;

    public static void initialize() {
        Trucc trucc = Trucc.getInstance();
        INSTANCE = new TruccClient();

        EntityRendererRegistry.INSTANCE.register(trucc.entityTypes.truck, TruckRenderer::new);
        EntityRendererRegistry.INSTANCE.register(trucc.entityTypes.cableTraveler, CableTravelerRenderer::new);
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            CableRenderer.get(ctx.world()).render(ctx);
        });
    }

    public static TruccClient getInstance() {
        return Objects.requireNonNull(INSTANCE);
    }
}
