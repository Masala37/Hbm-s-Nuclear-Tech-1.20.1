package com.hbm.registry;

import com.hbm.lib.RefStrings;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Tag keys for early content. JSON datapack entries live under data/namespace/tags/.
 */
public final class ModTags {
    public static final class Blocks {
        public static final TagKey<Block> ORES_URANIUM = mod("ores/uranium");
        public static final TagKey<Block> ORES_TITANIUM = mod("ores/titanium");
        public static final TagKey<Block> ORES_TUNGSTEN = mod("ores/tungsten");
        public static final TagKey<Block> ORES_ALUMINIUM = mod("ores/aluminium");
        public static final TagKey<Block> ORES_LEAD = mod("ores/lead");
        public static final TagKey<Block> ORES_BERYLLIUM = mod("ores/beryllium");
        public static final TagKey<Block> ORES_COBALT = mod("ores/cobalt");

        public static final TagKey<Block> STORAGE_BLOCKS_URANIUM = mod("storage_blocks/uranium");
        public static final TagKey<Block> STORAGE_BLOCKS_TITANIUM = mod("storage_blocks/titanium");
        public static final TagKey<Block> STORAGE_BLOCKS_TUNGSTEN = mod("storage_blocks/tungsten");
        public static final TagKey<Block> STORAGE_BLOCKS_ALUMINIUM = mod("storage_blocks/aluminium");
        public static final TagKey<Block> STORAGE_BLOCKS_LEAD = mod("storage_blocks/lead");
        public static final TagKey<Block> STORAGE_BLOCKS_BERYLLIUM = mod("storage_blocks/beryllium");
        public static final TagKey<Block> STORAGE_BLOCKS_STEEL = mod("storage_blocks/steel");
        public static final TagKey<Block> STORAGE_BLOCKS_COBALT = mod("storage_blocks/cobalt");
        public static final TagKey<Block> STORAGE_BLOCKS_LITHIUM = mod("storage_blocks/lithium");
        public static final TagKey<Block> STORAGE_BLOCKS_GRAPHITE = mod("storage_blocks/graphite");
        public static final TagKey<Block> STORAGE_BLOCKS_DESH = mod("storage_blocks/desh");

        public static final TagKey<Block> RBMK_COLUMNS = mod("rbmk_columns");
        public static final TagKey<Block> RBMK_CASING = mod("rbmk_casing");

        public static final TagKey<Block> FORGE_ORES = forge("ores");
        public static final TagKey<Block> FORGE_STORAGE_BLOCKS = forge("storage_blocks");

        private Blocks() {
        }

        private static TagKey<Block> mod(String path) {
            return TagKey.create(Registries.BLOCK, new ResourceLocation(RefStrings.MODID, path));
        }

        private static TagKey<Block> forge(String path) {
            return TagKey.create(Registries.BLOCK, new ResourceLocation("forge", path));
        }
    }

    public static final class Items {
        public static final TagKey<Item> ORES_URANIUM = mod("ores/uranium");
        public static final TagKey<Item> ORES_TITANIUM = mod("ores/titanium");
        public static final TagKey<Item> ORES_TUNGSTEN = mod("ores/tungsten");
        public static final TagKey<Item> ORES_ALUMINIUM = mod("ores/aluminium");
        public static final TagKey<Item> ORES_LEAD = mod("ores/lead");
        public static final TagKey<Item> ORES_BERYLLIUM = mod("ores/beryllium");
        public static final TagKey<Item> ORES_COBALT = mod("ores/cobalt");

        public static final TagKey<Item> STORAGE_BLOCKS_URANIUM = mod("storage_blocks/uranium");
        public static final TagKey<Item> STORAGE_BLOCKS_TITANIUM = mod("storage_blocks/titanium");
        public static final TagKey<Item> STORAGE_BLOCKS_TUNGSTEN = mod("storage_blocks/tungsten");
        public static final TagKey<Item> STORAGE_BLOCKS_ALUMINIUM = mod("storage_blocks/aluminium");
        public static final TagKey<Item> STORAGE_BLOCKS_LEAD = mod("storage_blocks/lead");
        public static final TagKey<Item> STORAGE_BLOCKS_BERYLLIUM = mod("storage_blocks/beryllium");
        public static final TagKey<Item> STORAGE_BLOCKS_STEEL = mod("storage_blocks/steel");
        public static final TagKey<Item> STORAGE_BLOCKS_COBALT = mod("storage_blocks/cobalt");
        public static final TagKey<Item> STORAGE_BLOCKS_LITHIUM = mod("storage_blocks/lithium");
        public static final TagKey<Item> STORAGE_BLOCKS_GRAPHITE = mod("storage_blocks/graphite");
        public static final TagKey<Item> STORAGE_BLOCKS_DESH = mod("storage_blocks/desh");

        public static final TagKey<Item> INGOTS_URANIUM = mod("ingots/uranium");
        public static final TagKey<Item> INGOTS_TITANIUM = mod("ingots/titanium");
        public static final TagKey<Item> INGOTS_TUNGSTEN = mod("ingots/tungsten");
        public static final TagKey<Item> INGOTS_ALUMINIUM = mod("ingots/aluminium");
        public static final TagKey<Item> INGOTS_LEAD = mod("ingots/lead");
        public static final TagKey<Item> INGOTS_BERYLLIUM = mod("ingots/beryllium");
        public static final TagKey<Item> INGOTS_STEEL = mod("ingots/steel");
        public static final TagKey<Item> INGOTS_COBALT = mod("ingots/cobalt");
        public static final TagKey<Item> INGOTS_GRAPHITE = mod("ingots/graphite");
        public static final TagKey<Item> INGOTS_DESH = mod("ingots/desh");
        public static final TagKey<Item> INGOTS_LITHIUM = mod("ingots/lithium");

        public static final TagKey<Item> RBMK_COLUMNS = mod("rbmk_columns");
        public static final TagKey<Item> RBMK_CASING = mod("rbmk_casing");

        public static final TagKey<Item> FORGE_ORES = forge("ores");
        public static final TagKey<Item> FORGE_STORAGE_BLOCKS = forge("storage_blocks");
        public static final TagKey<Item> FORGE_INGOTS = forge("ingots");

        private Items() {
        }

        private static TagKey<Item> mod(String path) {
            return TagKey.create(Registries.ITEM, new ResourceLocation(RefStrings.MODID, path));
        }

        private static TagKey<Item> forge(String path) {
            return TagKey.create(Registries.ITEM, new ResourceLocation("forge", path));
        }
    }

    private ModTags() {
    }
}
