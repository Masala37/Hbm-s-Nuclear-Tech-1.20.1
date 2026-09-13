package com.hbm.world.gen.nbt;

import com.hbm.HbmNuclearTechMod;
import com.hbm.blockentity.machine.DecoLootBlockEntity;
import com.hbm.config.StructureConfig;
import com.hbm.inventory.loot.StructureLoot;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom 1.7 NBT structure (version 1, palette meta as a string). Not a vanilla structure template.
 */
public final class NbtStructure {
    public final String name;
    public final int sizeX;
    public final int sizeY;
    public final int sizeZ;
    public final Cell[][][] cells;
    public final List<List<JigsawConnection>> fromConnections;
    public final Map<String, List<JigsawConnection>> toTopConnections;
    public final Map<String, List<JigsawConnection>> toBottomConnections;
    public final Map<String, List<JigsawConnection>> toHorizontalConnections;
    public final boolean loaded;

    private NbtStructure(String name, int sizeX, int sizeY, int sizeZ, Cell[][][] cells,
                         List<List<JigsawConnection>> fromConnections,
                         Map<String, List<JigsawConnection>> toTop,
                         Map<String, List<JigsawConnection>> toBottom,
                         Map<String, List<JigsawConnection>> toHorizontal,
                         boolean loaded) {
        this.name = name;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.cells = cells;
        this.fromConnections = fromConnections;
        this.toTopConnections = toTop;
        this.toBottomConnections = toBottom;
        this.toHorizontalConnections = toHorizontal;
        this.loaded = loaded;
    }

