package com.hbm.world.gen;

import com.hbm.config.StructureConfig;
import com.hbm.lib.RefStrings;
import com.hbm.world.gen.nbt.JigsawPiece;
import com.hbm.world.gen.nbt.JigsawPool;
import com.hbm.world.gen.nbt.SpawnCondition;
import com.hbm.world.gen.nbt.StructureSpacing;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 1.7 {@code NTMWorldGenerator} NBT spawn list plus MapGen silo / houses / labs / offices.
 * Bunker jigsaw still occupies 1.7 weight as an empty cell.
 */
public final class NTMStructures {
    private static final List<SpawnCondition> SPAWNS = new ArrayList<>();
    private static final Map<String, SpawnCondition> BY_NAME = new LinkedHashMap<>();
    private static boolean bootstrapped;

    private NTMStructures() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        JigsawPiece.BY_NAME.clear();
        SPAWNS.clear();
        BY_NAME.clear();

        add(named("spire", "structures/spire.nbt", -1, StructureConfig.spireSpawnWeight.get())
                .canSpawn(NTMStructures::veryFlat));
        // 1.7 MapGenNTMFeatures. Weight 50; inner pick places silo / houses / labs / offices.
        SpawnCondition features = new SpawnCondition("features");
        features.spawnWeight = StructureConfig.featuresSpawnWeight.get();
        features.canSpawn = b -> !water(b);
        SPAWNS.add(features);
        mapGen("silo", MapGenFeatures.SILO);
        mapGen("house_1", MapGenFeatures.HOUSE_1);
        mapGen("house_2", MapGenFeatures.HOUSE_2);
        mapGen("lab_1", MapGenFeatures.LAB_1);
        mapGen("lab_2", MapGenFeatures.LAB_2);
        mapGen("office", MapGenFeatures.OFFICE);
        mapGen("office_corner", MapGenFeatures.OFFICE_CORNER);
        mapGen("rural_house", MapGenFeatures.RURAL_HOUSE);
        SPAWNS.add(new SpawnCondition(StructureConfig.bunkerSpawnWeight.get(), b -> !water(b)));
        add(named("vertibird", "structures/vertibird.nbt", -3, StructureConfig.vertibirdSpawnWeight.get())
                .canSpawn(b -> !water(b) && sandy(b)));
        add(named("crashed_vertibird", "structures/crashed-vertibird.nbt", -10,
                StructureConfig.vertibirdCrashedSpawnWeight.get())
                .canSpawn(b -> !water(b) && sandy(b)));
        add(named("beached_patrol", "structures/beached_patrol.nbt", -5,
                StructureConfig.beachedPatrolSpawnWeight.get())
                .canSpawn(NTMStructures::beach)
                .height(58, 67));
        add(named("aircraft_carrier", "structures/aircraft_carrier.nbt", -6,
                StructureConfig.enableOceanStructures.get() ? StructureConfig.aircraftCarrierSpawnWeight.get() : 0)
                .canSpawn(NTMStructures::ocean)
                .maxHeight(42));
        add(named("oil_rig", "structures/oil_rig.nbt", -20,
                StructureConfig.enableOceanStructures.get() ? StructureConfig.oilRigSpawnWeight.get() : 0)
                .canSpawn(b -> ocean(b) && !b.is(BiomeTags.IS_DEEP_OCEAN))
                .height(11, 12));
        add(named("lighthouse", "structures/lighthouse.nbt", -40,
                StructureConfig.enableOceanStructures.get() ? StructureConfig.lighthouseSpawnWeight.get() : 0)
                .canSpawn(b -> ocean(b) || beach(b))
                .height(28, 29));
        add(named("dish", "structures/dish.nbt", -10, StructureConfig.dishSpawnWeight.get())
                .canSpawn(NTMStructures::plains)
                .height(53, 65));
        add(named("forestchem", "structures/forest_chem.nbt", -9, StructureConfig.forestChemSpawnWeight.get())
                .canSpawn(b -> moderateFlat(b) && !water(b)));
        add(named("labolatory", "structures/laboratory.nbt", -10, StructureConfig.laboratorySpawnWeight.get())
                .canSpawn(NTMStructures::flat)
                .height(53, 65));
        add(named("forest_post", "structures/forest_post.nbt", -10, StructureConfig.forestPostSpawnWeight.get())
                .canSpawn(b -> moderateFlat(b) && !water(b)));
        add(named("radio", "structures/radio_house.nbt", -6, StructureConfig.radioSpawnWeight.get())
                .canSpawn(NTMStructures::flat));
        // 1.7 StructureManager loaded this NBT but NTMWorldGenerator never registered it.
        locatableOnly(named("repeater_radio", "structures/repeater_radio.nbt", -6, 0)
                .canSpawn(NTMStructures::flat));
        add(named("factory", "structures/factory.nbt", -10, StructureConfig.factorySpawnWeight.get())
                .canSpawn(NTMStructures::flat));
        add(named("crane", "structures/crane_mod.nbt", -9, StructureConfig.craneSpawnWeight.get())
                .canSpawn(NTMStructures::flat));
        add(named("broadcaster_tower", "structures/broadcasting_tower.nbt", -9,
                StructureConfig.broadcastingTowerSpawnWeight.get())
                .canSpawn(NTMStructures::flat));
        add(named("plane1", "structures/crashed_plane_1.nbt", -5, StructureConfig.plane1SpawnWeight.get())
                .canSpawn(b -> moderateFlat(b) && !water(b)));
        add(named("plane2", "structures/crashed_plane_2.nbt", -8, StructureConfig.plane2SpawnWeight.get())
                .canSpawn(b -> moderateFlat(b) && !water(b)));
        add(named("desert_shack_1", "structures/desert_shack_1.nbt", -7,
                StructureConfig.desertShack1SpawnWeight.get())
                .canSpawn(NTMStructures::sandy));
        add(named("desert_shack_2", "structures/desert_shack_2.nbt", -7,
                StructureConfig.desertShack2SpawnWeight.get())
                .canSpawn(NTMStructures::sandy));
        add(named("desert_shack_3", "structures/desert_shack_3.nbt", -5,
                StructureConfig.desertShack3SpawnWeight.get())
                .canSpawn(NTMStructures::sandy));

