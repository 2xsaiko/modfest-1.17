package trucc.client.init;

import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public final KeyBinding toggleCamera;
    public final KeyBinding rotateCameraLeft;
    public final KeyBinding rotateCameraRight;

    public KeyBindings() {
        toggleCamera = KeyBindingRegistryImpl.registerKeyBinding(new KeyBinding(
                "key.trucc.togglecamera",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.trucc.main"
        ));

        rotateCameraLeft = KeyBindingRegistryImpl.registerKeyBinding(new KeyBinding(
                "key.trucc.rotateleft",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_COMMA,
                "category.trucc.main"
        ));

        rotateCameraRight = KeyBindingRegistryImpl.registerKeyBinding(new KeyBinding(
                "key.trucc.rotateright",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_PERIOD,
                "category.trucc.main"
        ));
    }
}
