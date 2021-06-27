package trucc.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import trucc.Trucc;
import trucc.client.init.KeyBindings;
import trucc.client.render.CableRenderer;
import trucc.client.render.entity.CableTravelerRenderer;
import trucc.client.render.entity.TruckRenderer;
import trucc.util.SelectUtil;

import java.util.Objects;

public class TruccClient {
    public final KeyBindings keyBindings = new KeyBindings();

    private static TruccClient INSTANCE;

    public final SelectUtil su = new SelectUtil();

    public static void initialize() {
        Trucc trucc = Trucc.getInstance();
        TruccClient tc = new TruccClient();

        EntityRendererRegistry.INSTANCE.register(trucc.entityTypes.truck, TruckRenderer::new);
        EntityRendererRegistry.INSTANCE.register(trucc.entityTypes.cableTraveler, CableTravelerRenderer::new);

        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            tc.su.saveWorldMatrices(ctx.projectionMatrix(), ctx.matrixStack().peek().getModel(), ctx.camera().getPos());
            CableRenderer.get(ctx.world()).render(ctx);
        });
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            tc.su.saveGuiMatrices(RenderSystem.getProjectionMatrix(), matrixStack.peek().getModel());
        });

        INSTANCE = tc;
    }

    public static TruccClient getInstance() {
        return Objects.requireNonNull(INSTANCE);
    }
}
