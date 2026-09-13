package com.hbm.world.gen.nbt;

import com.hbm.HbmNuclearTechMod;
import com.hbm.config.GeneralConfig;
import com.hbm.world.gen.NbtStructurePiece;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.ToIntBiFunction;

/**
 * 1.7 {@code NBTStructure.Start} jigsaw assembler, emitting 1.20 structure pieces.
 */
public final class NbtStructureAssembler {
    private NbtStructureAssembler() {
    }

    public static void addPieces(StructurePiecesBuilder builder, SpawnCondition spawn, RandomSource randomSource,
                                 ChunkPos chunk, ToIntBiFunction<Integer, Integer> height) {
        if (spawn == null || !spawn.hasGeometry()) {
            return;
        }
        Random random = new Random(randomSource.nextLong());
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        JigsawPiece startPiece;
        if (spawn.structure != null) {
            startPiece = spawn.structure;
        } else {
            JigsawPool startPool = spawn.pool(spawn.startPool);
            startPiece = startPool == null ? null : startPool.get(random);
        }
        if (startPiece == null) {
            return;
        }
        int coord = random.nextInt(4);
        Assembled start = new Assembled(spawn, startPiece, x, 0, z, coord, null, 0);
        List<Assembled> components = new ArrayList<>();
        components.add(start);
        List<Assembled> queued = new ArrayList<>();
        if (spawn.structure == null) {
            queued.add(start);
        }
        Set<JigsawPiece> required = findRequired(spawn);

        while (!queued.isEmpty()) {
            queued.sort((a, b) -> Integer.compare(b.priority, a.priority));
            int match = queued.get(0).priority;
            int max = 1;
            while (max < queued.size() && queued.get(max).priority == match) {
                max++;
            }
            Assembled from = queued.remove(random.nextInt(max));
            if (from.piece.structure().fromConnections == null || from.piece.structure().fromConnections.isEmpty()) {
                continue;
            }
            int distance = distanceTo(from.box, chunk);
            boolean fallbacksOnly = required.isEmpty()
                    && (components.size() >= spawn.sizeLimit || distance >= spawn.rangeLimit)
                    || components.size() > 1024;

            for (List<JigsawConnection> unshuffled : from.piece.structure().fromConnections) {
                List<JigsawConnection> connectionList = new ArrayList<>(unshuffled);
                Collections.shuffle(connectionList, random);
                for (JigsawConnection fromConnection : connectionList) {
                    if (from.connectedFrom == fromConnection) {
                        continue;
                    }
                    if (fallbacksOnly) {
                        JigsawPool pool = spawn.pools == null ? null : spawn.pools.get(fromConnection.poolName);
                        if (pool != null && pool.fallback != null) {
                            Assembled fallback = buildNext(random, spawn, spawn.pools.get(pool.fallback), from,
                                    fromConnection, components);
                            add(components, fallback, fromConnection.placementPriority);
                        }
                        continue;
                    }
                    JigsawPool nextPool = spawn.pool(fromConnection.poolName);
                    if (nextPool == null) {
                        HbmNuclearTechMod.LOGGER.warn("[Jigsaw] invalid pool {}", fromConnection.poolName);
                        continue;
                    }
                    Assembled next = null;
                    while (nextPool.totalWeight() > 0) {
                        next = buildNext(random, spawn, nextPool, from, fromConnection, components);
                        if (next != null && !intersectsOthers(components, from, next.box)) {
                            break;
                        }
                        next = null;
                    }
                    if (next != null) {
                        add(components, next, fromConnection.placementPriority);
                        queued.add(next);
                        required.remove(next.piece);
                    } else if (nextPool.fallback != null && spawn.pools != null) {
                        BlockPos check = connectionTarget(from, fromConnection);
                        if (!insideOthers(components, from, check)) {
                            Assembled fallback = buildNext(random, spawn, spawn.pools.get(nextPool.fallback), from,
                                    fromConnection, components);
                            add(components, fallback, fromConnection.placementPriority);
                        }
                    }
                }
            }
        }

        int y = sampleHeight(start, height);
        y = Mth.clamp(y, spawn.minHeight, spawn.maxHeight);
        for (Assembled component : components) {
            if (!component.piece.conformToTerrain && !component.piece.alignToTerrain) {
                component.offsetY(y);
            } else if (component.piece.alignToTerrain) {
                component.offsetY(sampleHeight(component, height));
            }
        }
        if (GeneralConfig.enableDebugMode.get()) {
            HbmNuclearTechMod.LOGGER.info("[Debug] Spawning NBT structure {} with {} piece(s) at {}, {}",
                    spawn.name, components.size(), x, z);
        }
        for (Assembled component : components) {
            builder.addPiece(component.toPiece(spawn.name));
        }
    }

    private static int sampleHeight(Assembled component, ToIntBiFunction<Integer, Integer> height) {
        int total = 0;
        int n = 0;
        BoundingBox box = component.box;
        for (int z = box.minZ(); z <= box.maxZ(); z += 4) {
            for (int x = box.minX(); x <= box.maxX(); x += 4) {
                total += height.applyAsInt(x, z);
                n++;
            }
        }
        if (n == 0) {
            return 64 + component.piece.heightOffset;
        }
        return total / n + component.piece.heightOffset;
    }

    private static Set<JigsawPiece> findRequired(SpawnCondition spawn) {
        Set<JigsawPiece> required = new HashSet<>();
        if (spawn.pools == null) {
            return required;
        }
        for (JigsawPool pool : spawn.pools.values()) {
            for (JigsawPool.Entry entry : pool.pieces()) {
                if (entry.piece().required) {
                    required.add(entry.piece());
                }
            }
        }
        return required;
    }

