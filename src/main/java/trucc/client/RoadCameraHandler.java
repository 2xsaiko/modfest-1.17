package trucc.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.text.LiteralText;

import trucc.client.gui.screen.RoadBuilderScreen;
import trucc.client.render.TransformationManager;
import trucc.entity.RoadCameraEntity;

public class RoadCameraHandler {
    public static float speed = 2;
    public static int zoom = 50;
    public static int minZoom = 70;
    public static int maxZoom = 30;
    private static Perspective prevPerspective = null;
    public static RoadCameraEntity cameraEntity = null;
    private static MinecraftClient client = MinecraftClient.getInstance();

    public static void startCamera() {
        if (cameraEntity == null) {
            RoadCameraEntity entity = new RoadCameraEntity(client.world, client.player);
            MinecraftClient.getInstance().setCameraEntity(entity);
            TransformationManager.RemoteCallables.enableIsometricView(50);
            cameraEntity = entity;

            prevPerspective = client.options.getPerspective();
            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            client.world.spawnEntity(entity);
        }
    }

    public static void stopCamera() {
        if (cameraEntity != null) {
            TransformationManager.RemoteCallables.disableIsometricView();
            MinecraftClient.getInstance().setCameraEntity(client.player);
            cameraEntity.kill();
            cameraEntity = null;

            client.options.setPerspective(prevPerspective);
            prevPerspective = null;
        }
    }

    public static void tick() {
        while (TruccClient.getInstance().keyBindings.toggleCamera.wasPressed()) {
            client.player.sendMessage(new LiteralText("Toggle Camera was pressed!"), false);
            client.openScreen(new RoadBuilderScreen());
        }

        if (client.currentScreen instanceof RoadBuilderScreen s) {
            s.worldTick();
        }

        if (cameraEntity != null) {
            cameraEntity.tick();
        }
    }
}
