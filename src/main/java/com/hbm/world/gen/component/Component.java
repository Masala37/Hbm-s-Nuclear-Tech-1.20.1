package com.hbm.world.gen.component;

import com.hbm.blockentity.bomb.LandmineBlockEntity;
import com.hbm.blockentity.machine.LaunchPadRustedBlockEntity;
import com.hbm.blockentity.machine.SafeBlockEntity;
import com.hbm.blockentity.machine.StorageCrateBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.blocks.machine.DummyablePlacement;
import com.hbm.blocks.machine.LaunchPadBlock;
import com.hbm.config.StructureConfig;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.loot.StructureLoot;
import com.hbm.world.gen.nbt.LegacyBlockPalette;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.Random;

/**
 * 1.7 {@code Component} helpers on a 1.20 {@link StructurePiece}.
 * Positions use 1.7 {@code coordBaseMode} 0–3 (fixed-mirror), not vanilla Rotation.
 */
public abstract class Component extends StructurePiece {
    protected int hpos = -1;
    protected int coordBaseMode;

    protected Component(StructurePieceType type, Random rand, int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
        this(type, rand.nextInt(4), minX, minY, minZ, sizeX, sizeY, sizeZ);
    }

    protected Component(StructurePieceType type, int coordBaseMode, int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
        super(type, 0, boxFor(coordBaseMode, minX, minY, minZ, sizeX, sizeY, sizeZ));
        this.coordBaseMode = coordBaseMode;
        setOrientation(null);
    }

    protected Component(StructurePieceType type, CompoundTag tag) {
        super(type, tag);
        this.coordBaseMode = tag.getInt("cbm");
        this.hpos = tag.contains("HPos") ? tag.getInt("HPos") : -1;
        setOrientation(null);
    }

    private static BoundingBox boxFor(int mode, int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
        if (mode == 1 || mode == 3) {
            return new BoundingBox(minX, minY, minZ, minX + sizeZ, minY + sizeY, minZ + sizeX);
        }
        return new BoundingBox(minX, minY, minZ, minX + sizeX, minY + sizeY, minZ + sizeZ);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("cbm", coordBaseMode);
        tag.putInt("HPos", hpos);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        addComponentParts(level, javaRandom(random), box);
    }

    protected abstract boolean addComponentParts(WorldGenLevel world, Random rand, BoundingBox box);

    private static Random javaRandom(RandomSource source) {
        return new Random(source.nextLong());
    }

    protected static BlockState stateOf(String name, int meta) {
        return LegacyBlockPalette.toState(name, meta, 0);
    }

    protected static Block blockOf(String name) {
        Block block = stateOf(name, 0).getBlock();
        return block == null ? Blocks.AIR : block;
    }

    protected int getCoordMode() {
        return coordBaseMode;
    }

    @Override
    protected int getWorldX(int x, int z) {
        return getXWithOffset(x, z);
    }

    @Override
    protected int getWorldY(int y) {
        return getYWithOffset(y);
    }

    @Override
    protected int getWorldZ(int x, int z) {
        return getZWithOffset(x, z);
    }

    protected int getYWithOffset(int y) {
        return this.boundingBox.minY() + y;
    }

    /** 1.7 Component.getXWithOffset (East/North are mirrored, not rotated). */
    protected int getXWithOffset(int x, int z) {
        return switch (this.coordBaseMode) {
            case 0 -> this.boundingBox.minX() + x;
            case 1 -> this.boundingBox.maxX() - z;
            case 2 -> this.boundingBox.maxX() - x;
            case 3 -> this.boundingBox.minX() + z;
            default -> x;
        };
    }

    protected int getZWithOffset(int x, int z) {
        return switch (this.coordBaseMode) {
            case 0 -> this.boundingBox.minZ() + z;
            case 1 -> this.boundingBox.minZ() + x;
            case 2 -> this.boundingBox.maxZ() - z;
            case 3 -> this.boundingBox.maxZ() - x;
            default -> z;
        };
    }

    protected boolean inside(BoundingBox box, int x, int y, int z) {
        return box.isInside(x, y, z);
    }

