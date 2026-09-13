package com.hbm.world.gen.nbt;

import com.hbm.blocks.generic.ConcreteExtBlock;
import com.hbm.blocks.generic.ColoredConcreteBlock;
import com.hbm.blocks.generic.DecoObjBlock;
import com.hbm.blocks.generic.DeadPlantBlock;
import com.hbm.blocks.generic.PlantFlowerBlock;
import com.hbm.blocks.generic.FileCabinetBlock;
import com.hbm.blocks.generic.LightstoneBlock;
import com.hbm.blocks.generic.SteelGrateBlock;
import com.hbm.blocks.generic.StructureMultiSlabBlock;
import com.hbm.blocks.generic.WoodStructureBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 1.7 palette Name + meta → 1.20 {@link BlockState}.
 * Rotates with 1.7 {@code INBTBlockTransformable} meta, not vanilla {@link Rotation}
 * (vanilla rotate on top of already-mapped facing double-spins deco and breaks doors).
 */
public final class LegacyBlockPalette {
    private LegacyBlockPalette() {
    }

    public static Rotation rotation(int coordBaseMode) {
        return switch (coordBaseMode & 3) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public static BlockState toState(String rawName, int meta, int coordBaseMode) {
        String id = StructureBlockNames.portId(rawName, meta);
        ResourceLocation key = ResourceLocation.tryParse(id);
        Block block = key == null ? Blocks.AIR : ForgeRegistries.BLOCKS.getValue(key);
        if (block == null || block == Blocks.AIR) {
            return Blocks.AIR.defaultBlockState();
        }
        block = wallTorchIfNeeded(block, meta);
        int rotatedMeta = transformLegacyMeta(block, rawName, meta, coordBaseMode);
        return applyMeta(block, rawName, rotatedMeta);
    }

    /**
     * 1.20 stores facing on both door halves; 1.7 only stored it on the lower block.
     * Copy facing/open from the lower half and hinge onto the lower half once both exist.
     * Runs for whichever half was just placed so Y-order and chunk splits still stitch.
     */
    public static void stitchDoor(WorldGenLevel level, BlockPos pos, BlockState placed) {
        if (!(placed.getBlock() instanceof DoorBlock) || !placed.hasProperty(DoorBlock.HALF)) {
            return;
        }
        BlockPos upperPos;
        BlockState upper;
        BlockState lower;
        if (placed.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            upperPos = pos;
            upper = placed;
            lower = level.getBlockState(pos.below());
        } else {
            upperPos = pos.above();
            upper = level.getBlockState(upperPos);
            lower = placed;
        }
        if (!lower.is(placed.getBlock()) || !upper.is(placed.getBlock())
                || !lower.hasProperty(DoorBlock.HALF) || !upper.hasProperty(DoorBlock.HALF)
                || lower.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER
                || upper.getValue(DoorBlock.HALF) != DoubleBlockHalf.UPPER) {
            return;
        }
        BlockState stitchedUpper = upper
                .setValue(DoorBlock.FACING, lower.getValue(DoorBlock.FACING))
                .setValue(DoorBlock.OPEN, lower.getValue(DoorBlock.OPEN));
        BlockState stitchedLower = lower.setValue(DoorBlock.HINGE, upper.getValue(DoorBlock.HINGE));
        if (!stitchedUpper.equals(upper)) {
            level.setBlock(upperPos, stitchedUpper, 2);
        }
        if (!stitchedLower.equals(lower)) {
            level.setBlock(upperPos.below(), stitchedLower, 2);
        }
    }

    private static Block wallTorchIfNeeded(Block block, int meta) {
        if (meta < 1 || meta > 4) {
            return block;
        }
        if (block == Blocks.TORCH) {
            return Blocks.WALL_TORCH;
        }
        if (block == Blocks.REDSTONE_TORCH) {
            return Blocks.REDSTONE_WALL_TORCH;
        }
        if (block == Blocks.SOUL_TORCH) {
            return Blocks.SOUL_WALL_TORCH;
        }
        return block;
    }

    private static int transformLegacyMeta(Block block, String rawName, int meta, int coordBaseMode) {
        int mode = coordBaseMode & 3;
        if (mode == 0) {
            return meta;
        }
        String path = palettePath(rawName);
        if (block instanceof DoorBlock) {
            return LegacyMetaTransform.door(meta, mode);
        }
        if (block instanceof StairBlock) {
            return LegacyMetaTransform.stairs(meta, mode);
        }
        if (block instanceof TrapDoorBlock) {
            return LegacyMetaTransform.trapdoor(meta, mode);
        }
        if (block instanceof RotatedPillarBlock) {
            return LegacyMetaTransform.pillar(meta, mode);
        }
        if (block instanceof FileCabinetBlock
                || (block instanceof DecoObjBlock deco && deco.usesDecoModelMeta())) {
            return LegacyMetaTransform.decoModelHigh(meta, mode);
        }
        if (path.equals("deco_crt") || path.equals("deco_toaster")
                || path.startsWith("crt_") || path.startsWith("toaster_")) {
            return LegacyMetaTransform.decoModelLow(meta, mode);
        }
        if (block instanceof LeverBlock) {
            return LegacyMetaTransform.lever(meta, mode);
        }
        if (block instanceof VineBlock) {
            return LegacyMetaTransform.vine(meta, mode);
        }
        if (block instanceof WallTorchBlock || block instanceof ButtonBlock) {
            return LegacyMetaTransform.torch(meta, mode);
        }
        if (block instanceof LadderBlock || block instanceof WallSignBlock) {
            return LegacyMetaTransform.deco(meta, mode);
        }
        if (block instanceof HorizontalDirectionalBlock) {
            return LegacyMetaTransform.directional(meta, mode);
        }
        BlockState def = block.defaultBlockState();
        if (def.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                || def.hasProperty(BlockStateProperties.FACING)) {
            return LegacyMetaTransform.deco(meta, mode);
        }
        return meta;
    }

    private static BlockState applyMeta(Block block, String rawName, int meta) {
        BlockState state = block.defaultBlockState();
        String path = palettePath(rawName);

        if (block instanceof FileCabinetBlock) {
            return block.defaultBlockState()
                    .setValue(FileCabinetBlock.FACING, FileCabinetBlock.decoModelFacing(meta >> 2))
                    .setValue(FileCabinetBlock.STEEL, (meta & 3) == 1);
        }
        if (block instanceof DecoObjBlock deco && deco.usesDecoModelMeta()
                && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, FileCabinetBlock.decoModelFacing(meta >> 2));
        }
        if ((path.equals("deco_crt") || path.equals("deco_toaster") || path.startsWith("crt_") || path.startsWith("toaster_"))
                && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                    Direction.from2DDataValue(LegacyMetaTransform.crtScreen2d(meta)));
        }
        if (block instanceof StructureMultiSlabBlock slab) {
            return slab.fromMeta(meta, path.contains("double_slab") || path.startsWith("double_"));
        }
        if (block instanceof ColoredConcreteBlock && state.hasProperty(ColoredConcreteBlock.COLOR)) {
            return state.setValue(ColoredConcreteBlock.COLOR, ColoredConcreteBlock.fromMeta(meta));
        }
        if (block instanceof LightstoneBlock && state.hasProperty(LightstoneBlock.VARIANT)) {
            return state.setValue(LightstoneBlock.VARIANT, LightstoneBlock.Variant.byMeta(meta));
        }
        if (block instanceof ConcreteExtBlock && state.hasProperty(ConcreteExtBlock.VARIANT)) {
            return state.setValue(ConcreteExtBlock.VARIANT, Mth.clamp(meta, 0, 7));
        }
        if (block instanceof DeadPlantBlock && state.hasProperty(DeadPlantBlock.VARIANT)) {
            return state.setValue(DeadPlantBlock.VARIANT, DeadPlantBlock.Variant.byMeta(meta));
        }
        if (block instanceof PlantFlowerBlock && state.hasProperty(PlantFlowerBlock.VARIANT)) {
            return state.setValue(PlantFlowerBlock.VARIANT, PlantFlowerBlock.Variant.byMeta(meta));
        }
        if (block instanceof WoodStructureBlock && state.hasProperty(WoodStructureBlock.VARIANT)) {
            return state.setValue(WoodStructureBlock.VARIANT, WoodStructureBlock.Variant.byMeta(meta));
        }
        if (block instanceof SteelGrateBlock && state.hasProperty(SteelGrateBlock.OFFSET)) {
            return state.setValue(SteelGrateBlock.OFFSET, Mth.clamp(meta, 0, 9));
        }
        if (block instanceof DoorBlock && state.hasProperty(DoorBlock.FACING)) {
            boolean upper = (meta & 8) != 0;
            if (upper) {
                state = state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                        .setValue(DoorBlock.HINGE, (meta & 1) != 0 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT);
                return state;
            }
            Direction facing = switch (meta & 3) {
                case 0 -> Direction.EAST;
                case 1 -> Direction.SOUTH;
                case 2 -> Direction.WEST;
                default -> Direction.NORTH;
            };
            return state.setValue(DoorBlock.FACING, facing)
                    .setValue(DoorBlock.OPEN, (meta & 4) != 0)
                    .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        }