        ruin("ruin_a", "structures/ntmruinsa.nbt", StructureConfig.ruinsASpawnWeight.get());
        ruin("ruin_b", "structures/ntmruinsb.nbt", StructureConfig.ruinsBSpawnWeight.get());
        ruin("ruin_c", "structures/ntmruinsc.nbt", StructureConfig.ruinsCSpawnWeight.get());
        ruin("ruin_d", "structures/ntmruinsd.nbt", StructureConfig.ruinsDSpawnWeight.get());
        ruin("ruin_e", "structures/ntmruinse.nbt", StructureConfig.ruinsESpawnWeight.get());
        ruin("ruin_f", "structures/ntmruinsf.nbt", StructureConfig.ruinsFSpawnWeight.get());
        ruin("ruin_g", "structures/ntmruinsg.nbt", StructureConfig.ruinsGSpawnWeight.get());
        ruin("ruin_h", "structures/ntmruinsh.nbt", StructureConfig.ruinsHSpawnWeight.get());
        ruin("ruin_i", "structures/ntmruinsi.nbt", StructureConfig.ruinsISpawnWeight.get());
        ruin("ruin_j", "structures/ntmruinsj.nbt", StructureConfig.ruinsJSpawnWeight.get());

        SPAWNS.add(new SpawnCondition(StructureConfig.plainsNullWeight.get(), NTMStructures::plains));
        SPAWNS.add(new SpawnCondition(StructureConfig.oceanNullWeight.get(), NTMStructures::ocean));

