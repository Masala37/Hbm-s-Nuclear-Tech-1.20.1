package com.hbm.world.gen;

import com.hbm.config.StructureConfig;
import com.hbm.world.gen.component.SiloComponent;
import com.hbm.world.gen.nbt.NbtStructureAssembler;
import com.hbm.world.gen.nbt.SpawnCondition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * One named 1.7 NBT spawn. Each ID has its own structure set on {@code hbm:ntm_grid}
 * so vanilla asks every candidate on the same spawn chunk; this type only places
 * if {@link NTMStructures#pickAt} selects this name (1.7 biome-filtered weights).
 */
public class NbtNtmStructure extends Structure {
    public static final Codec<NbtNtmStructure> CODEC = RecordCodecBuilder.<NbtNtmStructure>mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    Codec.STRING.fieldOf("spawn").forGetter(NbtNtmStructure::spawnName)
            ).apply(instance, NbtNtmStructure::new)).codec();

    private final String spawnName;

    public NbtNtmStructure(StructureSettings settings, String spawnName) {
        super(settings);
        this.spawnName = spawnName;
    }

    public String spawnName() {
        return spawnName;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        NTMStructures.bootstrap();
        if (StructureConfig.parseFlag() == 0) {
            return Optional.empty();
        }
        SpawnCondition named = NTMStructures.byName(spawnName);
        if (named == null || !named.hasGeometry()) {
            return Optional.empty();
        }
        if (named.mapGen == null && named.spawnWeight <= 0) {
            return Optional.empty();
        }
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMinBlockX() + 8;
        int z = chunk.getMinBlockZ() + 8;
        int y = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        Holder<Biome> biome = context.chunkGenerator().getBiomeSource().getNoiseBiome(
                QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z),
                context.randomState().sampler());
        if (!context.validBiome().test(biome)) {
            return Optional.empty();
        }
        NTMStructures.Pick pick = NTMStructures.roll(context.seed(), chunk, biome, 0);
        SpawnCondition picked = pick.spawn();
        if (picked == null || picked.name == null || !spawnName.equals(picked.name)) {
            return Optional.empty();
        }
        if (named.mapGen != null) {
            return Optional.of(new GenerationStub(new BlockPos(chunk.getMinBlockX(), y, chunk.getMinBlockZ()),
                    builder -> {
                        StructurePiece piece = named.mapGen.create(pick.leftover(), chunk);
                        if (piece instanceof SiloComponent silo) {
                            silo.alignToSurface((ix, iz) -> context.chunkGenerator().getFirstOccupiedHeight(
                                    ix, iz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(),
                                    context.randomState()));
                        }
                        builder.addPiece(piece);
                    }));
        }
        return Optional.of(new GenerationStub(new BlockPos(chunk.getMinBlockX(), y, chunk.getMinBlockZ()),
                builder -> NbtStructureAssembler.addPieces(builder, named, context.random(), chunk,
                        (ix, iz) -> context.chunkGenerator().getFirstOccupiedHeight(ix, iz,
                                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(),
                                context.randomState()))));
    }

    @Override
    public StructureType<?> type() {
        return com.hbm.registry.ModStructures.NBT_STRUCTURE.get();
    }
}
