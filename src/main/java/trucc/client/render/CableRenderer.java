package trucc.client.render;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import static trucc.Trucc.MOD_ID;

public class CableRenderer {
    private static final float WIRE_RADIUS = 1 / 16f;
    private static final Identifier TEXTURE = new Identifier(MOD_ID, "textures/cable.png");

    private static final WeakHashMap<ClientWorld, CableRenderer> FOR_WORLD = new WeakHashMap<>();

    private final Set<Set<BlockPos>> connections = new HashSet<>();
    private final Multimap<BlockPos, Set<BlockPos>> byPos = HashMultimap.create();

    public static CableRenderer get(ClientWorld world) {
        return FOR_WORLD.computeIfAbsent(world, _world -> new CableRenderer());
    }

    public void add(BlockPos p1, BlockPos p2) {
        var s = Set.of(p1, p2);
        this.connections.add(s);
        this.byPos.put(p1, s);
        this.byPos.put(p2, s);
    }

    public void remove(BlockPos p1, BlockPos p2) {
        var s = Set.of(p1, p2);
        this.connections.remove(s);
        this.byPos.remove(p1, s);
        this.byPos.remove(p2, s);
    }

    public void removeAll(BlockPos pos) {
        Collection<Set<BlockPos>> sets = this.byPos.get(pos);

        for (Set<BlockPos> set : sets) {
            this.connections.remove(set);

            for (BlockPos blockPos : set) {
                // will this cry because I'm technically iterating over byPos
                // here?
                this.byPos.remove(blockPos, set);
            }
        }
    }

    public void render(WorldRenderContext ctx) {
        for (Set<BlockPos> connection : this.connections) {
            Iterator<BlockPos> iterator = connection.iterator();
            var pos1 = iterator.next();
            var pos2 = iterator.next();

            // TODO yeah this needs to be better lol
            this.renderCableSegment(ctx, Vec3d.ofCenter(pos1), Vec3d.ofCenter(pos2));
        }
    }

    private void renderCableSegment(WorldRenderContext ctx, Vec3d from, Vec3d to) {
        Vec3d rel = new Vec3d(0.0, 1.0, 0.0);
        Vec3d direction = to.subtract(from).normalize();

        if (rel.dotProduct(direction) > 0.99) {
            rel = new Vec3d(1.0, 0.0, 0.0);
        }

        Vec3d fromRel = from.subtract(ctx.camera().getPos());
        Vec3d toRel = to.subtract(ctx.camera().getPos());

        Vec3d normal1 = rel.crossProduct(direction).normalize();
        Vec3d normal2 = direction.crossProduct(normal1);

        Vec3d corner1 = normal1.multiply(-WIRE_RADIUS).add(normal2.multiply(-WIRE_RADIUS));
        Vec3d corner2 = normal1.multiply(-WIRE_RADIUS).add(normal2.multiply(WIRE_RADIUS));
        Vec3d corner3 = normal1.multiply(WIRE_RADIUS).add(normal2.multiply(WIRE_RADIUS));
        Vec3d corner4 = normal1.multiply(WIRE_RADIUS).add(normal2.multiply(-WIRE_RADIUS));
        Vec3d from1 = fromRel.add(corner1);
        Vec3d from2 = fromRel.add(corner2);
        Vec3d from3 = fromRel.add(corner3);
        Vec3d from4 = fromRel.add(corner4);
        Vec3d to1 = toRel.add(corner1);
        Vec3d to2 = toRel.add(corner2);
        Vec3d to3 = toRel.add(corner3);
        Vec3d to4 = toRel.add(corner4);

        float tw = 16.0f;
        float th = 64.0f;

        int lightFrom = WorldRenderer.getLightmapCoordinates(ctx.world(), new BlockPos(from));
        int lightTo = WorldRenderer.getLightmapCoordinates(ctx.world(), new BlockPos(to));
        Vec3d normalFrom = direction.negate();
        Vec3d normalTo = direction;

        VertexConsumerProvider consumers = Objects.requireNonNull(ctx.consumers());
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getEntityNoOutline(TEXTURE));

        Matrix4f mat = ctx.matrixStack().peek().getModel();

