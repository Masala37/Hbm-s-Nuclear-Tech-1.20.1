package com.hbm.world.gen.nbt;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * One NBT piece referenced by a spawn condition or jigsaw pool.
 */
public final class JigsawPiece {
    public static final Map<String, JigsawPiece> BY_NAME = new HashMap<>();

    public final String name;
    public final ResourceLocation location;
    public final int heightOffset;
    public boolean conformToTerrain;
    public boolean alignToTerrain;
    public int instanceLimit;
    public boolean required;
    public PaletteKind palette = PaletteKind.NONE;

    private NbtStructure loaded;

    public JigsawPiece(String name, ResourceLocation location) {
        this(name, location, 0);
    }

    public JigsawPiece(String name, ResourceLocation location, int heightOffset) {
        if (name == null) {
            throw new IllegalStateException("Jigsaw piece missing a name");
        }
        if (BY_NAME.containsKey(name)) {
            throw new IllegalStateException("Duplicate jigsaw piece: " + name);
        }
        this.name = name;
        this.location = location;
        this.heightOffset = heightOffset;
        BY_NAME.put(name, this);
    }

    public NbtStructure structure() {
        if (loaded == null) {
            loaded = NbtStructure.load(location);
        }
        return loaded;
    }

    public enum PaletteKind {
        NONE,
        BRICKS,
        CRATES,
        OOZE
    }
}
