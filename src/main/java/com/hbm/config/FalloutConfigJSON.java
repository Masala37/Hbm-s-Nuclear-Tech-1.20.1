package com.hbm.config;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.HbmNuclearTechMod;
import com.hbm.blocks.generic.SellafieldSlakedBlock;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Port of legacy {@code FalloutConfigJSON}: JSON-driven fallout block conversions.
 * Legacy sellafield intensity meta darkened {@code sellafield_slaked} via colorMultiplier;
 * crater conversions always place slaked sellafite (not live waste tiers).
 */
public final class FalloutConfigJSON {

    public static final List<FalloutEntry> entries = new ArrayList<>();
    public static final Random rand = new Random();
    public static final Gson gson = new Gson();

    /** Known material keys for JSON serialize/deserialize (legacy matNames stand-in; 1.20 has no Material). */
    public static final HashBiMap<String, String> matNames = HashBiMap.create();

    static {
        for (String key : new String[]{
                "grass", "ground", "wood", "rock", "iron", "anvil", "water", "lava",
                "leaves", "plants", "vine", "sponge", "cloth", "fire", "sand", "circuits",
                "carpet", "redstoneLight", "tnt", "coral", "ice", "packedIce", "snow",
                "craftedSnow", "cactus", "clay", "gourd", "dragonEgg", "portal", "cake", "web"
        }) {
            matNames.put(key, key);
        }
    }

    private FalloutConfigJSON() {
    }

    public static void initialize() {
        Path folder = FMLPaths.CONFIGDIR.get().resolve("hbm");
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            HbmNuclearTechMod.LOGGER.error("Failed to create HBM config directory {}; using built-in defaults", folder, e);
            entries.clear();
            initDefault();
            return;
        }

        Path config = folder.resolve("hbmFallout.json");
        Path template = folder.resolve("_hbmFallout.json");

        entries.clear();
        initDefault();