        if (block instanceof StairBlock && state.hasProperty(StairBlock.FACING)) {
            Direction facing = switch (meta & 3) {
                case 0 -> Direction.EAST;
                case 1 -> Direction.WEST;
                case 2 -> Direction.SOUTH;
                default -> Direction.NORTH;
            };
            state = state.setValue(StairBlock.FACING, facing);
            if (state.hasProperty(StairBlock.HALF)) {
                state = state.setValue(StairBlock.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM);
            }
            return state;
        }
        if (block instanceof SlabBlock && state.hasProperty(SlabBlock.TYPE)) {
            boolean top = (meta & 8) != 0 || path.startsWith("double_");
            state = state.setValue(SlabBlock.TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
            if (path.startsWith("double_")) {
                state = state.setValue(SlabBlock.TYPE, SlabType.DOUBLE);
            }
            return state;
        }
        if (block instanceof RotatedPillarBlock && state.hasProperty(RotatedPillarBlock.AXIS)) {
            Direction.Axis axis = switch ((meta >> 2) & 3) {
                case 1 -> Direction.Axis.X;
                case 2 -> Direction.Axis.Z;
                default -> Direction.Axis.Y;
            };
            return state.setValue(RotatedPillarBlock.AXIS, axis);
        }
        if (block instanceof ChestBlock && state.hasProperty(ChestBlock.FACING)) {
            return state.setValue(ChestBlock.FACING, furnaceFacing(meta));
        }
        if (block instanceof LadderBlock && state.hasProperty(LadderBlock.FACING)) {
            return state.setValue(LadderBlock.FACING, furnaceFacing(meta));
        }
        if (block instanceof TrapDoorBlock && state.hasProperty(TrapDoorBlock.FACING)) {
            Direction facing = switch (meta & 3) {
                case 0 -> Direction.SOUTH;
                case 1 -> Direction.NORTH;
                case 2 -> Direction.EAST;
                default -> Direction.WEST;
            };
            state = state.setValue(TrapDoorBlock.FACING, facing);
            if (state.hasProperty(TrapDoorBlock.HALF)) {
                state = state.setValue(TrapDoorBlock.HALF, (meta & 8) != 0 ? Half.TOP : Half.BOTTOM);
            }
            if (state.hasProperty(TrapDoorBlock.OPEN)) {
                state = state.setValue(TrapDoorBlock.OPEN, (meta & 4) != 0);
            }
            return state;
        }
        if (block instanceof WallTorchBlock && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction facing = switch (meta) {
                case 1 -> Direction.EAST;
                case 2 -> Direction.WEST;
                case 3 -> Direction.SOUTH;
                case 4 -> Direction.NORTH;
                default -> Direction.NORTH;
            };
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        }
        if (block instanceof TorchBlock) {
            return state;
        }
        if (block instanceof HorizontalDirectionalBlock && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.from2DDataValue(meta & 3));
            if (state.hasProperty(BlockStateProperties.OPEN) && (meta & 4) != 0) {
                state = state.setValue(BlockStateProperties.OPEN, true);
            }
            return state;
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && meta >= 2 && meta <= 5) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, furnaceFacing(meta));
        }
        if (state.hasProperty(BlockStateProperties.FACING) && meta >= 0 && meta <= 5) {
            return state.setValue(BlockStateProperties.FACING, Direction.from3DDataValue(Mth.clamp(meta, 0, 5)));
        }
        return state;
    }

    private static String palettePath(String rawName) {
        String path = rawName == null ? "" : rawName.toLowerCase();
        if (path.contains(":")) {
            path = path.substring(path.indexOf(':') + 1);
        }
        if (path.startsWith("tile.")) {
            path = path.substring(5);
        }
        return path;
    }

    /** 1.7 furnace/chest/ladder: 2 north, 3 south, 4 west, 5 east. */
    private static Direction furnaceFacing(int meta) {
        return switch (meta & 7) {
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.WEST;
            case 5 -> Direction.EAST;
            default -> Direction.NORTH;
        };
    }
}
