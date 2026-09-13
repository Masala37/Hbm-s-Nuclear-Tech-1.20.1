package com.hbm.world.gen.nbt;

/**
 * 1.7 {@code INBTBlockTransformable} meta rotations. Pure ints so tests can run without Forge.
 */
public final class LegacyMetaTransform {
    private LegacyMetaTransform() {
    }

    /** Furnace / ladder / chest / deco: 2 north, 3 south, 4 west, 5 east. */
    public static int deco(int meta, int coordBaseMode) {
        if (coordBaseMode == 0) {
            return meta;
        }
        return switch (coordBaseMode) {
            case 1 -> switch (meta) {
                case 2 -> 5;
                case 3 -> 4;
                case 4 -> 2;
                case 5 -> 3;
                default -> meta;
            };
            case 2 -> switch (meta) {
                case 2 -> 3;
                case 3 -> 2;
                case 4 -> 5;
                case 5 -> 4;
                default -> meta;
            };
            case 3 -> switch (meta) {
                case 2 -> 4;
                case 3 -> 5;
                case 4 -> 3;
                case 5 -> 2;
                default -> meta;
            };
            default -> meta;
        };
    }

    /** CRT / toaster: rotation in bits 0–1. */
    public static int decoModelLow(int meta, int coordBaseMode) {
        if (coordBaseMode == 0) {
            return meta;
        }
        int rot = (meta + coordBaseMode) % 4;
        int type = (meta / 4) * 4;
        return rot | type;
    }

    /**
     * 1.7 CRT/toaster store player yaw (0 south, 1 west, 2 north, 3 east).
     * The screen faces the opposite direction, which is a 2D data-value of {@code (meta + 2) & 3}.
     */
    public static int crtScreen2d(int meta) {
        return (meta + 2) & 3;
    }

    /** Filing cabinet / BlockDecoModel: rotation in bits 2–3. */
    public static int decoModelHigh(int meta, int coordBaseMode) {
        if (coordBaseMode == 0) {
            return meta;
        }
        int rot = meta >> 2;
        int type = meta & 3;
        rot = switch (coordBaseMode) {
            case 1 -> (rot & 3) < 2 ? rot ^ 3 : rot ^ 2;
            case 2 -> rot ^ 1;
            case 3 -> (rot & 3) < 2 ? rot ^ 2 : rot ^ 3;
            default -> rot;
        };
        return (rot << 2) | type;
    }

    public static int stairs(int meta, int coordBaseMode) {
        if (coordBaseMode == 0) {
            return meta;
        }
        return switch (coordBaseMode) {
            case 1 -> (meta & 3) < 2 ? meta ^ 2 : meta ^ 3;
            case 2 -> meta ^ 1;
            case 3 -> (meta & 3) < 2 ? meta ^ 3 : meta ^ 2;
            default -> meta;
        };
    }

    public static int trapdoor(int meta, int coordBaseMode) {
        if (coordBaseMode == 0) {
            return meta;
        }
        return switch (coordBaseMode) {
            case 1 -> (meta & 3) < 2 ? meta ^ 3 : meta ^ 2;
            case 2 -> meta ^ 1;
            case 3 -> (meta & 3) < 2 ? meta ^ 2 : meta ^ 3;
            default -> meta;
        };
    }

    public static int pillar(int meta, int coordBaseMode) {
        if (coordBaseMode == 0 || coordBaseMode == 2) {
            return meta;
        }
        int type = meta & 3;
        int rot = meta & 12;
        if (rot == 4) {
            return type | 8;
        }
        if (rot == 8) {
            return type | 4;
        }
        return meta;
    }

    public static int directional(int meta, int coordBaseMode) {
        if (coordBaseMode == 0) {
            return meta;
        }
        int rot = meta & 3;
        int other = meta & 12;
        rot = switch (coordBaseMode) {
            case 1 -> (rot + 1) % 4;
            case 2 -> rot ^ 2;
            case 3 -> (rot + 3) % 4;
            default -> rot;
        };
        return other | rot;
    }

    public static int torch(int meta, int coordBaseMode) {
        if (coordBaseMode == 0) {
            return meta;
        }
        return switch (coordBaseMode) {
            case 1 -> switch (meta) {
                case 1 -> 3;
                case 2 -> 4;
                case 3 -> 2;
                case 4 -> 1;
                default -> meta;
            };
            case 2 -> switch (meta) {
                case 1 -> 2;
                case 2 -> 1;
                case 3 -> 4;
                case 4 -> 3;
                default -> meta;
            };
            case 3 -> switch (meta) {
                case 1 -> 4;
                case 2 -> 3;
                case 3 -> 1;
                case 4 -> 2;
                default -> meta;
            };
            default -> meta;
        };
    }

    /** Upper halves (meta 8/9) keep hinge bits; only the lower half rotates. */
    public static int door(int meta, int coordBaseMode) {
        if (coordBaseMode == 0 || meta == 8 || meta == 9) {
            return meta;
        }
        return directional(meta, coordBaseMode);
    }

    public static int lever(int meta, int coordBaseMode) {
        if (coordBaseMode == 0) {
            return meta;
        }
        int dir = meta & 7;
        int powered = meta & 8;
        if (dir <= 0 || dir >= 7) {
            if (coordBaseMode == 1 || coordBaseMode == 3) {
                dir ^= 0b111;
            }
        } else if (dir >= 5) {
            if (coordBaseMode == 1 || coordBaseMode == 3) {
                dir = (dir + 1) % 2 + 5;
            }
        } else {
            dir = torch(dir, coordBaseMode);
        }
        return powered | dir;
    }

    public static int vine(int meta, int coordBaseMode) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            int bit = 1 << i;
            if ((meta & bit) != 0) {
                result |= rotateVineBit(bit, coordBaseMode);
            }
        }
        return result;
    }

    private static int rotateVineBit(int bit, int coordBaseMode) {
        int index = switch (bit) {
            case 1 -> 0;
            case 2 -> 1;
            case 4 -> 2;
            case 8 -> 3;
            default -> -1;
        };
        if (index < 0) {
            return 0;
        }
        int rotated = switch (coordBaseMode) {
            case 1 -> (index + 1) % 4;
            case 2 -> (index + 2) % 4;
            case 3 -> (index + 3) % 4;
            default -> index;
        };
        return switch (rotated) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 4;
            default -> 8;
        };
    }
}
