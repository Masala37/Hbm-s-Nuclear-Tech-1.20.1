package com.hbm.world.gen;

import com.hbm.registry.ModStructures;
import com.hbm.world.gen.nbt.JigsawPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Places one 1.7 NBT jigsaw piece inside the generating chunk box.
 */
public class NbtStructurePiece extends StructurePiece {
    private final String pieceName;
    private final String spawnName;
    private final int coordBaseMode;
    private final boolean conformToTerrain;
    private final JigsawPiece.PaletteKind palette;

    public NbtStructurePiece(String pieceName, String spawnName, int coordBaseMode, boolean conformToTerrain,
                             JigsawPiece.PaletteKind palette, BoundingBox box) {
        super(ModStructures.NBT_PIECE.get(), 0, box);
        this.pieceName = pieceName;
        this.spawnName = spawnName;
        this.coordBaseMode = coordBaseMode;
        this.conformToTerrain = conformToTerrain;
        this.palette = palette == null ? JigsawPiece.PaletteKind.NONE : palette;
    }

    public NbtStructurePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.NBT_PIECE.get(), tag);
        this.pieceName = tag.getString("piece");
        this.spawnName = tag.getString("spawn");
        this.coordBaseMode = tag.getInt("cbm");
        this.conformToTerrain = tag.getBoolean("conform");
        JigsawPiece.PaletteKind kind = JigsawPiece.PaletteKind.NONE;
        try {
            kind = JigsawPiece.PaletteKind.valueOf(tag.getString("palette"));
        } catch (IllegalArgumentException ignored) {
        }
        this.palette = kind;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("piece", pieceName);
        tag.putString("spawn", spawnName == null ? "" : spawnName);
        tag.putInt("cbm", coordBaseMode);
        tag.putBoolean("conform", conformToTerrain);
        tag.putString("palette", palette.name());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        JigsawPiece piece = JigsawPiece.BY_NAME.get(pieceName);
        if (piece == null) {
            return;
        }
        piece.conformToTerrain = conformToTerrain;
        piece.palette = palette;
        piece.structure().place(level, piece, boundingBox, box, coordBaseMode, random);
    }
}
