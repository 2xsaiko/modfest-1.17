package trucc.client;

import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import trucc.Trucc;
import trucc.client.render.CableRenderer;
import trucc.client.render.entity.TruckRenderer;

public class TruccClient {
    private final CableRenderer cableRenderer = new CableRenderer();

    public static void initialize() {
        Trucc trucc = Trucc.getInstance();

        EntityRendererRegistry.INSTANCE.register(trucc.entityTypes.truck, TruckRenderer::new);
        WorldRenderEvents.BEFORE_ENTITIES.register(ctx -> {
            CableRenderer.get(ctx.world()).render(ctx);
        });
    }
}