        if (!Files.exists(config)) {
            writeDefault(template);
        } else {
            List<FalloutEntry> conf = readConfig(config);
            if (conf != null && !conf.isEmpty()) {
                entries.clear();
                entries.addAll(conf);
                HbmNuclearTechMod.LOGGER.info("Loaded {} fallout conversion entries from {}", entries.size(), config);
            } else {
                HbmNuclearTechMod.LOGGER.warn(
                        "Fallout config {} missing or empty — keeping {} built-in defaults (see {})",
                        config, entries.size(), template);
                if (!Files.exists(template)) {
                    writeDefault(template);
                }
            }
        }
    }

    private static int sellafieldRank(Block block) {
        if (block == ModBlocks.SELLAFIELD_5.get()) {
            return 8;
        }
        if (block == ModBlocks.SELLAFIELD_4.get()) {
            return 6;
        }
        if (block == ModBlocks.SELLAFIELD_3.get()) {
            return 5;
        }
        if (block == ModBlocks.SELLAFIELD_2.get()) {
            return 4;
        }
        if (block == ModBlocks.SELLAFIELD_1.get()) {
            return 3;
        }
        if (block == ModBlocks.SELLAFIELD_0.get()) {
            return 2;
        }
        if (block == ModBlocks.SELLAFIELD_SLAKED_1.get()
                || block == ModBlocks.SELLAFIELD_SLAKED_2.get()
                || block == ModBlocks.SELLAFIELD_SLAKED_3.get()) {
            return 1;
        }
        if (block instanceof SellafieldSlakedBlock || block == ModBlocks.SELLAFIELD_SLAKED.get()) {
            return 0;
        }
        if (block == ModBlocks.SELLAFIELD_BEDROCK.get()) {
            return 100;
        }
        return -1;
    }

    private static boolean isSellafieldSurface(Block block) {
        return sellafieldRank(block) >= 0 && block != ModBlocks.SELLAFIELD_BEDROCK.get();
    }

    private static WeightedBlock wb(Block block, int weight) {
        return new WeightedBlock(block, weight, -1);
    }

    private static WeightedBlock wb(Block block, int weight, int meta) {
        return new WeightedBlock(block, weight, meta);
    }

    private static WeightedBlock wb(Block block) {
        return wb(block, 1);
    }

    private static void initDefault() {
        double woodEffectRange = 65D;

        /* Petrify logs / planks; destroy other wood within range */
        entries.add(new FalloutEntry().mT(BlockTags.LOGS).prim(wb(ModBlocks.WASTE_LOG.get())).max(woodEffectRange));
        entries.add(new FalloutEntry().mB(Blocks.MUSHROOM_STEM).prim(wb(ModBlocks.WASTE_LOG.get())).max(woodEffectRange));
        entries.add(new FalloutEntry().mB(Blocks.RED_MUSHROOM_BLOCK).prim(wb(Blocks.AIR)).max(woodEffectRange));
        entries.add(new FalloutEntry().mB(Blocks.BROWN_MUSHROOM_BLOCK).prim(wb(Blocks.AIR)).max(woodEffectRange));
        entries.add(new FalloutEntry().mB(Blocks.SNOW).prim(wb(Blocks.AIR)).max(woodEffectRange));
        entries.add(new FalloutEntry().mT(BlockTags.PLANKS).prim(wb(ModBlocks.WASTE_PLANKS.get())).max(woodEffectRange));
        entries.add(new FalloutEntry().mMa("wood").prim(wb(Blocks.AIR)).max(woodEffectRange));

        /* Destroy foliage within radius; waste-leaves outside inner band */
        entries.add(new FalloutEntry().mMa("leaves").prim(wb(Blocks.AIR)).max(woodEffectRange));
        entries.add(new FalloutEntry().mMa("plants").prim(wb(Blocks.AIR)).max(woodEffectRange));
        entries.add(new FalloutEntry().mMa("vine").prim(wb(Blocks.AIR)).max(woodEffectRange));
        entries.add(new FalloutEntry().mB(ModBlocks.WASTE_LEAVES.get()).prim(wb(Blocks.AIR)).max(woodEffectRange));
        entries.add(new FalloutEntry().mT(BlockTags.LEAVES).prim(wb(ModBlocks.WASTE_LEAVES.get())).min(woodEffectRange - 5D));

        /* Glyphid entries skipped — blocks may not exist in this port */

        entries.add(new FalloutEntry().mB(Blocks.MOSSY_COBBLESTONE).prim(wb(Blocks.COAL_ORE)));
        entries.add(new FalloutEntry()
                .mB(ModBlocks.ORE_NETHER_URANIUM.get())
                .prim(wb(ModBlocks.ORE_NETHER_SCHRABIDIUM.get(), 1),
                        wb(ModBlocks.ORE_NETHER_URANIUM_SCORCHED.get(), 99)));

        for (int i = 1; i <= 10; i++) {
            int m = 10 - i;
            Block sellafield = ModBlocks.SELLAFIELD_SLAKED.get();
            double max = i * 5D;

            entries.add(new FalloutEntry()
                    .prim(wb(ModBlocks.ORE_SELLAFIELD_DIAMOND.get(), 3),
                            wb(ModBlocks.ORE_SELLAFIELD_EMERALD.get(), 2))
                    .c(0.5).max(max).sol(true).mB(Blocks.COAL_ORE));
            entries.add(new FalloutEntry()
                    .prim(wb(ModBlocks.ORE_SELLAFIELD_DIAMOND.get()))
                    .c(0.2).max(max).sol(true).mB(ModBlocks.ORE_LIGNITE.get()));
            entries.add(new FalloutEntry()
                    .prim(wb(ModBlocks.ORE_SELLAFIELD_EMERALD.get()))
                    .max(max).sol(true).mB(ModBlocks.ORE_BERYLLIUM.get()));
            if (m > 4) {
                entries.add(new FalloutEntry()
                        .prim(wb(ModBlocks.ORE_SELLAFIELD_SCHRABIDIUM.get(), 1),
                                wb(ModBlocks.ORE_SELLAFIELD_URANIUM_SCORCHED.get(), 9))
                        .max(max).sol(true).mB(ModBlocks.ORE_URANIUM.get()));
                entries.add(new FalloutEntry()
                        .prim(wb(ModBlocks.ORE_SELLAFIELD_SCHRABIDIUM.get(), 1),
                                wb(ModBlocks.ORE_SELLAFIELD_URANIUM_SCORCHED.get(), 9))
                        .max(max).sol(true).mB(ModBlocks.ORE_GNEISS_URANIUM.get()));
            }
            entries.add(new FalloutEntry()
                    .prim(wb(ModBlocks.ORE_SELLAFIELD_RADGEM.get()))
                    .max(max).sol(true).mB(Blocks.DIAMOND_ORE));

            entries.add(new FalloutEntry()
                    .prim(wb(ModBlocks.SELLAFIELD_BEDROCK.get())).max(max).sol(true).mB(Blocks.BEDROCK));
            entries.add(new FalloutEntry()
                    .prim(wb(ModBlocks.SELLAFIELD_BEDROCK.get())).max(max).sol(true).mB(ModBlocks.ORE_BEDROCK_OIL.get()));
            entries.add(new FalloutEntry()
                    .prim(wb(ModBlocks.SELLAFIELD_BEDROCK.get())).max(max).sol(true).mB(ModBlocks.SELLAFIELD_BEDROCK.get()));

            entries.add(new FalloutEntry().prim(wb(sellafield, 1, m)).max(max).sol(true).mMa("iron"));
            entries.add(new FalloutEntry().prim(wb(sellafield, 1, m)).max(max).sol(true).mMa("rock"));
            entries.add(new FalloutEntry().prim(wb(sellafield, 1, m)).max(max).sol(true).mMa("sand"));
            entries.add(new FalloutEntry().prim(wb(sellafield, 1, m)).max(max).sol(true).mMa("ground"));
            if (i <= 9) {
                entries.add(new FalloutEntry().prim(wb(sellafield, 1, m)).max(max).sol(true).mMa("grass"));
            }
            entries.add(new FalloutEntry().prim(wb(sellafield, 1, m)).max(max).sol(true).mB(Blocks.DEEPSLATE));
            entries.add(new FalloutEntry().prim(wb(sellafield, 1, m)).max(max).sol(true).mB(Blocks.STONE));
        }

        entries.add(new FalloutEntry()
                .mB(Blocks.MYCELIUM)
                .prim(wb(ModBlocks.WASTE_MYCELIUM.get())));
        entries.add(new FalloutEntry()
                .mB(Blocks.SAND)
                .prim(wb(ModBlocks.WASTE_TRINITITE.get()))
                .c(0.05));
        entries.add(new FalloutEntry()
                .mB(Blocks.RED_SAND)
                .prim(wb(ModBlocks.WASTE_TRINITITE_RED.get()))
                .c(0.05));
        entries.add(new FalloutEntry()
                .mB(Blocks.CLAY)
                .prim(wb(Blocks.TERRACOTTA)));
    }

    private static void writeDefault(Path file) {
        try (Writer fileWriter = Files.newBufferedWriter(file);
             JsonWriter writer = new JsonWriter(fileWriter)) {
            writer.setIndent("  ");
            writer.beginObject();
            writer.name("entries").beginArray();

            for (FalloutEntry entry : entries) {
                // Expand tag-matched defaults into concrete matchesBlock rows for the template
                List<Block> tagBlocks = entry.resolveTagBlocksForWrite();
                if (tagBlocks != null) {
                    for (Block block : tagBlocks) {
                        writer.beginObject();
                        entry.clone().mT(null).mB(block).write(writer);
                        writer.endObject();
                    }
                } else {
                    writer.beginObject();
                    entry.write(writer);
                    writer.endObject();
                }
            }

            writer.endArray();
            writer.endObject();
        } catch (IOException e) {
            HbmNuclearTechMod.LOGGER.error("Failed to write fallout template {}", file, e);
        }
    }

    private static List<FalloutEntry> readConfig(Path config) {
        try (Reader reader = Files.newBufferedReader(config)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json == null || !json.has("entries")) {
                return null;
            }
            JsonArray recipes = json.getAsJsonArray("entries");
            List<FalloutEntry> conf = new ArrayList<>();
            for (JsonElement recipe : recipes) {
                FalloutEntry entry = FalloutEntry.readEntry(recipe);
                if (entry != null) {
                    conf.add(entry);
                }
            }
            return conf;
        } catch (Exception ex) {
            HbmNuclearTechMod.LOGGER.error("Failed to read fallout config {}", config, ex);
        }
        return null;
    }

    public static boolean matchesMaterialKey(String key, BlockState state) {
        if (key == null) {
            return false;
        }
        Predicate<BlockState> predicate = materialPredicate(key);
        return predicate != null && predicate.test(state);
    }

    private static Predicate<BlockState> materialPredicate(String key) {
        return switch (key) {
            case "wood" -> FalloutConfigJSON::isWood;
            case "leaves" -> s -> s.is(BlockTags.LEAVES) || s.getBlock() instanceof LeavesBlock;
            case "plants" -> FalloutConfigJSON::isPlant;
            case "vine" -> s -> s.is(Blocks.VINE) || s.is(Blocks.CAVE_VINES) || s.is(Blocks.CAVE_VINES_PLANT)
                    || s.is(Blocks.TWISTING_VINES) || s.is(Blocks.TWISTING_VINES_PLANT)
                    || s.is(Blocks.WEEPING_VINES) || s.is(Blocks.WEEPING_VINES_PLANT);
            case "rock" -> FalloutConfigJSON::isRock;
            case "iron" -> FalloutConfigJSON::isIron;
            case "sand" -> s -> s.is(BlockTags.SAND) || s.is(Blocks.SAND) || s.is(Blocks.RED_SAND)
                    || s.is(Blocks.SUSPICIOUS_SAND);
            case "ground" -> FalloutConfigJSON::isGround;
            case "grass" -> s -> s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.PODZOL) || s.is(Blocks.MYCELIUM);
            case "water" -> s -> s.getFluidState().is(net.minecraft.world.level.material.Fluids.WATER);
            case "lava" -> s -> s.getFluidState().is(net.minecraft.world.level.material.Fluids.LAVA);
            case "fire" -> s -> s.is(Blocks.FIRE) || s.is(Blocks.SOUL_FIRE);
            case "ice" -> s -> s.is(Blocks.ICE) || s.is(Blocks.FROSTED_ICE);
            case "packedIce" -> s -> s.is(Blocks.PACKED_ICE) || s.is(Blocks.BLUE_ICE);
            case "snow" -> s -> s.is(Blocks.SNOW) || s.is(Blocks.SNOW_BLOCK);
            case "craftedSnow" -> s -> s.is(Blocks.SNOW_BLOCK);
            case "clay" -> s -> s.is(Blocks.CLAY);
            case "cactus" -> s -> s.is(Blocks.CACTUS);
            case "cloth" -> s -> s.is(BlockTags.WOOL);
            case "carpet" -> s -> s.is(BlockTags.WOOL_CARPETS);
            case "tnt" -> s -> s.is(Blocks.TNT);
            case "web" -> s -> s.is(Blocks.COBWEB);
            case "anvil" -> s -> s.is(Blocks.ANVIL) || s.is(Blocks.CHIPPED_ANVIL) || s.is(Blocks.DAMAGED_ANVIL);
            case "sponge" -> s -> s.is(Blocks.SPONGE) || s.is(Blocks.WET_SPONGE);
            case "portal" -> s -> s.is(Blocks.NETHER_PORTAL) || s.is(Blocks.END_PORTAL) || s.is(Blocks.END_GATEWAY);
            case "cake" -> s -> s.is(Blocks.CAKE);
            case "dragonEgg" -> s -> s.is(Blocks.DRAGON_EGG);
            case "gourd" -> s -> s.is(Blocks.PUMPKIN) || s.is(Blocks.CARVED_PUMPKIN) || s.is(Blocks.MELON)
                    || s.is(Blocks.JACK_O_LANTERN);
            case "coral" -> s -> s.is(BlockTags.CORALS) || s.is(BlockTags.CORAL_BLOCKS) || s.is(BlockTags.WALL_CORALS);
            case "circuits", "redstoneLight" -> s -> s.is(Blocks.REDSTONE_LAMP) || s.is(Blocks.REDSTONE_BLOCK)
                    || s.is(Blocks.REDSTONE_WIRE) || s.is(Blocks.REPEATER) || s.is(Blocks.COMPARATOR);
            default -> null;
        };
    }

    private static boolean isWood(BlockState state) {
        if (state.is(ModBlocks.WASTE_LOG.get()) || state.is(ModBlocks.WASTE_PLANKS.get())) {
            return false;
        }
        return state.is(BlockTags.LOGS)
                || state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOODEN_FENCES)
                || state.is(BlockTags.FENCE_GATES)
                || state.is(BlockTags.WOODEN_STAIRS)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_DOORS)
                || state.is(BlockTags.WOODEN_TRAPDOORS)
                || state.is(BlockTags.WOODEN_BUTTONS)
                || state.is(BlockTags.WOODEN_PRESSURE_PLATES);
    }

    private static boolean isPlant(BlockState state) {
        return state.is(BlockTags.FLOWERS)
                || state.getBlock() instanceof BushBlock
                || state.is(Blocks.GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.DEAD_BUSH)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.MOSS_CARPET)
                || state.is(Blocks.GLOW_LICHEN);
    }

    private static boolean isRock(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.BASE_STONE_NETHER)
                || state.is(BlockTags.STONE_ORE_REPLACEABLES)
                || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
                || state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.MOSSY_COBBLESTONE)
                || state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.NETHERRACK)
                || state.is(Blocks.BASALT)
                || state.is(Blocks.BLACKSTONE)
                || state.is(Blocks.GRAVEL);
    }

    private static boolean isIron(BlockState state) {
        return state.is(Blocks.IRON_BLOCK)
                || state.is(Blocks.IRON_BARS)
                || state.is(Blocks.IRON_DOOR)
                || state.is(Blocks.IRON_TRAPDOOR)
                || state.is(Blocks.RAW_IRON_BLOCK)
                || state.is(Blocks.GOLD_BLOCK)
                || state.is(Blocks.RAW_GOLD_BLOCK)
                || state.is(Blocks.COPPER_BLOCK)
                || state.is(Blocks.RAW_COPPER_BLOCK)
                || state.is(Blocks.CUT_COPPER)
                || state.is(Blocks.EXPOSED_COPPER)
                || state.is(Blocks.WEATHERED_COPPER)
                || state.is(Blocks.OXIDIZED_COPPER)
                || state.is(Blocks.ANVIL)
                || state.is(Blocks.CHIPPED_ANVIL)
                || state.is(Blocks.DAMAGED_ANVIL)
                || state.is(Blocks.CHAIN)
                || state.is(Blocks.LANTERN)
                || state.is(Blocks.SOUL_LANTERN)
                || state.is(Blocks.HOPPER)
                || state.is(Blocks.CAULDRON)
                || state.is(Blocks.LIGHTNING_ROD);
    }

    private static boolean isGround(BlockState state) {
        // Exclude grass-family (legacy Material.grass was separate from Material.ground)
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM)) {
            return false;
        }
        return state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.FARMLAND)
                || state.is(Blocks.DIRT_PATH)
                || state.is(Blocks.MUD)
                || state.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || (state.is(BlockTags.DIRT)
                && !state.is(Blocks.GRASS_BLOCK)
                && !state.is(Blocks.PODZOL)
                && !state.is(Blocks.MYCELIUM)
                && !state.is(Blocks.MOSS_BLOCK));
    }

    /** Weighted outcome; {@code meta} is legacy intensity for sellafield_slaked ({@code -1} = none). */
    public record WeightedBlock(Block block, int weight, int meta) {
        public WeightedBlock(Block block, int weight) {
            this(block, weight, -1);
        }
    }

    public static class FalloutEntry {
        private Block matchesBlock = null;
        /** Optional; used for sand color in legacy. Prefer separate Blocks.SAND / RED_SAND. */
        private int matchesMeta = -1;
        private String matchesMaterial = null;
        private net.minecraft.tags.TagKey<Block> matchesTag = null;
        private boolean matchesOpaque = false;

        private WeightedBlock[] primaryBlocks = null;
        private WeightedBlock[] secondaryBlocks = null;
        private double primaryChance = 1.0D;
        private double minDist = 0.0D;
        private double maxDist = 100.0D;
        private double falloffStart = 0.9D;

        /** Whether the depth value should be decremented when this block is converted. */
        private boolean isSolid = false;

        public FalloutEntry clone() {
            FalloutEntry entry = new FalloutEntry();
            entry.mB(matchesBlock);
            entry.mM(matchesMeta);
            entry.mMa(matchesMaterial);
            entry.mT(matchesTag);
            entry.mO(matchesOpaque);
            entry.prim(primaryBlocks);
            entry.sec(secondaryBlocks);
            entry.c(primaryChance);
            entry.min(minDist);
            entry.max(maxDist);
            entry.fo(falloffStart);
            entry.sol(isSolid);
            return entry;
        }

        public FalloutEntry mB(Block block) {
            this.matchesBlock = block;
            return this;
        }

        public FalloutEntry mM(int meta) {
            this.matchesMeta = meta;
            return this;
        }

        public FalloutEntry mMa(String mat) {
            this.matchesMaterial = mat;
            return this;
        }

        public FalloutEntry mT(net.minecraft.tags.TagKey<Block> tag) {
            this.matchesTag = tag;
            return this;
        }

        public FalloutEntry mO(boolean opaque) {
            this.matchesOpaque = opaque;
            return this;
        }

        public FalloutEntry prim(WeightedBlock... blocks) {
            this.primaryBlocks = blocks;
            return this;
        }

        public FalloutEntry sec(WeightedBlock... blocks) {
            this.secondaryBlocks = blocks;
            return this;
        }

        public FalloutEntry c(double chance) {
            this.primaryChance = chance;
            return this;
        }

        public FalloutEntry min(double min) {
            this.minDist = min;
            return this;
        }

        public FalloutEntry max(double max) {
            this.maxDist = max;
            return this;
        }

        public FalloutEntry fo(double falloffStart) {
            this.falloffStart = falloffStart;
            return this;
        }

        public FalloutEntry sol(boolean solid) {
            this.isSolid = solid;
            return this;
        }

        public boolean eval(Level level, BlockPos pos, BlockState state, double dist) {
            if (dist > maxDist || dist < minDist) {
                return false;
            }
            Block b = state.getBlock();
            if (matchesBlock != null && b != matchesBlock) {
                return false;
            }
            if (matchesTag != null && !state.is(matchesTag)) {
                return false;
            }
            if (matchesMaterial != null && !matchesMaterialKey(matchesMaterial, state)) {
                return false;
            }
            // matchesMeta kept for JSON compat; sand colors are separate blocks in 1.20
            if (matchesOpaque && !state.canOcclude()) {
                return false;
            }
            if (dist > maxDist * falloffStart
                    && Math.abs(level.random.nextGaussian()) < Math.pow(
                    (dist - maxDist * falloffStart) / (maxDist - maxDist * falloffStart), 2D) * 3D) {
                return false;
            }

            WeightedBlock conversion = chooseRandomOutcome(
                    (primaryChance == 1D || rand.nextDouble() < primaryChance) ? primaryBlocks : secondaryBlocks);

            if (conversion != null && conversion.block() != null) {
                Block out = conversion.block();
                int outMeta = conversion.meta();

                if (out instanceof SellafieldSlakedBlock && state.getBlock() instanceof SellafieldSlakedBlock) {
                    int existing = state.getValue(SellafieldSlakedBlock.INTENSITY);
                    int next = outMeta >= 0 ? outMeta : 0;
                    if (next <= existing) {
                        return false;
                    }
                } else if (isSellafieldSurface(out) && isSellafieldSurface(b)
                        && sellafieldRank(out) <= sellafieldRank(b)) {
                    return false;
                }
                if (out == ModBlocks.SELLAFIELD_BEDROCK.get() && b == ModBlocks.SELLAFIELD_BEDROCK.get()) {
                    return false;
                }
                if (b == ModBlocks.SELLAFIELD_BEDROCK.get() && out != ModBlocks.SELLAFIELD_BEDROCK.get()) {
                    return false;
                }
                if (pos.getY() == level.getMinBuildHeight() && out != ModBlocks.SELLAFIELD_BEDROCK.get()) {
                    return false;
                }

                BlockState placed;
                if (out instanceof SellafieldSlakedBlock slaked) {
                    placed = slaked.stateFor(outMeta >= 0 ? outMeta : 0, pos);
                } else {
                    placed = out.defaultBlockState();
                }
                level.setBlock(pos, placed, 3);
                return true;
            }

            return false;
        }

        private WeightedBlock chooseRandomOutcome(WeightedBlock[] blocks) {
            if (blocks == null || blocks.length == 0) {
                return null;
            }

            int weight = 0;
            for (WeightedBlock choice : blocks) {
                if (choice != null) {
                    weight += choice.weight();
                }
            }
            if (weight <= 0) {
                return blocks[0];
            }

            int r = rand.nextInt(weight);
            for (WeightedBlock choice : blocks) {
                if (choice == null) {
                    continue;
                }
                r -= choice.weight();
                if (r <= 0) {
                    return choice;
                }
            }
            return blocks[0];
        }

        public boolean isSolid() {
            return this.isSolid;
        }

        /**
         * When this entry matches a block tag, return concrete blocks for template serialization.
         * Runtime {@link #eval} still uses the tag directly.
         */
        List<Block> resolveTagBlocksForWrite() {
            if (matchesTag == null) {
                return null;
            }
            List<Block> blocks = new ArrayList<>();
            var tag = ForgeRegistries.BLOCKS.tags();
            if (tag != null) {
                var holders = tag.getTag(matchesTag);
                if (holders != null) {
                    for (Block block : holders) {
                        blocks.add(block);
                    }
                }
            }
            if (blocks.isEmpty()) {
                blocks.addAll(fallbackBlocksForTag(matchesTag));
            }
            return blocks;
        }

        private static List<Block> fallbackBlocksForTag(net.minecraft.tags.TagKey<Block> tag) {
            List<Block> list = new ArrayList<>();
            if (tag == BlockTags.LOGS) {
                list.add(Blocks.OAK_LOG);
                list.add(Blocks.SPRUCE_LOG);
                list.add(Blocks.BIRCH_LOG);
                list.add(Blocks.JUNGLE_LOG);
                list.add(Blocks.ACACIA_LOG);
                list.add(Blocks.DARK_OAK_LOG);
                list.add(Blocks.MANGROVE_LOG);
                list.add(Blocks.CHERRY_LOG);
                list.add(Blocks.CRIMSON_STEM);
                list.add(Blocks.WARPED_STEM);
            } else if (tag == BlockTags.PLANKS) {
                list.add(Blocks.OAK_PLANKS);
                list.add(Blocks.SPRUCE_PLANKS);
                list.add(Blocks.BIRCH_PLANKS);
                list.add(Blocks.JUNGLE_PLANKS);
                list.add(Blocks.ACACIA_PLANKS);
                list.add(Blocks.DARK_OAK_PLANKS);
                list.add(Blocks.MANGROVE_PLANKS);
                list.add(Blocks.CHERRY_PLANKS);
                list.add(Blocks.CRIMSON_PLANKS);
                list.add(Blocks.WARPED_PLANKS);
                list.add(Blocks.BAMBOO_PLANKS);
            } else if (tag == BlockTags.LEAVES) {
                list.add(Blocks.OAK_LEAVES);
                list.add(Blocks.SPRUCE_LEAVES);
                list.add(Blocks.BIRCH_LEAVES);
                list.add(Blocks.JUNGLE_LEAVES);
                list.add(Blocks.ACACIA_LEAVES);
                list.add(Blocks.DARK_OAK_LEAVES);
                list.add(Blocks.MANGROVE_LEAVES);
                list.add(Blocks.CHERRY_LEAVES);
                list.add(Blocks.AZALEA_LEAVES);
                list.add(Blocks.FLOWERING_AZALEA_LEAVES);
            }
            return list;
        }

        public void write(JsonWriter writer) throws IOException {
            if (matchesBlock != null) {
                ResourceLocation key = ForgeRegistries.BLOCKS.getKey(matchesBlock);
                if (key != null) {
                    writer.name("matchesBlock").value(key.toString());
                }
            }
            if (matchesMeta != -1) {
                writer.name("matchesMeta").value(matchesMeta);
            }
            if (matchesOpaque) {
                writer.name("mustBeOpaque").value(true);
            }
            if (matchesMaterial != null && matNames.containsKey(matchesMaterial)) {
                writer.name("matchesMaterial").value(matchesMaterial);
            }
            if (isSolid) {
                writer.name("restrictDepth").value(true);
            }

            if (primaryBlocks != null) {
                writer.name("primarySubstitution");
                writeMetaArray(writer, primaryBlocks);
            }
            if (secondaryBlocks != null) {
                writer.name("secondarySubstitutions");
                writeMetaArray(writer, secondaryBlocks);
            }

            if (primaryChance != 1D) {
                writer.name("chance").value(primaryChance);
            }
            if (minDist != 0.0D) {
                writer.name("minimumDistancePercent").value(minDist);
            }
            if (maxDist != 100.0D) {
                writer.name("maximumDistancePercent").value(maxDist);
            }
            if (falloffStart != 0.9D) {
                writer.name("falloffStartFactor").value(falloffStart);
            }
        }

        private static FalloutEntry readEntry(JsonElement recipe) {
            FalloutEntry entry = new FalloutEntry();
            if (!recipe.isJsonObject()) {
                return null;
            }

            JsonObject obj = recipe.getAsJsonObject();

            if (obj.has("matchesBlock")) {
                Block block = resolveBlock(obj.get("matchesBlock").getAsString());
                if (block != null) {
                    entry.mB(block);
                }
            }
            if (obj.has("matchesMeta")) {
                entry.mM(obj.get("matchesMeta").getAsInt());
            }
            if (obj.has("mustBeOpaque")) {
                entry.mO(obj.get("mustBeOpaque").getAsBoolean());
            }
            // Fix legacy bug: previously read mustBeOpaque key for material
            if (obj.has("matchesMaterial")) {
                String mat = obj.get("matchesMaterial").getAsString();
                if (matNames.containsKey(mat) || materialPredicate(mat) != null) {
                    entry.mMa(mat);
                }
            }
            if (obj.has("restrictDepth")) {
                entry.sol(obj.get("restrictDepth").getAsBoolean());
            }

            if (obj.has("primarySubstitution")) {
                entry.prim(readMetaArray(obj.get("primarySubstitution")));
            }
            if (obj.has("secondarySubstitutions")) {
                entry.sec(readMetaArray(obj.get("secondarySubstitutions")));
            }

            if (obj.has("chance")) {
                entry.c(obj.get("chance").getAsDouble());
            }
            if (obj.has("minimumDistancePercent")) {
                entry.min(obj.get("minimumDistancePercent").getAsDouble());
            }
            if (obj.has("maximumDistancePercent")) {
                entry.max(obj.get("maximumDistancePercent").getAsDouble());
            }
            if (obj.has("falloffStartFactor")) {
                entry.fo(obj.get("falloffStartFactor").getAsDouble());
            }

            return entry;
        }

        private static Block resolveBlock(String name) {
            if (name == null || name.isEmpty()) {
                return null;
            }
            ResourceLocation id = ResourceLocation.tryParse(name);
            if (id == null || !ForgeRegistries.BLOCKS.containsKey(id)) {
                return null;
            }
            return ForgeRegistries.BLOCKS.getValue(id);
        }

        private static void writeMetaArray(JsonWriter writer, WeightedBlock[] array) throws IOException {
            writer.beginArray();
            writer.setIndent("");

            for (WeightedBlock meta : array) {
                if (meta == null || meta.block() == null) {
                    continue;
                }
                ResourceLocation key = ForgeRegistries.BLOCKS.getKey(meta.block());
                writer.beginArray();
                writer.value(key != null ? key.toString() : "minecraft:air");
                writer.value(Math.max(0, meta.meta()));
                writer.value(meta.weight());
                writer.endArray();
            }

            writer.endArray();
            writer.setIndent("  ");
        }

        private static WeightedBlock[] readMetaArray(JsonElement jsonElement) {
            if (!jsonElement.isJsonArray()) {
                return null;
            }

            JsonArray array = jsonElement.getAsJsonArray();
            WeightedBlock[] metaArray = new WeightedBlock[array.size()];

            for (int i = 0; i < metaArray.length; i++) {
                JsonElement metaBlock = array.get(i);
                if (!metaBlock.isJsonArray()) {
                    throw new IllegalStateException("Could not read meta block " + metaBlock);
                }
                JsonArray mBArray = metaBlock.getAsJsonArray();
                // [blockName, metaIgnored, weight] — meta parsed then ignored
                Block block = resolveBlock(mBArray.get(0).getAsString());
                int meta = mBArray.size() >= 2 ? mBArray.get(1).getAsInt() : -1;
                int weight = mBArray.size() >= 3 ? mBArray.get(2).getAsInt() : 1;
                metaArray[i] = new WeightedBlock(block, weight, meta);
            }

            return metaArray;
        }
    }
}
