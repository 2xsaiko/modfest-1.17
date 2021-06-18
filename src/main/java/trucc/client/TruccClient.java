package trucc.client;

import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

import trucc.Trucc;
import trucc.client.render.entity.TruckRenderer;

public class TruccClient {
    public static void initialize() {
        Trucc trucc = Trucc.getInstance();

        EntityRendererRegistry.INSTANCE.register(trucc.entityTypes.truck, TruckRenderer::new);
    }
}