        addNamed(meteorDungeon());
        bootstrapped = true;
    }

    public static SpawnCondition byName(String name) {
        bootstrap();
        return name == null ? null : BY_NAME.get(name);
    }

    public static List<String> locatableIds() {
        bootstrap();
        return Collections.unmodifiableList(new ArrayList<>(BY_NAME.keySet()));
    }

    public static SpawnCondition pickAt(long seed, ChunkPos chunk, Holder<Biome> biome, int heightSpread) {
        return roll(seed, chunk, biome, heightSpread).spawn;
    }

    public static Pick roll(long seed, ChunkPos chunk, Holder<Biome> biome, int heightSpread) {
        Random random = StructureSpacing.pickRandom(chunk.x, chunk.z, seed,
                StructureConfig.minChunks(), StructureConfig.maxChunks());
        SpawnCondition picked = pick(biome, random, heightSpread);
        if (picked != null && "features".equals(picked.name)) {
            picked = MapGenFeatures.pickInner(biome, random);
        }
        return new Pick(picked, random);
    }

    public record Pick(SpawnCondition spawn, Random leftover) {
    }

    public static SpawnCondition pick(Holder<Biome> biome, Random random, int heightSpread) {
        int total = 0;
        List<SpawnCondition> valid = new ArrayList<>();
        for (SpawnCondition spawn : SPAWNS) {
            if (spawn.spawnWeight > 0 && spawn.isValid(biome)) {
                valid.add(spawn);
                total += spawn.spawnWeight;
            }
        }
        if (total <= 0) {
            return null;
        }
        int weight = random.nextInt(total);
        for (SpawnCondition spawn : valid) {
            weight -= spawn.spawnWeight;
            if (weight < 0) {
                return spawn;
            }
        }
        return null;
    }

    private static Builder named(String name, String path, int offset, int weight) {
        JigsawPiece piece = new JigsawPiece(name, loc(path), offset);
        SpawnCondition spawn = new SpawnCondition(name);
        spawn.structure = piece;
        spawn.spawnWeight = weight;
        return new Builder(spawn);
    }

    private static void ruin(String name, String path, int weight) {
        JigsawPiece piece = new JigsawPiece(name, loc(path), -1);
        piece.conformToTerrain = true;
        SpawnCondition spawn = new SpawnCondition(name);
        spawn.structure = piece;
        spawn.spawnWeight = StructureConfig.enableRuins.get() ? weight : 0;
        spawn.canSpawn = b -> !water(b) && b.value().hasPrecipitation();
        addNamed(spawn);
    }

    private static void add(Builder builder) {
        addNamed(builder.spawn);
    }

    private static void mapGen(String name, MapGenFeatures.Factory factory) {
        SpawnCondition spawn = new SpawnCondition(name);
        spawn.spawnWeight = 1;
        spawn.mapGen = factory;
        spawn.canSpawn = b -> !water(b);
        BY_NAME.put(name, spawn);
    }

    private static void locatableOnly(Builder builder) {
        if (builder.spawn.name != null) {
            BY_NAME.put(builder.spawn.name, builder.spawn);
        }
    }

    private static void addNamed(SpawnCondition spawn) {
        SPAWNS.add(spawn);
        if (spawn.name != null) {
            BY_NAME.put(spawn.name, spawn);
        }
    }

    private static SpawnCondition meteorDungeon() {
        SpawnCondition spawn = new SpawnCondition("meteor_dungeon");
        spawn.minHeight = 32;
        spawn.maxHeight = 32;
        spawn.sizeLimit = 128;
        spawn.spawnWeight = 1;
        spawn.canSpawn = b -> !water(b); // 1.7 biome.rootHeight >= 0
        spawn.startPool = "start";
        spawn.pools = new LinkedHashMap<>();
        spawn.pools.put("start", pool().add(bricks("meteor_core", "structures/meteor/meteor-core.nbt"), 1));
        spawn.pools.put("spike", pool().add(conform("meteor_spike", "structures/meteor/meteor-spike.nbt", -3), 1));
        spawn.pools.put("default", pool()
                .add(bricks("meteor_corner", "structures/meteor/meteor-corner.nbt"), 2)
                .add(bricks("meteor_t", "structures/meteor/meteor-t.nbt"), 3)
                .add(bricks("meteor_stairs", "structures/meteor/meteor-stairs.nbt"), 1)
                .add(bricks("meteor_room_base_thru", "structures/meteor/room10/room-base-thru.nbt"), 3)
                .add(bricks("meteor_room_base_end", "structures/meteor/room10/room-base-end.nbt"), 4)
                .fallback("fallback"));
        spawn.pools.put("10room", pool()
                .add(bricks("meteor_room_basic", "structures/meteor/room10/room-basic.nbt"), 1)
                .add(bricks("meteor_room_balcony", "structures/meteor/room10/room-balcony.nbt"), 1)
                .add(bricks("meteor_room_dragon", "structures/meteor/room10/room-dragon.nbt"), 1)
                .add(bricks("meteor_room_ladder", "structures/meteor/room10/room-ladder.nbt"), 1)
                .add(ooze("meteor_room_ooze", "structures/meteor/room10/room-ooze.nbt"), 1)
                .add(bricks("meteor_room_split", "structures/meteor/room10/room-split.nbt"), 1)
                .add(bricks("meteor_room_stairs", "structures/meteor/room10/room-stairs.nbt"), 1)
                .add(bricks("meteor_room_triple", "structures/meteor/room10/room-triple.nbt"), 1)
                .fallback("roomback"));
        JigsawPool loot3 = pool();
        String[] loot = {
                "meteor-3-bale", "meteor-3-blank", "meteor-3-block", "meteor-3-crab", "meteor-3-crab-tesla",
                "meteor-3-crate", "meteor-3-dirt", "meteor-3-lead", "meteor-3-ooze", "meteor-3-pillar",
                "meteor-3-star", "meteor-3-tesla", "meteor-3-book", "meteor-3-mku", "meteor-3-statue",
                "meteor-3-glow"
        };
        for (String file : loot) {
            loot3.add(new JigsawPiece(file, loc("structures/meteor/loot3x3/" + file + ".nbt")), 1);
        }
        loot3.fallback("3x3loot");
        spawn.pools.put("3x3loot", loot3);
        spawn.pools.put("headloot", pool()
                .add(crates("meteor_dragon_chest", "structures/meteor/room10/headloot/loot-chest.nbt"), 1)
                .add(crates("meteor_dragon_tesla", "structures/meteor/room10/headloot/loot-tesla.nbt"), 1)
                .add(crates("meteor_dragon_trap", "structures/meteor/room10/headloot/loot-trap.nbt"), 1)
                .add(crates("meteor_dragon_crate_crab", "structures/meteor/room10/headloot/loot-crate-crab.nbt"), 1)
                .fallback("headback"));
        spawn.pools.put("fallback", pool().add(bricks("meteor_fallback", "structures/meteor/meteor-fallback.nbt"), 1));
        spawn.pools.put("roomback", pool().add(bricks("meteor_room_fallback",
                "structures/meteor/room10/room-fallback.nbt"), 1));
        spawn.pools.put("headback", pool().add(crates("meteor_loot_fallback",
                "structures/meteor/room10/headloot/loot-fallback.nbt"), 1));
        return spawn;
    }

    private static JigsawPool pool() {
        return new JigsawPool();
    }

    private static JigsawPiece bricks(String name, String path) {
        JigsawPiece piece = new JigsawPiece(name, loc(path));
        piece.palette = JigsawPiece.PaletteKind.BRICKS;
        return piece;
    }

    private static JigsawPiece crates(String name, String path) {
        JigsawPiece piece = new JigsawPiece(name, loc(path));
        piece.palette = JigsawPiece.PaletteKind.CRATES;
        return piece;
    }

    private static JigsawPiece ooze(String name, String path) {
        JigsawPiece piece = new JigsawPiece(name, loc(path));
        piece.palette = JigsawPiece.PaletteKind.OOZE;
        return piece;
    }

    private static JigsawPiece conform(String name, String path, int offset) {
        JigsawPiece piece = new JigsawPiece(name, loc(path), offset);
        piece.conformToTerrain = true;
        return piece;
    }

    private static ResourceLocation loc(String path) {
        return new ResourceLocation(RefStrings.MODID, path.toLowerCase(java.util.Locale.ROOT));
    }

    private static boolean water(Holder<Biome> biome) {
        return biome.is(Tags.Biomes.IS_WATER) || biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER);
    }

    private static boolean ocean(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_OCEAN);
    }

    private static boolean beach(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_BEACH);
    }

    private static boolean sandy(Holder<Biome> biome) {
        return biome.is(Tags.Biomes.IS_SANDY);
    }

    private static boolean plains(Holder<Biome> biome) {
        return biome.is(Tags.Biomes.IS_PLAINS);
    }

    private static boolean flat(Holder<Biome> biome) {
        // 1.7: heightVariation <= 0.2 && SPARSE && !water. 1.20 SPARSE is still savanna-like.
        return biome.is(Tags.Biomes.IS_SPARSE) && !water(biome) && moderateFlat(biome);
    }

    static boolean flatEnoughForSilo(Holder<Biome> biome) {
        // 1.7 MapGenNTMFeatures: heightVariation <= 0.25 (forests yes, extreme hills no).
        return moderateFlat(biome);
    }

    /**
     * 1.7 {@code heightVariation <= 0.05 && !water}: plains, desert, beach, snowy plains.
     * Forests and taiga are too rough for the spire.
     */
    private static boolean veryFlat(Holder<Biome> biome) {
        if (water(biome) || !moderateFlat(biome) || biome.is(BiomeTags.IS_BADLANDS)
                || biome.is(Tags.Biomes.IS_PLATEAU) || biome.is(BiomeTags.IS_TAIGA)
                || biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_FOREST)) {
            return false;
        }
        return biome.is(Tags.Biomes.IS_PLAINS) || biome.is(Tags.Biomes.IS_SANDY)
                || biome.is(Tags.Biomes.IS_SNOWY);
    }

    /** 1.7 {@code heightVariation <= 0.3}: forests and swamps yes, mountains/hills no. */
    private static boolean moderateFlat(Holder<Biome> biome) {
        return !biome.is(Tags.Biomes.IS_MOUNTAIN) && !biome.is(BiomeTags.IS_HILL);
    }

    private static final class Builder {
        private final SpawnCondition spawn;

        private Builder(SpawnCondition spawn) {
            this.spawn = spawn;
        }

        private Builder canSpawn(java.util.function.Predicate<Holder<Biome>> predicate) {
            spawn.canSpawn = predicate;
            return this;
        }

        private Builder height(int min, int max) {
            spawn.minHeight = min;
            spawn.maxHeight = max;
            return this;
        }

        private Builder maxHeight(int max) {
            spawn.maxHeight = max;
            return this;
        }
    }
}
