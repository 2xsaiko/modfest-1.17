package trucc.world;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import trucc.Trucc;
import trucc.util.Catenary;

import net.dblsaiko.qcommon.croco.Vec3;

import static trucc.Trucc.MOD_ID;

public class CableTracker {
    private static final float WIRE_RADIUS = 1 / 16f;
    private static final Identifier TEXTURE = new Identifier(MOD_ID, "textures/cable.png");

    private static final Map<World, CableTracker> FOR_WORLD = Collections.synchronizedMap(new WeakHashMap<>());

    private final World world;
    private final Set<Set<BlockPos>> connections = new HashSet<>();
    private final Set<Set<BlockPos>> connectionsView = Collections.unmodifiableSet(this.connections);
    private final Multimap<BlockPos, Set<BlockPos>> byPos = HashMultimap.create();
    private final Map<Set<BlockPos>, List<Vec3>> segments = new HashMap<>();

    private final List<Set<BlockPos>> waiting = Collections.synchronizedList(new ArrayList<>());

    public CableTracker(World world) {
        this.world = world;
    }

    public static CableTracker get(World world) {
        return FOR_WORLD.computeIfAbsent(world, _world -> new CableTracker(world));
    }

    public void add(BlockPos p1, BlockPos p2) {
        var s = Set.of(p1, p2);
        this.waiting.add(s);
    }

    public void tick() {
        var list = new ArrayList<>(this.waiting);
        this.waiting.clear();

        for (Set<BlockPos> s : list) {
            Iterator<BlockPos> iterator = s.iterator();
            BlockPos p1 = iterator.next();
            BlockPos p2 = iterator.next();

            this.connections.add(s);
            this.byPos.put(p1, s);
            this.byPos.put(p2, s);
            this.addSegments(p1, p2);
        }
    }

    private boolean addSegments(BlockPos pos1, BlockPos pos2) {
        Trucc trucc = Trucc.getInstance();
        Set<BlockPos> set = Set.of(pos1, pos2);

        BlockState state1 = this.world.getBlockState(pos1);
        BlockState state2 = this.world.getBlockState(pos2);

        if (!state1.isOf(trucc.blocks.ziplineAnchor) || !state2.isOf(trucc.blocks.ziplineAnchor)) {
            return false;
        }

        Direction side1 = state1.get(Properties.FACING);
        Direction side2 = state2.get(Properties.FACING);

        Vec3 p1 = Vec3.from(Vec3d.ofCenter(pos1)).add(this.getSideOffset(side1));
        Vec3 p2 = Vec3.from(Vec3d.ofCenter(pos2)).add(this.getSideOffset(side2));
        float length = p2.sub(p1).getLength();
        List<Vec3> draw = Catenary.draw(p1, p2, 1.05f * length, (int) length, 100);
        this.segments.put(set, draw);

        Vec3 last = null;

        for (Vec3 vec3 : draw) {
            if (last != null) {
                raycast(last, vec3, pos -> this.byPos.put(pos, set));
            }

            last = vec3;
        }

        return true;
    }

    public void remove(BlockPos p1, BlockPos p2) {
        var s = Set.of(p1, p2);
        this.connections.remove(s);
        this.byPos.remove(p1, s);
        this.byPos.remove(p2, s);
        this.segments.remove(s);
        this.waiting.remove(s);
    }

    public void removeAll(BlockPos pos) {
        Collection<Set<BlockPos>> sets = this.byPos.get(pos);

        for (Set<BlockPos> set : sets) {
            this.connections.remove(set);
            this.segments.remove(set);
            this.waiting.remove(set);

            for (BlockPos blockPos : set) {
                // will this cry because I'm technically iterating over byPos
                // here?
                this.byPos.remove(blockPos, set);
            }
        }
    }

    public List<Vec3> getSegments(BlockPos pos1, BlockPos pos2) {
        if (pos1 == null || pos2 == null) {
            return List.of();
        }

        return this.segments.getOrDefault(Set.of(pos1, pos2), List.of());
    }

    public Collection<Set<BlockPos>> getCablesInBlock(BlockPos pos) {
        return this.byPos.get(pos);
    }

    public Set<Set<BlockPos>> getConnections() {
        return this.connectionsView;
    }

    private Vec3 getSideOffset(Direction side) {
        return Vec3.from(side.getOpposite().getVector()).mul(10 / 32f);
    }

    private static void raycast(Vec3 start, Vec3 end, Consumer<BlockPos> op) {
        BlockView.raycast(start.toVec3d(), end.toVec3d(), null, (ctx, pos) -> {
            op.accept(pos);
            return null;
        }, ctx -> null);
    }
}
