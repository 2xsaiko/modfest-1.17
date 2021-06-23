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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import trucc.util.Catenary;

import net.dblsaiko.qcommon.croco.Vec3;

import static java.lang.Math.min;
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

            Vec3 p1 = Vec3.from(Vec3d.ofCenter(pos1));
            Vec3 p2 = Vec3.from(Vec3d.ofCenter(pos2));
            List<Vec3> draw = Catenary.draw(p1, p2, 1.05f * p2.sub(p1).getLength(), 5, 100);
            Vec3 last = null;

            for (Vec3 vec3 : draw) {
                if (last != null) {
                    this.renderCableSegment(ctx, last, vec3);
                }

                last = vec3;
            }
        }
    }

    private void renderCableSegment(WorldRenderContext ctx, Vec3 from, Vec3 to) {
        Vec3 rel = new Vec3(0.0f, 1.0f, 0.0f);
        Vec3 direction = to.sub(from).getNormalized();

        if (Math.abs(rel.dot(direction)) > 0.99) {
            rel = new Vec3(1.0f, 0.0f, 0.0f);
        }

        Vec3 fromRel = from.sub(Vec3.from(ctx.camera().getPos()));
        Vec3 toRel = to.sub(Vec3.from(ctx.camera().getPos()));

        Vec3 normal1 = rel.cross(direction).getNormalized();
        Vec3 normal2 = direction.cross(normal1);

        Vec3 corner1 = normal1.mul(WIRE_RADIUS).add(normal2.mul(-WIRE_RADIUS));
        Vec3 corner2 = normal1.mul(WIRE_RADIUS).add(normal2.mul(WIRE_RADIUS));
        Vec3 corner3 = normal1.mul(-WIRE_RADIUS).add(normal2.mul(WIRE_RADIUS));
        Vec3 corner4 = normal1.mul(-WIRE_RADIUS).add(normal2.mul(-WIRE_RADIUS));
        Vec3 from1 = fromRel.add(corner1);
        Vec3 from2 = fromRel.add(corner2);
        Vec3 from3 = fromRel.add(corner3);
        Vec3 from4 = fromRel.add(corner4);
        Vec3 to1 = toRel.add(corner1);
        Vec3 to2 = toRel.add(corner2);
        Vec3 to3 = toRel.add(corner3);
        Vec3 to4 = toRel.add(corner4);

        float tw = 16.0f;
        float th = 64.0f;
        float partialHeight = min(64, 16 * to.sub(from).getLength()) / th;

        int lightFrom = WorldRenderer.getLightmapCoordinates(ctx.world(), new BlockPos(from.toMCVec3i()));
        int lightTo = WorldRenderer.getLightmapCoordinates(ctx.world(), new BlockPos(to.toMCVec3i()));
        Vec3 normalFrom = direction.negate();
        Vec3 normalTo = direction;

        VertexConsumerProvider consumers = Objects.requireNonNull(ctx.consumers());
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getEntitySolid(TEXTURE));

        Matrix4f mat = ctx.matrixStack().peek().getModel();

        // small quad on from side
        buffer
            .vertex(mat, from1.x, from1.y, from1.z)
            .color(255, 255, 255, 255)
            .texture(8 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(normalFrom.x, normalFrom.y, normalFrom.z)
            .next();
        buffer
            .vertex(mat, from4.x, from4.y, from4.z)
            .color(255, 255, 255, 255)
            .texture(10 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(normalFrom.x, normalFrom.y, normalFrom.z)
            .next();
        buffer
            .vertex(mat, from3.x, from3.y, from3.z)
            .color(255, 255, 255, 255)
            .texture(10 / tw, 2 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(normalFrom.x, normalFrom.y, normalFrom.z)
            .next();
        buffer
            .vertex(mat, from2.x, from2.y, from2.z)
            .color(255, 255, 255, 255)
            .texture(8 / tw, 2 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(normalFrom.x, normalFrom.y, normalFrom.z)
            .next();

        // small quad on to side
        buffer
            .vertex(mat, to1.x, to1.y, to1.z)
            .color(255, 255, 255, 255)
            .texture(8 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightTo)
            .normal(normalTo.x, normalTo.y, normalTo.z)
            .next();
        buffer
            .vertex(mat, to2.x, to2.y, to2.z)
            .color(255, 255, 255, 255)
            .texture(8 / tw, 2 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightTo)
            .normal(normalTo.x, normalTo.y, normalTo.z)
            .next();
        buffer
            .vertex(mat, to3.x, to3.y, to3.z)
            .color(255, 255, 255, 255)
            .texture(10 / tw, 2 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightTo)
            .normal(normalTo.x, normalTo.y, normalTo.z)
            .next();
        buffer
            .vertex(mat, to4.x, to4.y, to4.z)
            .color(255, 255, 255, 255)
            .texture(10 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightTo)
            .normal(normalTo.x, normalTo.y, normalTo.z)
            .next();

        // side 1
        buffer
            .vertex(mat, from1.x, from1.y, from1.z)
            .color(255, 255, 255, 255)
            .texture(0 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(normal1.x, normal1.y, normal1.z)
            .next();
        buffer
            .vertex(mat, from2.x, from2.y, from2.z)
            .color(255, 255, 255, 255)
            .texture(2 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(normal1.x, normal1.y, normal1.z)
            .next();
        buffer
            .vertex(mat, to2.x, to2.y, to2.z)
            .color(255, 255, 255, 255)
            .texture(2 / tw, partialHeight)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(normal1.x, normal1.y, normal1.z)
            .next();
        buffer
            .vertex(mat, to1.x, to1.y, to1.z)
            .color(255, 255, 255, 255)
            .texture(0 / tw, partialHeight)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(normal1.x, normal1.y, normal1.z)
            .next();

        // side 2
        buffer
            .vertex(mat, from2.x, from2.y, from2.z)
            .color(255, 255, 255, 255)
            .texture(2 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(normal2.x, normal2.y, normal2.z)
            .next();
        buffer
            .vertex(mat, from3.x, from3.y, from3.z)
            .color(255, 255, 255, 255)
            .texture(4 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(normal2.x, normal2.y, normal2.z)
            .next();
        buffer
            .vertex(mat, to3.x, to3.y, to3.z)
            .color(255, 255, 255, 255)
            .texture(4 / tw, partialHeight)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(normal2.x, normal2.y, normal2.z)
            .next();
        buffer
            .vertex(mat, to2.x, to2.y, to2.z)
            .color(255, 255, 255, 255)
            .texture(2 / tw, partialHeight)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(normal2.x, normal2.y, normal2.z)
            .next();

        // side 3
        buffer
            .vertex(mat, from3.x, from3.y, from3.z)
            .color(255, 255, 255, 255)
            .texture(4 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(-normal1.x, -normal1.y, -normal1.z)
            .next();
        buffer
            .vertex(mat, from4.x, from4.y, from4.z)
            .color(255, 255, 255, 255)
            .texture(6 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(-normal1.x, -normal1.y, -normal1.z)
            .next();
        buffer
            .vertex(mat, to4.x, to4.y, to4.z)
            .color(255, 255, 255, 255)
            .texture(6 / tw, partialHeight)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(-normal1.x, -normal1.y, -normal1.z)
            .next();
        buffer
            .vertex(mat, to3.x, to3.y, to3.z)
            .color(255, 255, 255, 255)
            .texture(4 / tw, partialHeight)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(-normal1.x, -normal1.y, -normal1.z)
            .next();

        // side 4
        buffer
            .vertex(mat, from4.x, from4.y, from4.z)
            .color(255, 255, 255, 255)
            .texture(6 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(-normal2.x, -normal2.y, -normal2.z)
            .next();
        buffer
            .vertex(mat, from1.x, from1.y, from1.z)
            .color(255, 255, 255, 255)
            .texture(8 / tw, 0 / th)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(-normal2.x, -normal2.y, -normal2.z)
            .next();
        buffer
            .vertex(mat, to1.x, to1.y, to1.z)
            .color(255, 255, 255, 255)
            .texture(8 / tw, partialHeight)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(-normal2.x, -normal2.y, -normal2.z)
            .next();
        buffer
            .vertex(mat, to4.x, to4.y, to4.z)
            .color(255, 255, 255, 255)
            .texture(6 / tw, partialHeight)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(lightFrom)
            .normal(-normal2.x, -normal2.y, -normal2.z)
            .next();
    }
}