    protected void setBlock(WorldGenLevel world, BoundingBox box, int featureX, int featureY, int featureZ, BlockState state) {
        int posX = getXWithOffset(featureX, featureZ);
        int posY = getYWithOffset(featureY);
        int posZ = getZWithOffset(featureX, featureZ);
        if (!inside(box, posX, posY, posZ)) {
            return;
        }
        world.setBlock(new BlockPos(posX, posY, posZ), state, 2);
    }

    protected void placeBlockAtCurrentPosition(WorldGenLevel world, String name, int meta, int x, int y, int z, BoundingBox box) {
        setBlock(world, box, x, y, z, stateOf(name, meta));
    }

    protected int getPillarMeta(int metadata) {
        if (this.coordBaseMode % 2 != 0 && this.coordBaseMode != -1) {
            metadata = metadata ^ 12;
        }
        return metadata;
    }

    protected int getDecoMeta(int metadata) {
        return switch (this.coordBaseMode) {
            case 1 -> switch (metadata) {
                case 2 -> 5;
                case 3 -> 4;
                case 4 -> 2;
                case 5 -> 3;
                default -> 0;
            };
            case 2 -> switch (metadata) {
                case 2 -> 3;
                case 3 -> 2;
                case 4 -> 5;
                case 5 -> 4;
                default -> 0;
            };
            case 3 -> switch (metadata) {
                case 2 -> 4;
                case 3 -> 5;
                case 4 -> 3;
                case 5 -> 2;
                default -> 0;
            };
            default -> metadata;
        };
    }

    protected int getDecoModelMeta(int metadata) {
        switch (this.coordBaseMode) {
            case 1 -> {
                if ((metadata & 3) < 2) {
                    metadata = metadata ^ 3;
                } else {
                    metadata = metadata ^ 2;
                }
            }
            case 2 -> metadata = metadata ^ 1;
            case 3 -> {
                if ((metadata & 3) < 2) {
                    metadata = metadata ^ 2;
                } else {
                    metadata = metadata ^ 3;
                }
            }
            default -> {
            }
        }
        return metadata << 2;
    }

    protected int getCRTMeta(int meta) {
        return (meta + this.coordBaseMode) % 4;
    }

    protected int getStairMeta(int metadata) {
        switch (this.coordBaseMode) {
            case 1 -> {
                if ((metadata & 3) < 2) {
                    metadata = metadata ^ 2;
                } else {
                    metadata = metadata ^ 3;
                }
            }
            case 2 -> metadata = metadata ^ 1;
            case 3 -> {
                if ((metadata & 3) < 2) {
                    metadata = metadata ^ 3;
                } else {
                    metadata = metadata ^ 2;
                }
            }
            default -> {
            }
        }
        return metadata;
    }

    protected void placeDoor(WorldGenLevel world, BoundingBox box, String door, int dirMeta, boolean opensRight,
                             boolean isOpen, int featureX, int featureY, int featureZ) {
        int posX = getXWithOffset(featureX, featureZ);
        int posY = getYWithOffset(featureY);
        int posZ = getZWithOffset(featureX, featureZ);
        if (!inside(box, posX, posY, posZ)) {
            return;
        }
        switch (this.coordBaseMode) {
            case 1 -> dirMeta = (dirMeta + 1) % 4;
            case 2 -> dirMeta ^= 2;
            case 3 -> dirMeta = (dirMeta + 3) % 4;
            default -> {
            }
        }
        int metaTop = opensRight ? 0b1001 : 0b1000;
        int metaBottom = dirMeta | (isOpen ? 0b100 : 0);
        BlockPos below = new BlockPos(posX, posY - 1, posZ);
        if (world.getBlockState(below).isFaceSturdy(world, below, Direction.UP)) {
            BlockPos lower = new BlockPos(posX, posY, posZ);
            BlockPos upper = new BlockPos(posX, posY + 1, posZ);
            world.setBlock(lower, stateOf(door, metaBottom), 2);
            world.setBlock(upper, stateOf(door, metaTop), 2);
            LegacyBlockPalette.stitchDoor(world, upper, world.getBlockState(upper));
        }
    }

