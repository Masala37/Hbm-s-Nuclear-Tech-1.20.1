package com.hbm.registry;

import com.hbm.lib.RefStrings;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Tag keys for early content. JSON datapack entries live under data/&lt;namespace&gt;/tags/.
 */
public final class ModTags {
    public static final class Blocks {
        public static final TagKey<Block> ORES_URANIUM = mod("ores/uranium");
        public static final TagKey<Block> STORAGE_BLOCKS_URANIUM = mod("storage_blocks/uranium");
        public static final TagKey<Block> RBMK_COLUMNS = mod("rbmk_columns");
        public static final TagKey<Block> RBMK_CASING = mod("rbmk_casing");

        public static final TagKey<Block> FORGE_ORES_URANIUM = forge("ores/uranium");
        public static final TagKey<Block> FORGE_ORES = forge("ores");
        public static final TagKey<Block> FORGE_STORAGE_BLOCKS_URANIUM = forge("storage_blocks/uranium");
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
        public static final TagKey<Item> STORAGE_BLOCKS_URANIUM = mod("storage_blocks/uranium");
        public static final TagKey<Item> INGOTS_URANIUM = mod("ingots/uranium");
        public static final TagKey<Item> RBMK_COLUMNS = mod("rbmk_columns");
        public static final TagKey<Item> RBMK_CASING = mod("rbmk_casing");

        public static final TagKey<Item> FORGE_ORES_URANIUM = forge("ores/uranium");
        public static final TagKey<Item> FORGE_ORES = forge("ores");
        public static final TagKey<Item> FORGE_STORAGE_BLOCKS_URANIUM = forge("storage_blocks/uranium");
        public static final TagKey<Item> FORGE_STORAGE_BLOCKS = forge("storage_blocks");
        public static final TagKey<Item> FORGE_INGOTS_URANIUM = forge("ingots/uranium");
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