    private static void add(List<Assembled> components, Assembled component, int priority) {
        if (component == null) {
            return;
        }
        component.priority = priority;
        components.add(component);
    }

    private static Assembled buildNext(Random random, SpawnCondition spawn, JigsawPool pool, Assembled from,
                                       JigsawConnection fromConnection, List<Assembled> components) {
        if (pool == null) {
            return null;
        }
        JigsawPiece nextPiece = pool.get(random);
        if (nextPiece == null) {
            return null;
        }
        if (nextPiece.instanceLimit > 0) {
            int instances = 0;
            for (Assembled component : components) {
                if (component.piece == nextPiece) {
                    instances++;
                    if (instances >= nextPiece.instanceLimit) {
                        return null;
                    }
                }
            }
        }
        List<JigsawConnection> connectionPool = nextPiece.structure().getConnectionPool(fromConnection.dir,
                fromConnection.targetName);
        if (connectionPool == null || connectionPool.isEmpty()) {
            return null;
        }
        JigsawConnection toConnection = connectionPool.get(random.nextInt(connectionPool.size()));
        int nextCoord = from.nextCoordBase(fromConnection, toConnection, random);
        BlockPos pos = connectionTarget(from, fromConnection);
        int ox = nextPiece.structure().rotateX(toConnection.pos.getX(), toConnection.pos.getZ(), nextCoord);
        int oy = toConnection.pos.getY();
        int oz = nextPiece.structure().rotateZ(toConnection.pos.getX(), toConnection.pos.getZ(), nextCoord);
        return new Assembled(spawn, nextPiece, pos.getX() - ox, pos.getY() - oy, pos.getZ() - oz, nextCoord,
                toConnection, 0);
    }

    private static BlockPos connectionTarget(Assembled component, JigsawConnection connection) {
        Direction extend = component.rotateDir(connection.dir);
        int x = component.box.minX() + component.piece.structure().rotateX(connection.pos.getX(),
                connection.pos.getZ(), component.coordBaseMode) + extend.getStepX();
        int y = component.box.minY() + connection.pos.getY() + extend.getStepY();
        int z = component.box.minZ() + component.piece.structure().rotateZ(connection.pos.getX(),
                connection.pos.getZ(), component.coordBaseMode) + extend.getStepZ();
        return new BlockPos(x, y, z);
    }

    private static int distanceTo(BoundingBox box, ChunkPos chunk) {
        int x = (box.minX() + box.maxX()) / 2;
        int z = (box.minZ() + box.maxZ()) / 2;
        return Math.max(Math.abs(x - (chunk.x << 4)), Math.abs(z - (chunk.z << 4)));
    }

    private static boolean intersectsOthers(List<Assembled> components, Assembled self, BoundingBox box) {
        for (Assembled component : components) {
            if (component == self) {
                continue;
            }
            if (component.box.intersects(box)) {
                return true;
            }
        }
        return false;
    }

    private static boolean insideOthers(List<Assembled> components, Assembled self, BlockPos pos) {
        for (Assembled component : components) {
            if (component == self) {
                continue;
            }
            if (component.box.isInside(pos)) {
                return true;
            }
        }
        return false;
    }

    private static final class Assembled {
        final SpawnCondition spawn;
        final JigsawPiece piece;
        final int coordBaseMode;
        final JigsawConnection connectedFrom;
        BoundingBox box;
        int priority;

        Assembled(SpawnCondition spawn, JigsawPiece piece, int x, int y, int z, int coordBaseMode,
                  JigsawConnection connectedFrom, int priority) {
            this.spawn = spawn;
            this.piece = piece;
            this.coordBaseMode = coordBaseMode;
            this.connectedFrom = connectedFrom;
            this.priority = priority;
            NbtStructure structure = piece.structure();
            if (coordBaseMode == 1 || coordBaseMode == 3) {
                this.box = new BoundingBox(x, y, z, x + structure.sizeZ - 1, y + structure.sizeY - 1,
                        z + structure.sizeX - 1);
            } else {
                this.box = new BoundingBox(x, y, z, x + structure.sizeX - 1, y + structure.sizeY - 1,
                        z + structure.sizeZ - 1);
            }
        }

        void offsetY(int y) {
            box = box.moved(0, y, 0);
        }

        Direction rotateDir(Direction dir) {
            if (dir.getAxis() == Direction.Axis.Y) {
                return dir;
            }
            return switch (coordBaseMode) {
                case 1 -> dir.getClockWise();
                case 2 -> dir.getOpposite();
                case 3 -> dir.getCounterClockWise();
                default -> dir;
            };
        }

        int nextCoordBase(JigsawConnection fromConnection, JigsawConnection toConnection, Random random) {
            if (fromConnection.dir.getAxis() == Direction.Axis.Y) {
                return fromConnection.rollable ? random.nextInt(4) : coordBaseMode;
            }
            Direction from = fromConnection.dir.getOpposite();
            Direction to = toConnection.dir;
            for (int i = 0; i < 4; i++) {
                if (from == to) {
                    return (i + coordBaseMode) % 4;
                }
                from = from.getCounterClockWise();
            }
            return coordBaseMode;
        }

        StructurePiece toPiece(String spawnName) {
            return new NbtStructurePiece(piece.name, spawnName, coordBaseMode, piece.conformToTerrain,
                    piece.palette, box);
        }
    }
}