    /** 1.7: 1 west face, 2 east, 3 north, 4 south. */
    protected void placeLever(WorldGenLevel world, BoundingBox box, int dirMeta, boolean on, int featureX, int featureY,
                              int featureZ) {
        int posX = getXWithOffset(featureX, featureZ);
        int posY = getYWithOffset(featureY);
        int posZ = getZWithOffset(featureX, featureZ);
        if (!inside(box, posX, posY, posZ)) {
            return;
        }
        if (dirMeta <= 0 || dirMeta >= 7) {
            if (this.coordBaseMode == 1 || this.coordBaseMode == 3) {
                dirMeta ^= 0b111;
            }
        } else if (dirMeta >= 5) {
            if (this.coordBaseMode == 1 || this.coordBaseMode == 3) {
                dirMeta = (dirMeta + 1) % 2 + 5;
            }
        } else {
            dirMeta = getButtonMeta(dirMeta);
        }
        world.setBlock(new BlockPos(posX, posY, posZ), leverState(on ? dirMeta | 8 : dirMeta), 2);
    }

    protected int getButtonMeta(int dirMeta) {
        return switch (this.coordBaseMode) {
            case 1 -> {
                if (dirMeta <= 2) {
                    yield dirMeta + 2;
                } else if (dirMeta < 4) {
                    yield dirMeta - 1;
                }
                yield dirMeta - 3;
            }
            case 2 -> dirMeta + (dirMeta % 2 == 0 ? -1 : 1);
            case 3 -> {
                if (dirMeta <= 1) {
                    yield dirMeta + 3;
                } else if (dirMeta <= 2) {
                    yield dirMeta + 1;
                }
                yield dirMeta - 2;
            }
            default -> dirMeta;
        };
    }

    private static BlockState leverState(int meta) {
        boolean powered = (meta & 8) != 0;
        AttachFace face;
        Direction facing;
        switch (meta & 7) {
            case 1 -> {
                face = AttachFace.WALL;
                facing = Direction.EAST;
            }
            case 2 -> {
                face = AttachFace.WALL;
                facing = Direction.WEST;
            }
            case 3 -> {
                face = AttachFace.WALL;
                facing = Direction.SOUTH;
            }
            case 4 -> {
                face = AttachFace.WALL;
                facing = Direction.NORTH;
            }
            case 6 -> {
                face = AttachFace.FLOOR;
                facing = Direction.EAST;
            }
            case 0 -> {
                face = AttachFace.CEILING;
                facing = Direction.EAST;
            }
            case 7 -> {
                face = AttachFace.CEILING;
                facing = Direction.SOUTH;
            }
            default -> {
                face = AttachFace.FLOOR;
                facing = Direction.SOUTH;
            }
        }
        return Blocks.LEVER.defaultBlockState()
                .setValue(LeverBlock.FACE, face)
                .setValue(LeverBlock.FACING, facing)
                .setValue(LeverBlock.POWERED, powered);
    }