    public static NbtStructure load(ResourceLocation location) {
        String path = "/assets/" + location.getNamespace() + "/" + location.getPath();
        InputStream stream = NbtStructure.class.getResourceAsStream(path);
        if (stream == null) {
            HbmNuclearTechMod.LOGGER.error("NBT structure missing: {}", location);
            return empty(location.getPath());
        }
        try (stream) {
            CompoundTag data = NbtIo.readCompressed(stream);
            ListTag sizeTag = data.getList("size", Tag.TAG_INT);
            int sizeX = sizeTag.getInt(0);
            int sizeY = sizeTag.getInt(1);
            int sizeZ = sizeTag.getInt(2);

            ListTag paletteList = data.getList("palette", Tag.TAG_COMPOUND);
            PaletteEntry[] palette = new PaletteEntry[paletteList.size()];
            for (int i = 0; i < paletteList.size(); i++) {
                CompoundTag entry = paletteList.getCompound(i);
                String blockName = entry.getString("Name");
                int meta = 0;
                try {
                    meta = Integer.parseInt(entry.getCompound("Properties").getString("meta"));
                } catch (NumberFormatException ignored) {
                }
                palette[i] = new PaletteEntry(blockName, meta);
            }

            Cell[][][] cells = new Cell[sizeX][sizeY][sizeZ];
            List<JigsawConnection> connections = new ArrayList<>();
            Map<String, List<JigsawConnection>> toTop = new HashMap<>();
            Map<String, List<JigsawConnection>> toBottom = new HashMap<>();
            Map<String, List<JigsawConnection>> toHorizontal = new HashMap<>();
            boolean debug = StructureConfig.debugStructures.get();

            ListTag blockData = data.getList("blocks", Tag.TAG_COMPOUND);
            for (int i = 0; i < blockData.size(); i++) {
                CompoundTag block = blockData.getCompound(i);
                int state = block.getInt("state");
                ListTag posTag = block.getList("pos", Tag.TAG_INT);
                int x = posTag.getInt(0);
                int y = posTag.getInt(1);
                int z = posTag.getInt(2);
                if (x < 0 || y < 0 || z < 0 || x >= sizeX || y >= sizeY || z >= sizeZ) {
                    continue;
                }
                PaletteEntry definition = palette[state];
                CompoundTag nbt = block.contains("nbt", Tag.TAG_COMPOUND) ? block.getCompound("nbt").copy() : null;
                String id = StructureBlockNames.portId(definition.name, definition.meta);
                String pathOnly = pathOf(id);

                if (nbt != null && "wand_jigsaw".equals(pathOnly)) {
                    Direction direction = Direction.from3DDataValue(nbt.getInt("direction"));
                    JigsawConnection connection = new JigsawConnection(
                            new Vec3i(x, y, z),
                            direction,
                            nbt.getString("pool"),
                            nbt.getString("target"),
                            nbt.getBoolean("roll"),
                            nbt.getInt("selection"),
                            nbt.getInt("placement"));
                    connections.add(connection);
                    Map<String, List<JigsawConnection>> named = switch (direction) {
                        case UP -> toTop;
                        case DOWN -> toBottom;
                        default -> toHorizontal;
                    };
                    named.computeIfAbsent(nbt.getString("name"), n -> new ArrayList<>()).add(connection);
                    if (!debug) {
                        definition = new PaletteEntry(nbt.getString("block"), nbt.getInt("meta"));
                        nbt = null;
                    }
                } else if (nbt != null && "wand_tandem".equals(pathOnly) && !debug) {
                    definition = new PaletteEntry(nbt.getString("block"), nbt.getInt("meta"));
                    nbt = null;
                }

                CellKind kind = CellKind.BLOCK;
                if (nbt != null && "wand_loot".equals(pathOnly)) {
                    kind = CellKind.LOOT;
                } else if (nbt != null && "wand_logic".equals(pathOnly)) {
                    kind = CellKind.LOGIC;
                }
                cells[x][y][z] = new Cell(definition.name, definition.meta, nbt, kind);
            }

            List<List<JigsawConnection>> from = new ArrayList<>();
            if (!connections.isEmpty()) {
                connections.sort((a, b) -> Integer.compare(b.selectionPriority, a.selectionPriority));
                List<JigsawConnection> inner = null;
                int current = Integer.MIN_VALUE;
                for (JigsawConnection connection : connections) {
                    if (inner == null || current != connection.selectionPriority) {
                        inner = new ArrayList<>();
                        from.add(inner);
                        current = connection.selectionPriority;
                    }
                    inner.add(connection);
                }
            }
            return new NbtStructure(location.getPath(), sizeX, sizeY, sizeZ, cells, from, toTop, toBottom,
                    toHorizontal, true);
        } catch (Exception e) {
            HbmNuclearTechMod.LOGGER.error("Failed to read NBT structure {}", location, e);
            return empty(location.getPath());
        }
    }

    private static NbtStructure empty(String name) {
        return new NbtStructure(name, 1, 1, 1, new Cell[1][1][1], List.of(), Map.of(), Map.of(), Map.of(), false);
    }

    public List<JigsawConnection> getConnectionPool(Direction dir, String target) {
        if (dir == Direction.DOWN) {
            return toTopConnections.get(target);
        }
        if (dir == Direction.UP) {
            return toBottomConnections.get(target);
        }
        return toHorizontalConnections.get(target);
    }

    public int rotateX(int x, int z, int coordBaseMode) {
        return switch (coordBaseMode) {
            case 1 -> sizeZ - 1 - z;
            case 2 -> sizeX - 1 - x;
            case 3 -> z;
            default -> x;
        };
    }

    public int rotateZ(int x, int z, int coordBaseMode) {
        return switch (coordBaseMode) {
            case 1 -> x;
            case 2 -> sizeZ - 1 - z;
            case 3 -> sizeX - 1 - x;
            default -> z;
        };
    }

    public int unrotateX(int x, int z, int coordBaseMode) {
        return switch (coordBaseMode) {
            case 3 -> sizeX - 1 - z;
            case 2 -> sizeX - 1 - x;
            case 1 -> z;
            default -> x;
        };
    }

    public int unrotateZ(int x, int z, int coordBaseMode) {
        return switch (coordBaseMode) {
            case 3 -> x;
            case 2 -> sizeZ - 1 - z;
            case 1 -> sizeZ - 1 - x;
            default -> z;
        };
    }

