package trucc.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
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
import org.jetbrains.annotations.Nullable;
import trucc.Trucc;
import trucc.block.BlockCablePlacement;
import trucc.block.entity.CableDriver;
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
    private final Multimap<BlockPos, Set<BlockPos>> segmentsPos = HashMultimap.create();
    private final Multimap<Set<BlockPos>, BlockPos> segmentsByCable = HashMultimap.create();
    private final Map<Set<BlockPos>, List<Vec3>> segments = new HashMap<>();

    private final Map<List<BlockPos>, CableDriver> drivers = new HashMap<>();
    private final Map<Set<BlockPos>, List<BlockPos>> loopsBySegment = new HashMap<>();

    private final List<Set<BlockPos>> waiting = Collections.synchronizedList(new ArrayList<>());
    private final List<BlockPos> waitingRebuild = Collections.synchronizedList(new ArrayList<>());

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
        var listRebuild = new ArrayList<>(this.waitingRebuild);
        this.waitingRebuild.clear();

        for (Set<BlockPos> s : list) {
            Iterator<BlockPos> iterator = s.iterator();
            BlockPos p1 = iterator.next();
            BlockPos p2 = iterator.next();

            this.connections.add(s);
            this.segmentsPos.put(p1, s);
            this.segmentsPos.put(p2, s);
            this.byPos.put(p1, s);
            this.byPos.put(p2, s);
            this.addSegments(p1, p2);
        }

        for (BlockPos blockPos : listRebuild) {
            for (Set<BlockPos> set : this.getCableEndsInBlock(blockPos)) {
                Iterator<BlockPos> iterator = set.iterator();
                BlockPos p1 = iterator.next();
                BlockPos p2 = iterator.next();
                this.removeSegments(set);
                this.addSegments(p1, p2);
            }
        }
    }

    @Nullable
    public List<BlockPos> addLoop(List<BlockPos> loop, CableDriver driver) {
        if (loop.size() < 3) {
            return null;
        }

        loop = new ArrayList<>(loop);
        Set<BlockPos> testSet = new HashSet<>();

        if (loop.get(0).compareTo(loop.get(loop.size() - 1)) > 0) {
            Collections.reverse(loop);
        }

        List<Set<BlockPos>> sets = new ArrayList<>(loop.size());
        BlockPos prev = loop.get(loop.size() - 1);

        for (BlockPos pos : loop) {
            if (!testSet.add(pos)) {
                return null;
            }

            Set<BlockPos> set = Set.of(prev, pos);

            if (!this.connections.contains(set)) {
                return null;
            }

            sets.add(set);
            prev = pos;
        }

        this.drivers.put(loop, driver);

        for (Set<BlockPos> set : sets) {
            this.loopsBySegment.put(set, loop);
        }

        return loop;
    }

    public float getSpeed(BlockPos pos1, BlockPos pos2) {
        Set<BlockPos> set = Set.of(pos1, pos2);
        List<BlockPos> loop = this.loopsBySegment.get(set);

        if (loop == null) {
            return 0.0f;
        }

        CableDriver cableDriver = this.drivers.get(loop);

        if (cableDriver == null) {
            return 0.0f;
        }

        return cableDriver.getSpeed();
    }

    @Nullable
    public BlockPos getOrigin(BlockPos pos1, BlockPos pos2) {
        List<BlockPos> loop = this.loopsBySegment.get(Set.of(pos1, pos2));

        if (loop == null) {
            return null;
        }

        BlockPos last = loop.get(loop.size() - 1);

        for (BlockPos cur : loop) {
            if (last.equals(pos1) && cur.equals(pos2)) {
                return pos1;
            } else if (last.equals(pos2) && cur.equals(pos1)) {
                return pos2;
            }

            last = cur;
        }

        return null;
    }

    private boolean addSegments(BlockPos pos1, BlockPos pos2) {
        Trucc trucc = Trucc.getInstance();
        Set<BlockPos> set = Set.of(pos1, pos2);

        BlockState state1 = this.world.getBlockState(pos1);
        BlockState state2 = this.world.getBlockState(pos2);

        if (!(state1.getBlock() instanceof BlockCablePlacement wp1) || !(state2.getBlock() instanceof BlockCablePlacement wp2)) {
            return false;
        }

        Vec3 wireOffset1 = wp1.getWireOffset(this.world, pos1, state1, pos2);
        Vec3 wireOffset2 = wp2.getWireOffset(this.world, pos2, state2, pos1);

        if (wireOffset1 == null && wireOffset2 != null) {
            wireOffset1 = wp1.getDependentWireOffset(this.world, pos1, state1, pos2, wireOffset2);
        } else if (wireOffset2 == null && wireOffset1 != null) {
            wireOffset2 = wp2.getDependentWireOffset(this.world, pos2, state2, pos1, wireOffset1);
        } else if (wireOffset1 == null && wireOffset2 == null) {
            wireOffset1 = wireOffset2 = Vec3.ORIGIN;
        }

        Vec3 p1 = Vec3.from(Vec3d.ofCenter(pos1)).add(wireOffset1);
        Vec3 p2 = Vec3.from(Vec3d.ofCenter(pos2)).add(wireOffset2);
        float length = p2.sub(p1).getLength();
        List<Vec3> draw = Catenary.draw(p1, p2, 1.05f * length, (int) length, 100);
        this.segments.put(set, draw);

        Vec3 last = null;

        for (Vec3 vec3 : draw) {
            if (last != null) {
                raycast(last, vec3, pos -> {
                    this.segmentsPos.put(pos, set);
                    this.segmentsByCable.put(set, pos);
                });
            }

            last = vec3;
        }

        return true;
    }

    private void removeSegments(Set<BlockPos> set) {
        Collection<BlockPos> positions = this.segmentsByCable.removeAll(set);
        this.segments.remove(set);

        for (BlockPos pos : positions) {
            this.segmentsPos.remove(pos, set);
        }
    }

    public void updateSegmentsFrom(BlockPos pos) {
        this.waitingRebuild.add(pos);
    }

    public void remove(BlockPos p1, BlockPos p2) {
        var set = Set.of(p1, p2);
        List<BlockPos> loop = this.loopsBySegment.remove(set);

        if (loop != null) {
            CableDriver driver = this.drivers.remove(loop);

            if (driver != null) {
                driver.notifyRemove(set);
            }
        }

        this.removeSegments(set);
        this.connections.remove(set);
        this.waiting.remove(set);
        this.byPos.remove(p1, set);
        this.byPos.remove(p2, set);
    }

    public void removeAll(BlockPos pos) {
        Collection<Set<BlockPos>> sets = this.byPos.get(pos);
        Multimap<BlockPos, Set<BlockPos>> toRemove = HashMultimap.create();

        for (Set<BlockPos> set : sets) {
            List<BlockPos> loop = this.loopsBySegment.remove(set);

            if (loop != null) {
                CableDriver driver = this.drivers.remove(loop);

                if (driver != null) {
                    driver.notifyRemove(set);
                }
            }

            this.removeSegments(set);
            this.connections.remove(set);
            this.waiting.remove(set);

            for (BlockPos blockPos : set) {
                toRemove.put(blockPos, set);
            }
        }

        toRemove.forEach(this.byPos::remove);
    }

    public List<Vec3> getSegments(BlockPos pos1, BlockPos pos2) {
        if (pos1 == null || pos2 == null) {
            return List.of();
        }

        return this.segments.getOrDefault(Set.of(pos1, pos2), List.of());
    }

    public Collection<Set<BlockPos>> getCablesInBlock(BlockPos pos) {
        return this.segmentsPos.get(pos);
    }

    public Collection<Set<BlockPos>> getCableEndsInBlock(BlockPos pos) {
        return this.byPos.get(pos);
    }

    public Set<Set<BlockPos>> getConnections() {
        return this.connectionsView;
    }

    private static void raycast(Vec3 start, Vec3 end, Consumer<BlockPos> op) {
        BlockView.raycast(start.toVec3d(), end.toVec3d(), null, (ctx, pos) -> {
            op.accept(pos);
            return null;
        }, ctx -> null);
    }
}