    /** 1.7: N:0 W:1 S:2 E:3 for the foot, head offset in feature space. */
    protected void placeBed(WorldGenLevel world, BoundingBox box, int meta, int featureX, int featureY, int featureZ) {
        int xOffset = 0;
        int zOffset = 0;
        switch (meta & 3) {
            case 1 -> xOffset = -1;
            case 2 -> zOffset = -1;
            case 3 -> xOffset = 1;
            default -> zOffset = 1;
        }
        switch (this.coordBaseMode) {
            case 1 -> meta = (meta + 1) % 4;
            case 2 -> meta ^= 2;
            case 3 -> meta = Math.floorMod(meta - 1, 4);
            default -> {
            }
        }
        Direction facing = switch (meta & 3) {
            case 1 -> Direction.WEST;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.EAST;
            default -> Direction.SOUTH;
        };
        BlockState foot = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.FOOT);
        setBlock(world, box, featureX, featureY, featureZ, foot);
        setBlock(world, box, featureX + xOffset, featureY, featureZ + zOffset,
                foot.setValue(BedBlock.PART, BedPart.HEAD));
    }

    protected void placeRandomBobble(WorldGenLevel world, BoundingBox box, Random rand, int featureX, int featureY,
                                     int featureZ) {
        placeBlockAtCurrentPosition(world, "hbm:tile.bobblehead", rand.nextInt(16), featureX, featureY, featureZ, box);
    }

    protected boolean generateInvContents(WorldGenLevel world, BoundingBox box, Random rand, String block, int featureX,
                                          int featureY, int featureZ, String pool, int amount) {
        return generateInvContents(world, box, rand, block, 0, featureX, featureY, featureZ, pool, amount);
    }

    protected boolean generateInvContents(WorldGenLevel world, BoundingBox box, Random rand, String block, int meta,
                                          int featureX, int featureY, int featureZ, String pool, int amount) {
        int posX = getXWithOffset(featureX, featureZ);
        int posY = getYWithOffset(featureY);
        int posZ = getZWithOffset(featureX, featureZ);
        if (!inside(box, posX, posY, posZ)) {
            return true;
        }
        BlockPos pos = new BlockPos(posX, posY, posZ);
        Block want = blockOf(block);
        if (world.getBlockState(pos).is(want) && want != Blocks.AIR) {
            return true;
        }
        placeBlockAtCurrentPosition(world, block, meta, featureX, featureY, featureZ, box);
        amount = (int) Math.floor(amount * StructureConfig.lootAmountFactor.get());
        fillLoot(world.getBlockEntity(pos), StructureLoot.generate(pool, amount < 1 ? 1 : amount, rand), rand);
        return true;
    }

    protected boolean generateLockableContents(WorldGenLevel world, BoundingBox box, Random rand, String block, int meta,
                                               int featureX, int featureY, int featureZ, String pool, int amount, double mod) {
        int posX = getXWithOffset(featureX, featureZ);
        int posY = getYWithOffset(featureY);
        int posZ = getZWithOffset(featureX, featureZ);
        if (!inside(box, posX, posY, posZ)) {
            return false;
        }
        BlockPos pos = new BlockPos(posX, posY, posZ);
        Block want = blockOf(block);
        if (world.getBlockState(pos).is(want) && want != Blocks.AIR) {
            return false;
        }
        placeBlockAtCurrentPosition(world, block, meta, featureX, featureY, featureZ, box);
        amount = (int) Math.floor(amount * StructureConfig.lootAmountFactor.get());
        fillLoot(world.getBlockEntity(pos), StructureLoot.generate(pool, amount < 1 ? 1 : amount, rand), rand);
        return true;
    }

    private static void fillLoot(BlockEntity be, List<ItemStack> stacks, Random rand) {
        if (be == null || stacks == null || stacks.isEmpty()) {
            return;
        }
        if (be instanceof Container container) {
            int slots = container.getContainerSize();
            for (ItemStack stack : stacks) {
                for (int attempt = 0; attempt < slots; attempt++) {
                    int slot = rand.nextInt(slots);
                    if (container.getItem(slot).isEmpty()) {
                        container.setItem(slot, stack);
                        break;
                    }
                }
            }
            be.setChanged();
            return;
        }
        ItemStackHandler handler = null;
        if (be instanceof StorageCrateBlockEntity crate) {
            handler = crate.getItems();
        } else if (be instanceof SafeBlockEntity safe) {
            handler = safe.getItems();
        }
        if (handler == null) {
            return;
        }
        int slots = handler.getSlots();
        for (ItemStack stack : stacks) {
            for (int attempt = 0; attempt < slots; attempt++) {
                int slot = rand.nextInt(slots);
                if (handler.getStackInSlot(slot).isEmpty()) {
                    handler.setStackInSlot(slot, stack);
                    break;
                }
            }
        }
        be.setChanged();
    }

    protected void placeFoundationUnderneath(WorldGenLevel world, String name, int meta, int minX, int minZ, int maxX,
                                             int maxZ, int featureY, BoundingBox box) {
        BlockState placed = stateOf(name, meta);
        for (int featureX = minX; featureX <= maxX; featureX++) {
            for (int featureZ = minZ; featureZ <= maxZ; featureZ++) {
                int posX = getXWithOffset(featureX, featureZ);
                int posY = getYWithOffset(featureY);
                int posZ = getZWithOffset(featureX, featureZ);
                if (!inside(box, posX, posY, posZ)) {
                    continue;
                }
                int brake = 0;
                while (posY > 1 && brake <= 15) {
                    BlockPos pos = new BlockPos(posX, posY, posZ);
                    BlockState at = world.getBlockState(pos);
                    if (!replaceableFoundation(at)) {
                        break;
                    }
                    world.setBlock(pos, placed, 2);
                    posY--;
                    brake++;
                }
            }
        }
    }

    private static boolean replaceableFoundation(BlockState state) {
        return state.isAir() || state.liquid() || !state.blocksMotion()
                || state.canBeReplaced() || state.is(BlockTags.LEAVES);
    }

    protected boolean yInBox(BoundingBox box, int minY, int maxY) {
        int a = getYWithOffset(minY);
        int b = getYWithOffset(maxY);
        return a >= box.minY() && b <= box.maxY();
    }

    protected void fillWithAir(WorldGenLevel world, BoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        fillWithBlocks(world, box, minX, minY, minZ, maxX, maxY, maxZ, "minecraft:air");
    }

    protected void fillWithBlocks(WorldGenLevel world, BoundingBox box, int minX, int minY, int minZ, int maxX, int maxY,
                                  int maxZ, String name) {
        fillWithMetadataBlocks(world, box, minX, minY, minZ, maxX, maxY, maxZ, name, 0);
    }

    protected void fillWithBlocks(WorldGenLevel world, BoundingBox box, int minX, int minY, int minZ, int maxX, int maxY,
                                  int maxZ, String name, String unusedReplace, boolean unusedAlways) {
        fillWithBlocks(world, box, minX, minY, minZ, maxX, maxY, maxZ, name);
    }

    protected void fillWithMetadataBlocks(WorldGenLevel world, BoundingBox box, int minX, int minY, int minZ, int maxX,
                                          int maxY, int maxZ, String name, int meta) {
        if (!yInBox(box, minY, maxY)) {
            return;
        }
        BlockState state = stateOf(name, meta);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int posX = getXWithOffset(x, z);
                int posZ = getZWithOffset(x, z);
                if (posX < box.minX() || posX > box.maxX() || posZ < box.minZ() || posZ > box.maxZ()) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    int posY = getYWithOffset(y);
                    world.setBlock(new BlockPos(posX, posY, posZ), state, 2);
                }
            }
        }
    }

    protected void fillWithMetadataBlocks(WorldGenLevel world, BoundingBox box, int minX, int minY, int minZ, int maxX,
                                          int maxY, int maxZ, String name, int meta, String unusedReplace,
                                          int unusedReplaceMeta, boolean unusedAlways) {
        fillWithMetadataBlocks(world, box, minX, minY, minZ, maxX, maxY, maxZ, name, meta);
    }

    protected void fillWithRandomizedBlocks(WorldGenLevel world, BoundingBox box, int minX, int minY, int minZ, int maxX,
                                            int maxY, int maxZ, boolean unusedAlways, Random rand, BlockSelector selector) {
        fillWithRandomizedBlocks(world, box, minX, minY, minZ, maxX, maxY, maxZ, rand, selector);
    }

    protected void fillWithRandomizedBlocks(WorldGenLevel world, BoundingBox box, int minX, int minY, int minZ, int maxX,
                                            int maxY, int maxZ, Random rand, BlockSelector selector) {
        if (!yInBox(box, minY, maxY)) {
            return;
        }
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int posX = getXWithOffset(x, z);
                int posZ = getZWithOffset(x, z);
                if (posX < box.minX() || posX > box.maxX() || posZ < box.minZ() || posZ > box.maxZ()) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    int posY = getYWithOffset(y);
                    selector.selectBlocks(rand, posX, posY, posZ, false);
                    world.setBlock(new BlockPos(posX, posY, posZ), selector.state(), 2);
                }
            }
        }
    }

    protected void randomlyFillWithBlocks(WorldGenLevel world, BoundingBox box, Random rand, float randLimit, int minX,
                                          int minY, int minZ, int maxX, int maxY, int maxZ, String name) {
        randomlyFillWithBlocks(world, box, rand, randLimit, minX, minY, minZ, maxX, maxY, maxZ, name, 0);
    }

    protected void randomlyFillWithBlocks(WorldGenLevel world, BoundingBox box, Random rand, float randLimit, int minX,
                                          int minY, int minZ, int maxX, int maxY, int maxZ, String name, String unusedReplace,
                                          boolean unusedAlways) {
        randomlyFillWithBlocks(world, box, rand, randLimit, minX, minY, minZ, maxX, maxY, maxZ, name, 0);
    }

    protected void randomlyFillWithBlocks(WorldGenLevel world, BoundingBox box, Random rand, float randLimit, int minX,
                                          int minY, int minZ, int maxX, int maxY, int maxZ, String name, int meta,
                                          String unusedReplace, boolean unusedAlways) {
        randomlyFillWithBlocks(world, box, rand, randLimit, minX, minY, minZ, maxX, maxY, maxZ, name, meta);
    }

    protected void randomlyFillWithBlocks(WorldGenLevel world, BoundingBox box, Random rand, float randLimit, int minX,
                                          int minY, int minZ, int maxX, int maxY, int maxZ, String name, int meta) {
        if (!yInBox(box, minY, maxY)) {
            return;
        }
        BlockState state = stateOf(name, meta);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int posX = getXWithOffset(x, z);
                int posZ = getZWithOffset(x, z);
                if (posX < box.minX() || posX > box.maxX() || posZ < box.minZ() || posZ > box.maxZ()) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    if (rand.nextFloat() <= randLimit) {
                        world.setBlock(new BlockPos(posX, getYWithOffset(y), posZ), state, 2);
                    }
                }
            }
        }
    }

    protected Direction getDirection(Direction dir) {
        if (dir == null) {
            return Direction.NORTH;
        }
        return switch (coordBaseMode) {
            case 1 -> dir.getClockWise();
            case 2 -> dir.getOpposite();
            case 3 -> dir.getCounterClockWise();
            default -> dir;
        };
    }

    protected void placeCore(WorldGenLevel world, BoundingBox box, String name, Direction dir, int x, int y, int z) {
        int posX = getXWithOffset(x, z);
        int posY = getYWithOffset(y);
        int posZ = getZWithOffset(x, z);
        if (!inside(box, posX, posY, posZ)) {
            return;
        }
        if (dir == null) {
            dir = Direction.NORTH;
        }
        dir = getDirection(dir.getOpposite());
        BlockPos pos = new BlockPos(posX, posY, posZ);
        Block block = blockOf(name);
        if (block == Blocks.AIR) {
            return;
        }
        if (block instanceof LaunchPadBlock pad) {
            DummyablePlacement.begin();
            try {
                world.setBlock(pos, pad.defaultBlockState()
                        .setValue(LaunchPadBlock.OX, com.hbm.blocks.machine.LaunchPadOffsets.CORE)
                        .setValue(LaunchPadBlock.OZ, com.hbm.blocks.machine.LaunchPadOffsets.CORE), 2);
                LaunchPadBlock.fillStructure(world, pos, pad);
            } finally {
                DummyablePlacement.end();
            }
            if (world.getBlockEntity(pos) instanceof LaunchPadRustedBlockEntity rusted) {
                rusted.setFacing(dir);
            }
            return;
        }
        if (block instanceof BlockDummyable) {
            DummyablePlacement.begin();
            try {
                world.setBlock(pos, block.defaultBlockState()
                        .setValue(BlockDummyable.META, DummyableMeta.coreMeta(dir.get3DDataValue())), 2);
            } finally {
                DummyablePlacement.end();
            }
            return;
        }
        world.setBlock(pos, block.defaultBlockState(), 2);
    }

    protected void fillSpace(WorldGenLevel world, BoundingBox box, int x, int y, int z, int[] dim, String name, Direction dir) {
        if (!yInBox(box, y - dim[1], y + dim[0])) {
            return;
        }
        if (dir == null) {
            dir = Direction.NORTH;
        }
        dir = getDirection(dir.getOpposite());
        Block block = blockOf(name);
        if (block == Blocks.AIR || block instanceof LaunchPadBlock || !(block instanceof BlockDummyable dummyable)) {
            return;
        }
        int posX = getXWithOffset(x, z);
        int posY = getYWithOffset(y);
        int posZ = getZWithOffset(x, z);
        DummyablePlacement.begin();
        try {
            for (MultiblockHandlerXR.Cell cell : MultiblockHandlerXR.dummyCells(posX, posY, posZ, dim, dir.get3DDataValue())) {
                if (!inside(box, cell.x(), cell.y(), cell.z())) {
                    continue;
                }
                world.setBlock(new BlockPos(cell.x(), cell.y(), cell.z()),
                        dummyable.defaultBlockState().setValue(BlockDummyable.META, cell.meta()), 2);
            }
        } finally {
            DummyablePlacement.end();
        }
    }

    public void makeExtra(WorldGenLevel world, BoundingBox box, String name, int x, int y, int z) {
        int posX = getXWithOffset(x, z);
        int posY = getYWithOffset(y);
        int posZ = getZWithOffset(x, z);
        if (!inside(box, posX, posY, posZ)) {
            return;
        }
        BlockPos pos = new BlockPos(posX, posY, posZ);
        BlockState state = world.getBlockState(pos);
        Block block = blockOf(name);
        if (block instanceof LaunchPadBlock || !state.is(block) || !(block instanceof BlockDummyable)) {
            return;
        }
        int meta = state.getValue(BlockDummyable.META);
        if (meta > 5) {
            return;
        }
        DummyablePlacement.begin();
        try {
            world.setBlock(pos, state.setValue(BlockDummyable.META, DummyableMeta.makeExtra(meta)), 3);
        } finally {
            DummyablePlacement.end();
        }
    }

    protected void placeCoreLaunchpad(WorldGenLevel world, BoundingBox box, String name, Direction dir, int x, int y, int z) {
        placeCore(world, box, name, dir, x, y, z);
        int posX = getXWithOffset(x, z);
        int posY = getYWithOffset(y);
        int posZ = getZWithOffset(x, z);
        if (!inside(box, posX, posY, posZ)) {
            return;
        }
        if (world.getBlockEntity(new BlockPos(posX, posY, posZ)) instanceof LaunchPadRustedBlockEntity rusted) {
            rusted.setMissileLoaded(true);
        }
    }

    protected void fillWithMines(WorldGenLevel world, BoundingBox box, Random rand, int minX, int minY, int minZ,
                                 int maxX, int maxY, int maxZ) {
        if (!yInBox(box, minY, maxY)) {
            return;
        }
        BlockState mine = stateOf("hbm:tile.mine_ap", 0);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int posX = getXWithOffset(x, z);
                int posZ = getZWithOffset(x, z);
                if (posX < box.minX() || posX > box.maxX() || posZ < box.minZ() || posZ > box.maxZ()) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    int posY = getYWithOffset(y);
                    BlockPos pos = new BlockPos(posX, posY, posZ);
                    if (rand.nextInt(15) == 0 && world.getBlockState(pos).isAir()
                            && !world.getBlockState(pos.below()).isAir()) {
                        world.setBlock(pos, mine, 2);
                        if (world.getBlockEntity(pos) instanceof LandmineBlockEntity landmine) {
                            landmine.setWaitingForPlayer(true);
                        }
                    }
                }
            }
        }
    }

    protected void setRTTYFreq(WorldGenLevel world, BoundingBox box, int featureX, int featureY, int featureZ, int freq) {
        // Radio torches are not in this port; keep the call so RNG / layout stay 1.7.
    }

    protected int getAverageHeight(WorldGenLevel world, BoundingBox area, BoundingBox box, int y) {
        int total = 0;
        int iterations = 0;
        for (int z = area.minZ(); z <= area.maxZ(); z++) {
            for (int x = area.minX(); x <= area.maxX(); x++) {
                if (box.isInside(x, y, z)) {
                    total += Math.max(world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), 64);
                    iterations++;
                }
            }
        }
        if (iterations == 0) {
            return -1;
        }
        return total / iterations;
    }

    /** 1.7 {@code setAverageHeight}: sit the piece on mean surface height. */
    protected boolean setAverageHeight(WorldGenLevel world, BoundingBox box, int y) {
        if (this.hpos >= 0) {
            return true;
        }
        int total = 0;
        int iterations = 0;
        for (int z = this.boundingBox.minZ(); z <= this.boundingBox.maxZ(); z++) {
            for (int x = this.boundingBox.minX(); x <= this.boundingBox.maxX(); x++) {
                if (box.isInside(x, y, z)) {
                    total += Math.max(world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), 64);
                    iterations++;
                }
            }
        }
        if (iterations == 0) {
            return false;
        }
        this.hpos = total / iterations;
        this.boundingBox.move(0, this.hpos - this.boundingBox.minY(), 0);
        return true;
    }

    protected BlockState getBlockAtCurrentPosition(WorldGenLevel world, int x, int y, int z, BoundingBox box) {
        int posX = getXWithOffset(x, z);
        int posY = getYWithOffset(y);
        int posZ = getZWithOffset(x, z);
        if (!inside(box, posX, posY, posZ)) {
            return Blocks.AIR.defaultBlockState();
        }
        return world.getBlockState(new BlockPos(posX, posY, posZ));
    }

    protected void generateLoreBook(WorldGenLevel world, BoundingBox box, int featureX, int featureY, int featureZ,
                                    int slot, ItemStack stack) {
        int posX = getXWithOffset(featureX, featureZ);
        int posY = getYWithOffset(featureY);
        int posZ = getZWithOffset(featureX, featureZ);
        if (!inside(box, posX, posY, posZ) || stack == null || stack.isEmpty()) {
            return;
        }
        BlockEntity be = world.getBlockEntity(new BlockPos(posX, posY, posZ));
        if (be instanceof Container container && slot >= 0 && slot < container.getContainerSize()) {
            container.setItem(slot, stack);
            be.setChanged();
        }
    }

    protected BoundingBox getRotatedBoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (this.coordBaseMode == 1 || this.coordBaseMode == 3) {
            return new BoundingBox(minX, minY, minZ, minX + maxZ, minY + maxY, minZ + maxX);
        }
        return new BoundingBox(minX, minY, minZ, minX + maxX, minY + maxY, minZ + maxZ);
    }

    public abstract static class BlockSelector {
        protected String blockName = "minecraft:air";
        protected int selectedBlockMetaData;

        public abstract void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior);

        public BlockState state() {
            return stateOf(blockName, selectedBlockMetaData);
        }
    }

    public static class ConcreteBricks extends BlockSelector {
        @Override
        public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
            float chance = rand.nextFloat();
            if (chance < 0.4F) {
                this.blockName = "hbm:tile.brick_concrete";
            } else if (chance < 0.7F) {
                this.blockName = "hbm:tile.brick_concrete_mossy";
            } else if (chance < 0.9F) {
                this.blockName = "hbm:tile.brick_concrete_cracked";
            } else {
                this.blockName = "hbm:tile.brick_concrete_broken";
            }
        }
    }

    public static class Sandstone extends BlockSelector {
        @Override
        public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
            float chance = rand.nextFloat();
            if (chance > 0.6F) {
                this.blockName = "minecraft:sandstone";
            } else if (chance < 0.5F) {
                this.blockName = "hbm:tile.reinforced_sand";
            } else {
                this.blockName = "minecraft:sand";
            }
        }
    }

    public static class LabTiles extends BlockSelector {
        @Override
        public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
            float chance = rand.nextFloat();
            if (chance < 0.5F) {
                this.blockName = "hbm:tile.tile_lab";
            } else if (chance < 0.9F) {
                this.blockName = "hbm:tile.tile_lab_cracked";
            } else {
                this.blockName = "hbm:tile.tile_lab_broken";
            }
        }
    }

    public static class SuperConcrete extends BlockSelector {
        public SuperConcrete() {
            this.blockName = "hbm:tile.concrete_super";
        }

        @Override
        public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
            this.selectedBlockMetaData = rand.nextInt(6) + 10;
        }
    }
}
