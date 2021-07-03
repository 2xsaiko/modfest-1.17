package trucc.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import trucc.Trucc;
import trucc.client.init.KeyBindings;
import trucc.client.render.CableRenderer;
import trucc.client.render.entity.CableTravelerRenderer;
import trucc.client.render.entity.TruckRenderer;
import trucc.network.CableTravelerDismountPacket;
import trucc.network.ClientNetworkHandler;
import trucc.util.ClientNetworkUtil;
import trucc.util.SelectUtil;
import trucc.world.CableTracker;

import java.util.Objects;

import static trucc.Trucc.id;

public class TruccClient {
    public final KeyBindings keyBindings = new KeyBindings();

    private static TruccClient INSTANCE;

    public final SelectUtil su = new SelectUtil();
    public final ClientNetworkHandler networkHandler = new ClientNetworkHandler();

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

        ClientPlayNetworking.registerGlobalReceiver(id("dismount"), ClientNetworkUtil.map(tc.networkHandler::handleCableTravelerDismount, CableTravelerDismountPacket::read));

        ClientTickEvents.END_WORLD_TICK.register(world -> {
            CableTracker.get(world).tick();
            RoadCameraHandler.tick();
        });

        INSTANCE = tc;
    }

    public static TruccClient getInstance() {
        return Objects.requireNonNull(INSTANCE);
    }
}
