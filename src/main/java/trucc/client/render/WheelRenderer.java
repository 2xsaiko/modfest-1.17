package trucc.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import trucc.Trucc;

import net.dblsaiko.quacklib.client.render.model.NewGLRenderer;
import net.dblsaiko.quacklib.model.ObjparserKt;

import static trucc.Trucc.id;

public class WheelRenderer {
    private static final WeakHashMap<ClientWorld, WheelRenderer> FOR_WORLD = new WeakHashMap<>();

    private static NewGLRenderer renderer;

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

        for (BlockPos pos : this.blocks) {
            BlockState state = world.getBlockState(pos);

            if (state.isOf(t.blocks.motorWheel)) {
                renderer.draw(
                    ctx.projectionMatrix(),
                    ctx.matrixStack().peek().getModel(),
                    WorldRenderer.getLightmapCoordinates(world, state, pos)
                );
            }
        }
    }

    public static void initialize() {
        if (renderer == null) {
            renderer = new NewGLRenderer(ObjparserKt.orEmpty(ObjparserKt.loadOBJ(id("models/block/wheel.obj"))), Map.of("Material", "trucc:textures/block/wheel.png"));
        }
    }
}
