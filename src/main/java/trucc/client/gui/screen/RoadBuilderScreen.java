package trucc.client.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

import trucc.client.RoadCameraHandler;
import trucc.client.TruccClient;
import trucc.client.init.KeyBindings;
import trucc.entity.RoadCameraEntity;
import trucc.util.SelectUtil.DeprojectResult;

public class RoadBuilderScreen extends Screen {
    private final TruccClient tc = TruccClient.getInstance();

    private boolean sneakPressed;
    private boolean forwardPressed;
    private boolean backPressed;
    private boolean leftPressed;
    private boolean rightPressed;

    private BlockPos hitBlock;

    public RoadBuilderScreen() {
        super(new TranslatableText("trucc.gui.road_builder"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        this.updateMouseOverBlock(mouseX, mouseY);

        if (this.hitBlock != null) {
            this.textRenderer.draw(matrices, "%d, %d, %d".formatted(this.hitBlock.getX(), this.hitBlock.getY(), this.hitBlock.getZ()), 2, 2, -1);
        }
    }

    private void updateMouseOverBlock(int mouseX, int mouseY) {
        MinecraftClient client = this.client;

        if (client == null) {
            return;
        }

        assert client.world != null;

        DeprojectResult deprojectResult = this.tc.su.cursorToWorld(mouseX, mouseY);
        RaycastContext ctx = new RaycastContext(deprojectResult.start(), deprojectResult.end(), ShapeType.OUTLINE, FluidHandling.NONE, client.player);
        BlockHitResult r = client.world.raycast(ctx);

        if (r.getType() == Type.BLOCK) {
            this.hitBlock = r.getBlockPos();
        } else {
            this.hitBlock = null;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        RoadCameraEntity cameraEntity = RoadCameraHandler.cameraEntity;
        MinecraftClient client = this.client;
        KeyBindings keyBindings = this.tc.keyBindings;

        if (cameraEntity == null || client == null) {
            return false;
        }

        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyBindings.toggleCamera.matchesKey(keyCode, scanCode)) {
            this.onClose();
            return true;
        } else if (keyBindings.rotateCameraLeft.matchesKey(keyCode, scanCode)) {
            if (cameraEntity.getYaw() > 45) {
                cameraEntity.setYaw(cameraEntity.getYaw() - 90);
            } else {
                cameraEntity.setYaw(315);
            }

            return true;
        } else if (keyBindings.rotateCameraRight.matchesKey(keyCode, scanCode)) {
            if (cameraEntity.getYaw() < 315) {
                cameraEntity.setYaw(cameraEntity.getYaw() + 90);
            } else {
                cameraEntity.setYaw(45);
            }

            return true;
        } else {
            return this.keyEvent(keyCode, scanCode, true);
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (super.keyReleased(keyCode, scanCode, modifiers)) {
            return true;
        } else {
            return this.keyEvent(keyCode, scanCode, false);
        }
    }

    private boolean keyEvent(int keyCode, int scanCode, boolean pressed) {
        MinecraftClient client = this.client;

        if (client == null) {
            return false;
        }

        if (client.options.keySneak.matchesKey(keyCode, scanCode)) {
            this.sneakPressed = pressed;
        } else if (client.options.keyForward.matchesKey(keyCode, scanCode)) {
            this.forwardPressed = pressed;
        } else if (client.options.keyBack.matchesKey(keyCode, scanCode)) {
            this.backPressed = pressed;
        } else if (client.options.keyLeft.matchesKey(keyCode, scanCode)) {
            this.leftPressed = pressed;
        } else if (client.options.keyRight.matchesKey(keyCode, scanCode)) {
            this.rightPressed = pressed;
        } else {
            return false;
        }

        return true;
    }

    private void updateEntitySpeed() {
        MinecraftClient client = this.client;
        RoadCameraEntity cameraEntity = RoadCameraHandler.cameraEntity;

        if (client == null || cameraEntity == null) {
            return;
        }

        float speed = RoadCameraHandler.speed;

        if (this.leftPressed) {
            speed = 2;
            cameraEntity.sidewaysSpeed = 1;
        } else if (this.rightPressed) {
            speed = 2;
            cameraEntity.sidewaysSpeed = -1;
        }

        if (this.forwardPressed) {
            if (!this.rightPressed && !this.leftPressed)
                speed = 3.3F;
            cameraEntity.forwardSpeed = 1;
        } else if (this.backPressed) {
            if (!this.rightPressed && !this.leftPressed)
                speed = 3.3F;
            cameraEntity.forwardSpeed = -1;
        }

        if (this.sneakPressed) {
            speed *= 2;
        }

        RoadCameraHandler.speed = speed;
    }

    public void worldTick() {
        this.updateEntitySpeed();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (super.mouseScrolled(mouseX, mouseY, amount)) {
            return true;
        }

        MinecraftClient client = this.client;

        if (client == null) {
            return false;
        }

        if (amount == 1) {
            RoadCameraHandler.zoom -= this.sneakPressed ? 10 : 5;
        } else if (amount == -1) {
            RoadCameraHandler.zoom += this.sneakPressed ? 10 : 5;
        }

        RoadCameraHandler.zoom = Math.min(Math.max(RoadCameraHandler.zoom, RoadCameraHandler.maxZoom), RoadCameraHandler.minZoom);
        return true;
    }

    @Override
    protected void init() {
        RoadCameraHandler.startCamera();
        super.init();
    }

    @Override
    public void removed() {
        RoadCameraHandler.stopCamera();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