        // small quad on from side
        buffer
            .vertex(mat, (float) from1.x, (float) from1.y, (float) from2.z)
            .color(255, 255, 255, 255)
            .texture(8 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) normalFrom.x, (float) normalFrom.y, (float) normalFrom.z)
            .next();
        buffer
            .vertex(mat, (float) from2.x, (float) from2.y, (float) from2.z)
            .color(255, 255, 255, 255)
            .texture(8 / tw, 2 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) normalFrom.x, (float) normalFrom.y, (float) normalFrom.z)
            .next();
        buffer
            .vertex(mat, (float) from3.x, (float) from3.y, (float) from3.z)
            .color(255, 255, 255, 255)
            .texture(10 / tw, 2 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) normalFrom.x, (float) normalFrom.y, (float) normalFrom.z)
            .next();
        buffer
            .vertex(mat, (float) from4.x, (float) from4.y, (float) from4.z)
            .color(255, 255, 255, 255)
            .texture(10 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) normalFrom.x, (float) normalFrom.y, (float) normalFrom.z)
            .next();

        // small quad on to side
        buffer
            .vertex(mat, (float) to1.x, (float) to1.y, (float) to2.z)
            .color(255, 255, 255, 255)
            .texture(8 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightTo)
            .normal((float) normalTo.x, (float) normalTo.y, (float) normalTo.z)
            .next();
        buffer
            .vertex(mat, (float) to4.x, (float) to4.y, (float) to4.z)
            .color(255, 255, 255, 255)
            .texture(10 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightTo)
            .normal((float) normalTo.x, (float) normalTo.y, (float) normalTo.z)
            .next();
        buffer
            .vertex(mat, (float) to3.x, (float) to3.y, (float) to3.z)
            .color(255, 255, 255, 255)
            .texture(10 / tw, 2 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightTo)
            .normal((float) normalTo.x, (float) normalTo.y, (float) normalTo.z)
            .next();
        buffer
            .vertex(mat, (float) to2.x, (float) to2.y, (float) to2.z)
            .color(255, 255, 255, 255)
            .texture(8 / tw, 2 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightTo)
            .normal((float) normalTo.x, (float) normalTo.y, (float) normalTo.z)
            .next();

        // side 1
        buffer
            .vertex(mat, (float) from1.x, (float) from1.y, (float) from2.z)
            .color(255, 255, 255, 255)
            .texture(0 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) normal1.x, (float) normal1.y, (float) normal1.z)
            .next();
        buffer
            .vertex(mat, (float) from2.x, (float) from2.y, (float) from2.z)
            .color(255, 255, 255, 255)
            .texture(2 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) normal1.x, (float) normal1.y, (float) normal1.z)
            .next();
        buffer
            .vertex(mat, (float) to2.x, (float) to2.y, (float) to2.z)
            .color(255, 255, 255, 255)
            .texture(2 / tw, 64 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) normal1.x, (float) normal1.y, (float) normal1.z)
            .next();
        buffer
            .vertex(mat, (float) to1.x, (float) to1.y, (float) to1.z)
            .color(255, 255, 255, 255)
            .texture(0 / tw, 64 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) normal1.x, (float) normal1.y, (float) normal1.z)
            .next();

        // side 2
        buffer
            .vertex(mat, (float) from2.x, (float) from2.y, (float) from2.z)
            .color(255, 255, 255, 255)
            .texture(2 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) normal2.x, (float) normal2.y, (float) normal2.z)
            .next();
        buffer
            .vertex(mat, (float) from3.x, (float) from3.y, (float) from2.z)
            .color(255, 255, 255, 255)
            .texture(4 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) normal2.x, (float) normal2.y, (float) normal2.z)
            .next();
        buffer
            .vertex(mat, (float) to3.x, (float) to3.y, (float) to2.z)
            .color(255, 255, 255, 255)
            .texture(4 / tw, 64 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) normal2.x, (float) normal2.y, (float) normal2.z)
            .next();
        buffer
            .vertex(mat, (float) to2.x, (float) to2.y, (float) to1.z)
            .color(255, 255, 255, 255)
            .texture(2 / tw, 64 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) normal2.x, (float) normal2.y, (float) normal2.z)
            .next();

        // side 3
        buffer
            .vertex(mat, (float) from3.x, (float) from3.y, (float) from3.z)
            .color(255, 255, 255, 255)
            .texture(4 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) -normal1.x, (float) -normal1.y, (float) -normal1.z)
            .next();
        buffer
            .vertex(mat, (float) from4.x, (float) from4.y, (float) from4.z)
            .color(255, 255, 255, 255)
            .texture(6 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) -normal1.x, (float) -normal1.y, (float) -normal1.z)
            .next();
        buffer
            .vertex(mat, (float) to4.x, (float) to4.y, (float) to4.z)
            .color(255, 255, 255, 255)
            .texture(6 / tw, 64 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) -normal1.x, (float) -normal1.y, (float) -normal1.z)
            .next();
        buffer
            .vertex(mat, (float) to3.x, (float) to3.y, (float) to3.z)
            .color(255, 255, 255, 255)
            .texture(4 / tw, 64 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) -normal1.x, (float) -normal1.y, (float) -normal1.z)
            .next();

        // side 4
        buffer
            .vertex(mat, (float) from4.x, (float) from4.y, (float) from4.z)
            .color(255, 255, 255, 255)
            .texture(6 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) -normal2.x, (float) -normal2.y, (float) -normal2.z)
            .next();
        buffer
            .vertex(mat, (float) from1.x, (float) from1.y, (float) from1.z)
            .color(255, 255, 255, 255)
            .texture(8 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) -normal2.x, (float) -normal2.y, (float) -normal2.z)
            .next();
        buffer
            .vertex(mat, (float) to1.x, (float) to1.y, (float) to1.z)
            .color(255, 255, 255, 255)
            .texture(8 / tw, 64 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) -normal2.x, (float) -normal2.y, (float) -normal2.z)
            .next();
        buffer
            .vertex(mat, (float) to4.x, (float) to4.y, (float) to4.z)
            .color(255, 255, 255, 255)
            .texture(6 / tw, 64 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal((float) -normal2.x, (float) -normal2.y, (float) -normal2.z)
            .next();
    }
}
