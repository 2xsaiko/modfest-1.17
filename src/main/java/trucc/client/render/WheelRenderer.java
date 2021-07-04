package trucc.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import trucc.Trucc;

import net.dblsaiko.quacklib.client.render.model.ObjGlRenderer;
import net.dblsaiko.quacklib.model.OBJRoot;
import net.dblsaiko.quacklib.model.ObjparserKt;

import static trucc.Trucc.id;

public class WheelRenderer {
    private static final WeakHashMap<ClientWorld, WheelRenderer> FOR_WORLD = new WeakHashMap<>();

    // private static NewGLRenderer renderer;
    private static ObjGlRenderer shittyRenderer;

    private final Set<BlockPos> blocks = new HashSet<>();

    public static WheelRenderer get(ClientWorld world) {
        return FOR_WORLD.computeIfAbsent(world, _world -> new WheelRenderer());
    }

    public void add(BlockPos pos) {
        this.blocks.add(pos);
    }

    public void remove(BlockPos pos) {
        this.blocks.remove(pos);
    }

    public void render(WorldRenderContext ctx) {
        initialize();
        Trucc t = Trucc.getInstance();
        ClientWorld world = ctx.world();
        MatrixStack stack = ctx.matrixStack();

        stack.push();
        Vec3d camPos = ctx.camera().getPos();
        stack.translate(-camPos.x, -camPos.y, -camPos.z);

        for (BlockPos pos : this.blocks) {
            BlockState state = world.getBlockState(pos);

            if (state.isOf(t.blocks.motorWheel)) {
                stack.push();
                stack.translate(pos.getX() + 0.5, pos.getY() + 10 / 16.0, pos.getZ() + 0.5);
                Vec3f offset = state.get(Properties.HORIZONTAL_FACING).getUnitVector();
                offset.scale(4 / 16f);
                stack.translate(offset.getX(), offset.getY(), offset.getZ());
                stack.multiply(new Quaternion(Vec3f.POSITIVE_Y, System.currentTimeMillis() % 3600 / 10f, true));

                // renderer.draw(
                //     ctx.projectionMatrix(),
                //     ctx.matrixStack().peek().getModel(),
                //     WorldRenderer.getLightmapCoordinates(world, state, pos)
                // );
                shittyRenderer.draw(
                    stack,
                    ctx.consumers(),
                    WorldRenderer.getLightmapCoordinates(world, state, pos)
                );
                stack.pop();
            }
        }

        stack.pop();
    }

    public static void initialize() {
        // if (renderer == null) {
        //     renderer = new NewGLRenderer(model, overrides);
        // }
        if (shittyRenderer == null) {
            OBJRoot model = ObjparserKt.orEmpty(ObjparserKt.loadOBJ(id("models/block/wheel.obj")));
            Map<String, String> overrides = Map.of("Material", "trucc:textures/block/wheel2.png");
            shittyRenderer = new ObjGlRenderer(model, overrides);
        }
    }
}
