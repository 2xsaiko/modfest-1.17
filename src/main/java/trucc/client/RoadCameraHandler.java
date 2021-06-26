package trucc.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.text.LiteralText;
import trucc.client.render.TransformationManager;
import trucc.entity.RoadCameraEntity;

public class RoadCameraHandler {
    public static boolean isCamera = false;
    public static float speed = 2;
    public static int zoom = 50;
    public static int minZoom = 70;
    public static int maxZoom = 30;
    public static double scrollDelta = 0;
    private static Perspective prevPerspective = null;
    private static RoadCameraEntity cameraEntity = null;
    private static MinecraftClient client = MinecraftClient.getInstance();

    public static void startCamera() {
        if (!isCamera) {
            RoadCameraEntity entity = new RoadCameraEntity(client.world, client.player);
            MinecraftClient.getInstance().setCameraEntity(entity);
            TransformationManager.RemoteCallables.enableIsometricView(50);
            isCamera = true;
            cameraEntity = entity;

            prevPerspective = client.options.getPerspective();
            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            client.world.spawnEntity(entity);

            client.mouse.unlockCursor();
        }
    }

    public static void stopCamera() {
        if (isCamera) {
            TransformationManager.RemoteCallables.disableIsometricView();
            MinecraftClient.getInstance().setCameraEntity(client.player);
            cameraEntity.kill();
            isCamera = false;
            cameraEntity = null;

            client.options.setPerspective(prevPerspective);
            prevPerspective = null;

            client.mouse.lockCursor();
        }
    }

    public static void tick() {
        while (TruccClient.getInstance().keyBindings.toggleCamera.wasPressed()) {
            client.player.sendMessage(new LiteralText("Toggle Camera was pressed!"), false);
            if (!isCamera) {
                startCamera();
            } else {
                stopCamera();
            }
        }

        if (isCamera) {
            if (scrollDelta == 1) {
                zoom -= client.options.keySneak.isPressed() ? 10 : 5;
            } else if (scrollDelta == -1) {
                zoom += client.options.keySneak.isPressed() ? 10 : 5;
            }
            zoom = Math.min(Math.max(zoom, maxZoom), minZoom);

            if (client.options.keyLeft.isPressed()) {
                speed = 2;
                cameraEntity.sidewaysSpeed = 1;
            } else if (client.options.keyRight.isPressed()) {
                speed = 2;
                cameraEntity.sidewaysSpeed = -1;
            }

            if (client.options.keyForward.isPressed()) {
                if (!client.options.keyRight.isPressed() && !client.options.keyLeft.isPressed())
                    speed = 3.3F;
                cameraEntity.forwardSpeed = 1;
            } else if (client.options.keyBack.isPressed()) {
                if (!client.options.keyRight.isPressed() && !client.options.keyLeft.isPressed())
                    speed = 3.3F;
                cameraEntity.forwardSpeed = -1;
            }

            if (client.options.keySneak.isPressed()) {
                speed *= 2;
            }

            cameraEntity.tick();
        }
        scrollDelta = 0;
    }
}