    public boolean place(WorldGenLevel level, JigsawPiece piece, BoundingBox total, BoundingBox generating,
                         int coordBaseMode, RandomSource random) {
        if (!loaded) {
            return false;
        }
        int sizeBoxX = total.maxX() - total.minX();
        int sizeBoxZ = total.maxZ() - total.minZ();
        int absMinX = Math.max(generating.minX() - total.minX(), 0);
        int absMaxX = Math.min(generating.maxX() - total.minX(), sizeBoxX);
        int absMinZ = Math.max(generating.minZ() - total.minZ(), 0);
        int absMaxZ = Math.min(generating.maxZ() - total.minZ(), sizeBoxZ);
        if (absMinX > sizeBoxX || absMaxX < 0 || absMinZ > sizeBoxZ || absMaxZ < 0) {
            return true;
        }
        int minX = Math.min(unrotateX(absMinX, absMinZ, coordBaseMode), unrotateX(absMaxX, absMaxZ, coordBaseMode));
        int maxX = Math.max(unrotateX(absMinX, absMinZ, coordBaseMode), unrotateX(absMaxX, absMaxZ, coordBaseMode));
        int minZ = Math.min(unrotateZ(absMinX, absMinZ, coordBaseMode), unrotateZ(absMaxX, absMaxZ, coordBaseMode));
        int maxZ = Math.max(unrotateZ(absMinX, absMinZ, coordBaseMode), unrotateZ(absMaxX, absMaxZ, coordBaseMode));
        minX = Math.max(0, minX);
        maxX = Math.min(sizeX - 1, maxX);
        minZ = Math.max(0, minZ);
        maxZ = Math.min(sizeZ - 1, maxZ);

        for (int bx = minX; bx <= maxX; bx++) {
            for (int bz = minZ; bz <= maxZ; bz++) {
                int rx = rotateX(bx, bz, coordBaseMode) + total.minX();
                int rz = rotateZ(bx, bz, coordBaseMode) + total.minZ();
                int oy = piece.conformToTerrain
                        ? level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, rx, rz) + piece.heightOffset
                        : total.minY();
                for (int by = 0; by < sizeY; by++) {
                    Cell cell = cells[bx][by][bz];
                    if (cell == null) {
                        continue;
                    }
                    int ry = by + oy;
                    if (ry < level.getMinBuildHeight() + 1) {
                        continue;
                    }
                    BlockPos pos = new BlockPos(rx, ry, rz);
                    if (!generating.isInside(pos)) {
                        continue;
                    }
                    placeCell(level, pos, cell, piece.palette, coordBaseMode, random);
                }
            }
        }
        return true;
    }

    private static void placeCell(WorldGenLevel level, BlockPos pos, Cell cell, JigsawPiece.PaletteKind palette,
                                  int coordBaseMode, RandomSource random) {
        if (cell.kind == CellKind.LOGIC && !StructureConfig.debugStructures.get()) {
            String disguise = cell.nbt != null ? cell.nbt.getString("disguise") : "";
            if (disguise.isEmpty()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            } else {
                level.setBlock(pos, LegacyBlockPalette.toState(disguise, cell.nbt.getInt("disguiseMeta"), coordBaseMode), 2);
            }
            return;
        }
        if (cell.kind == CellKind.LOOT && !StructureConfig.debugStructures.get()) {
            String replace = cell.nbt != null ? cell.nbt.getString("block") : "minecraft:chest";
            int meta = cell.nbt != null ? cell.nbt.getInt("meta") : 0;
            BlockState state = LegacyBlockPalette.toState(replace, meta, coordBaseMode);
            if (state.isAir()) {
                state = Blocks.CHEST.defaultBlockState();
            }
            level.setBlock(pos, state, 2);
            fillLoot(level, pos, cell, random);
            return;
        }
        BlockState state = select(cell, palette, random, coordBaseMode);
        if (state.isAir() && cell.kind == CellKind.BLOCK && "minecraft:air".equals(StructureBlockNames.portId(cell.name))) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            return;
        }
        if (state.isAir()) {
            return;
        }
        level.setBlock(pos, state, 2);
        LegacyBlockPalette.stitchDoor(level, pos, state);
    }

    private static BlockState select(Cell cell, JigsawPiece.PaletteKind palette, RandomSource random, int coordBaseMode) {
        String id = StructureBlockNames.portId(cell.name, cell.meta);
        String path = pathOf(id);
        if (palette != JigsawPiece.PaletteKind.NONE && "meteor_brick".equals(path)) {
            float chance = random.nextFloat();
            if (chance < 0.4F) {
                return ModBlocks.METEOR_BRICK.get().defaultBlockState();
            }
            if (chance < 0.7F) {
                return ModBlocks.METEOR_BRICK_MOSSY.get().defaultBlockState();
            }
            return ModBlocks.METEOR_BRICK_CRACKED.get().defaultBlockState();
        }
        if (palette == JigsawPiece.PaletteKind.CRATES && "crate".equals(path)) {
            float chance = random.nextFloat();
            if (chance < 0.6F) {
                return Blocks.AIR.defaultBlockState();
            }
            if (chance < 0.8F) {
                return ModBlocks.CRATE_AMMO.get().defaultBlockState();
            }
            if (chance < 0.9F) {
                return ModBlocks.CRATE_CAN.get().defaultBlockState();
            }
            return ModBlocks.CRATE.get().defaultBlockState();
        }
        // 1.7 CrabSpawners live on the crates table (80% brick / 20% spawner).
        if (palette == JigsawPiece.PaletteKind.CRATES && "meteor_spawner".equals(path)) {
            if (random.nextFloat() < 0.8F) {
                return ModBlocks.METEOR_BRICK.get().defaultBlockState();
            }
            return ModBlocks.METEOR_SPAWNER.get().defaultBlockState();
        }
        if (palette == JigsawPiece.PaletteKind.OOZE && "concrete_colored".equals(path)) {
            if (random.nextFloat() < 0.8F) {
                return ModBlocks.TOXIC_BLOCK.get().defaultBlockState();
            }
            return ModBlocks.METEOR_POLISHED.get().defaultBlockState();
        }
        return LegacyBlockPalette.toState(cell.name, cell.meta, coordBaseMode);
    }

    private static void fillLoot(WorldGenLevel level, BlockPos pos, Cell cell, RandomSource random) {
        if (cell.nbt == null) {
            return;
        }
        String pool = cell.nbt.getString("pool");
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DecoLootBlockEntity loot) {
            // 1.7 TileEntityWandLoot uses LootGenerator.applyLoot and ignores min/max.
            loot.setItems(StructureLoot.decoLoot(pool, random));
            return;
        }
        if (be instanceof Container container) {
            int min = cell.nbt.getInt("min");
            int max = cell.nbt.getInt("max");
            int count = min;
            // 1.7: nextInt(maxItems - minItems), exclusive of max.
            if (max - min > 0) {
                count += random.nextInt(max - min);
            }
            List<ItemStack> stacks = StructureLoot.generate(pool, count, random);
            if (stacks.isEmpty()) {
                return;
            }
            int slots = container.getContainerSize();
            for (ItemStack stack : stacks) {
                for (int attempt = 0; attempt < slots; attempt++) {
                    int slot = random.nextInt(slots);
                    if (container.getItem(slot).isEmpty()) {
                        container.setItem(slot, stack);
                        break;
                    }
                }
            }
            be.setChanged();
        }
    }

    public static String pathOf(String id) {
        int colon = id.indexOf(':');
        return colon >= 0 ? id.substring(colon + 1) : id;
    }

    public enum CellKind {
        BLOCK,
        LOOT,
        LOGIC
    }

    public record Cell(String name, int meta, CompoundTag nbt, CellKind kind) {
    }

    private record PaletteEntry(String name, int meta) {
    }
}
