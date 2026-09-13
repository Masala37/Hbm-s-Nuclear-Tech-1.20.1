package com.hbm.blockentity.network;

import com.hbm.blocks.DummyableMeta;
import com.hbm.energy.PylonConnectionType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * 1.7.10 pylon variants: type, range, mounts, dummyable box, energy faces.
 */
public enum PylonKind {
    CONNECTOR(PylonConnectionType.SINGLE, 10.0D, false, false, null, 0, false),
    SMALL(PylonConnectionType.SINGLE, 25.0D, false, false, null, 0, false),
    MEDIUM_WOOD(PylonConnectionType.TRIPLE, 45.0D, false, false, new int[]{6, 0, 0, 0, 0, 0}, 0, false),
    MEDIUM_WOOD_TRANSFORMER(PylonConnectionType.TRIPLE, 45.0D, true, false, new int[]{6, 0, 0, 0, 0, 0}, 0, false),
    MEDIUM_STEEL(PylonConnectionType.TRIPLE, 45.0D, false, true, new int[]{6, 0, 0, 0, 0, 0}, 0, false),
    MEDIUM_STEEL_TRANSFORMER(PylonConnectionType.TRIPLE, 45.0D, true, true, new int[]{6, 0, 0, 0, 0, 0}, 0, false),
    LARGE(PylonConnectionType.QUAD, 100.0D, false, false, new int[]{13, 0, 1, 1, 1, 1}, 0, false),
    SUBSTATION(PylonConnectionType.QUAD, 20.0D, false, false, new int[]{4, 0, 1, 1, 2, 2}, 1, true);

    private final PylonConnectionType type;
    private final double maxWireLength;
    private final boolean transformer;
    private final boolean steel;
    private final int[] dimensions;
    private final int offset;
    private final boolean extras;

    PylonKind(PylonConnectionType type, double maxWireLength, boolean transformer, boolean steel,
              int[] dimensions, int offset, boolean extras) {
        this.type = type;
        this.maxWireLength = maxWireLength;
        this.transformer = transformer;
        this.steel = steel;
        this.dimensions = dimensions;
        this.offset = offset;
        this.extras = extras;
    }

    public PylonConnectionType connectionType() {
        return type;
    }

    public double maxWireLength() {
        return maxWireLength;
    }

    public boolean hasTransformer() {
        return transformer;
    }

    public boolean steel() {
        return steel;
    }

    public boolean dummyable() {
        return dimensions != null;
    }

    public int[] dimensions() {
        return dimensions.clone();
    }

    public int offset() {
        return offset;
    }

    public boolean hasExtras() {
        return extras;
    }

    public boolean requiresSubstation() {
        return this == LARGE;
    }

    public String rangeLabel() {
        return ((int) maxWireLength) + "m";
    }

    /**
     * World-space offset of {@code getConnectionPoint} inside the core cell.
     */
    public Vec3 connectionPoint(Direction facing) {
        if (this == SUBSTATION) {
            return new Vec3(0.5D, 5.25D, 0.5D);
        }
        Vec3[] mounts = mounts(facing);
        if (mounts.length == 0) {
            return new Vec3(0.5D, 0.5D, 0.5D);
        }
        return mounts[0];
    }

    public Vec3[] mounts(Direction facing) {
        Direction dir = horizontal(facing);
        return switch (this) {
            case CONNECTOR -> new Vec3[]{new Vec3(0.5D, 0.5D, 0.5D)};
            case SMALL -> new Vec3[]{new Vec3(0.5D, 5.4D, 0.5D)};
            case MEDIUM_WOOD, MEDIUM_WOOD_TRANSFORMER, MEDIUM_STEEL, MEDIUM_STEEL_TRANSFORMER -> {
                double h = 7.5D;
                int dx = dir.getStepX();
                int dz = dir.getStepZ();
                yield new Vec3[]{
                        new Vec3(0.5D, h, 0.5D),
                        new Vec3(0.5D + dx, h, 0.5D + dz),
                        new Vec3(0.5D + dx * 2.0D, h, 0.5D + dz * 2.0D)
                };
            }
            case LARGE -> largeMounts(dir);
            case SUBSTATION -> substationMounts(dir);
        };
    }

    /**
     * Adjacent HE: connector opposite of placed face; small pylon all sides;
     * medium transformer opposite of core facing; others none (wires / extras).
     */
    public boolean connectsEnergy(Direction facing, Direction side) {
        return switch (this) {
            case CONNECTOR -> facing.getOpposite() == side;
            case SMALL -> true;
            case MEDIUM_WOOD_TRANSFORMER, MEDIUM_STEEL_TRANSFORMER -> horizontal(facing).getOpposite() == side;
            default -> false;
        };
    }

    public float tesrYaw(Direction facing) {
        int ordinal = horizontal(facing).get3DDataValue();
        return switch (this) {
            case MEDIUM_WOOD, MEDIUM_WOOD_TRANSFORMER, MEDIUM_STEEL, MEDIUM_STEEL_TRANSFORMER ->
                    DummyableMeta.dummyableYaw(ordinal);
            case LARGE -> switch (ordinal) {
                case DummyableMeta.NORTH -> 90.0F;
                case DummyableMeta.WEST -> 135.0F;
                case DummyableMeta.SOUTH -> 0.0F;
                default -> 45.0F;
            };
            case SUBSTATION -> (ordinal == DummyableMeta.WEST || ordinal == DummyableMeta.EAST) ? 0.0F : 90.0F;
            default -> 0.0F;
        };
    }

    private static Direction horizontal(Direction facing) {
        if (facing.getAxis().isHorizontal()) {
            return facing;
        }
        return Direction.SOUTH;
    }

    private static Vec3[] largeMounts(Direction dir) {
        double topOff = 0.75D + 0.0625D;
        double sideOff = 3.375D;
        float angle = switch (dir.get3DDataValue()) {
            case DummyableMeta.NORTH -> 0.0F;
            case DummyableMeta.WEST -> (float) (Math.PI * 0.25D);
            case DummyableMeta.SOUTH -> (float) (Math.PI * 0.5D);
            default -> (float) (Math.PI * 0.75D);
        };
        Vec3 vec = new Vec3(sideOff, 0.0D, 0.0D).yRot(angle);
        return new Vec3[]{
                new Vec3(0.5D + vec.x, 11.5D + topOff, 0.5D + vec.z),
                new Vec3(0.5D + vec.x, 11.5D - topOff, 0.5D + vec.z),
                new Vec3(0.5D - vec.x, 11.5D + topOff, 0.5D - vec.z),
                new Vec3(0.5D - vec.x, 11.5D - topOff, 0.5D - vec.z)
        };
    }

    private static Vec3[] substationMounts(Direction dir) {
        double topOff = 5.25D;
        float angle = (dir.get3DDataValue() == DummyableMeta.WEST || dir.get3DDataValue() == DummyableMeta.EAST)
                ? (float) (Math.PI * 0.5D)
                : 0.0F;
        Vec3 vec = new Vec3(1.0D, 0.0D, 0.0D).yRot(angle);
        return new Vec3[]{
                new Vec3(0.5D + vec.x * 0.5D, topOff, 0.5D + vec.z * 0.5D),
                new Vec3(0.5D + vec.x * 1.5D, topOff, 0.5D + vec.z * 1.5D),
                new Vec3(0.5D - vec.x * 0.5D, topOff, 0.5D - vec.z * 0.5D),
                new Vec3(0.5D - vec.x * 1.5D, topOff, 0.5D - vec.z * 1.5D)
        };
    }
}
