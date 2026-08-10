package com.hbm.blockentity.bomb;

import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

/**
 * Yield contributions for Custom Nuke inventory items (legacy {@code TileEntityNukeCustom.registerBombItems}).
 */
public final class NukeCustomEntries {
    public enum BombType {
        TNT, NUKE, HYDRO, AMAT, DIRTY, SCHRAB, EUPH
    }

    public enum EntryType {
        ADD, MULT
    }

    public record Entry(BombType type, EntryType entry, float value) {
        public Entry(BombType type, float value) {
            this(type, EntryType.ADD, value);
        }
    }

    private static final Map<Item, Entry> ENTRIES = new HashMap<>();
    private static boolean registered;

    private NukeCustomEntries() {
    }

    public static Entry get(Item item) {
        ensureRegistered();
        return ENTRIES.get(item);
    }

    public static boolean isRegistered(Item item) {
        return get(item) != null || item == ModItems.CUSTOM_FALL.get();
    }

    public static synchronized void ensureRegistered() {
        if (registered) {
            return;
        }
        registered = true;

        put(Items.GUNPOWDER, new Entry(BombType.TNT, 0.8F));
        put(Items.TNT, new Entry(BombType.TNT, 4.0F));
        put(ModBlocks.DET_CORD.get().asItem(), new Entry(BombType.TNT, 1.5F));
        put(ModItems.INGOT_SEMTEX.get(), new Entry(BombType.TNT, 8.0F));
        put(ModBlocks.DET_CHARGE.get().asItem(), new Entry(BombType.TNT, 15.0F));
        put(ModBlocks.BARREL_RED.get().asItem(), new Entry(BombType.TNT, 2.5F));
        put(ModBlocks.BARREL_PINK.get().asItem(), new Entry(BombType.TNT, 4.0F));
        put(ModItems.CUSTOM_TNT.get(), new Entry(BombType.TNT, 10.0F));

        put(ModItems.INGOT_U233.get(), new Entry(BombType.NUKE, 15.0F));
        put(ModItems.INGOT_U235.get(), new Entry(BombType.NUKE, 15.0F));
        put(ModItems.INGOT_PU239.get(), new Entry(BombType.NUKE, 25.0F));
        put(ModItems.INGOT_PU241.get(), new Entry(BombType.NUKE, 25.0F));
        put(ModItems.INGOT_NEPTUNIUM.get(), new Entry(BombType.NUKE, 30.0F));
        put(ModItems.NUGGET_U233.get(), new Entry(BombType.NUKE, 1.5F));
        put(ModItems.NUGGET_U235.get(), new Entry(BombType.NUKE, 1.5F));
        put(ModItems.NUGGET_PU239.get(), new Entry(BombType.NUKE, 2.5F));
        put(ModItems.NUGGET_PU241.get(), new Entry(BombType.NUKE, 2.5F));
        put(ModItems.NUGGET_NEPTUNIUM.get(), new Entry(BombType.NUKE, 3.0F));
        put(ModItems.POWDER_NEPTUNIUM.get(), new Entry(BombType.NUKE, 30.0F));
        put(ModItems.CUSTOM_NUKE_PART.get(), new Entry(BombType.NUKE, 30.0F));

        put(ModItems.CELL_DEUTERIUM.get(), new Entry(BombType.HYDRO, 20.0F));
        put(ModItems.CELL_TRITIUM.get(), new Entry(BombType.HYDRO, 30.0F));
        put(ModItems.LITHIUM.get(), new Entry(BombType.HYDRO, 20.0F));
        put(ModItems.CUSTOM_HYDRO.get(), new Entry(BombType.HYDRO, 30.0F));

        put(ModItems.CELL_ANTIMATTER.get(), new Entry(BombType.AMAT, 5.0F));
        put(ModItems.CUSTOM_AMAT.get(), new Entry(BombType.AMAT, 15.0F));
        put(ModItems.EGG_BALEFIRE.get(), new Entry(BombType.AMAT, 150.0F));
        put(ModItems.EGG_BALEFIRE_SHARD.get(), new Entry(BombType.AMAT, 15.0F));

        put(ModItems.TUNGSTEN_INGOT.get(), new Entry(BombType.DIRTY, 1.0F));
        put(ModItems.CUSTOM_DIRTY.get(), new Entry(BombType.DIRTY, 10.0F));

        put(ModItems.SCHRABIDIUM_INGOT.get(), new Entry(BombType.SCHRAB, 5.0F));
        put(ModBlocks.BLOCK_SCHRABIDIUM.get().asItem(), new Entry(BombType.SCHRAB, 50.0F));
        put(ModItems.NUGGET_SCHRABIDIUM.get(), new Entry(BombType.SCHRAB, 0.5F));
        put(ModItems.POWDER_SCHRABIDIUM.get(), new Entry(BombType.SCHRAB, 5.0F));
        put(ModItems.CELL_SAS3.get(), new Entry(BombType.SCHRAB, 7.5F));
        put(ModItems.CELL_ANTI_SCHRABIDIUM.get(), new Entry(BombType.SCHRAB, 15.0F));
        put(ModItems.CUSTOM_SCHRAB.get(), new Entry(BombType.SCHRAB, 15.0F));

        put(ModItems.NUGGET_EUPHEMIUM.get(), new Entry(BombType.EUPH, 1.0F));
        put(ModItems.EUPHEMIUM_INGOT.get(), new Entry(BombType.EUPH, 1.0F));

        put(Items.REDSTONE, new Entry(BombType.TNT, EntryType.MULT, 1.05F));
        put(Blocks.REDSTONE_BLOCK.asItem(), new Entry(BombType.TNT, EntryType.MULT, 1.5F));

        put(ModItems.URANIUM_INGOT.get(), new Entry(BombType.NUKE, EntryType.MULT, 1.05F));
        put(ModItems.INGOT_PLUTONIUM.get(), new Entry(BombType.NUKE, EntryType.MULT, 1.15F));
        put(ModItems.INGOT_U238.get(), new Entry(BombType.NUKE, EntryType.MULT, 1.1F));
        put(ModItems.INGOT_PU238.get(), new Entry(BombType.NUKE, EntryType.MULT, 1.15F));
        put(ModItems.NUGGET_URANIUM.get(), new Entry(BombType.NUKE, EntryType.MULT, 1.005F));
        put(ModItems.NUGGET_PLUTONIUM.get(), new Entry(BombType.NUKE, EntryType.MULT, 1.15F));
        put(ModItems.NUGGET_U238.get(), new Entry(BombType.NUKE, EntryType.MULT, 1.01F));
        put(ModItems.NUGGET_PU238.get(), new Entry(BombType.NUKE, EntryType.MULT, 1.015F));
        put(ModItems.POWDER_URANIUM.get(), new Entry(BombType.NUKE, EntryType.MULT, 1.05F));
        put(ModItems.POWDER_PLUTONIUM.get(), new Entry(BombType.NUKE, EntryType.MULT, 1.15F));

        put(ModItems.INGOT_PU240.get(), new Entry(BombType.DIRTY, EntryType.MULT, 1.05F));
        put(ModBlocks.BLOCK_WASTE.get().asItem(), new Entry(BombType.DIRTY, EntryType.MULT, 1.25F));
    }

    private static void put(Item item, Entry entry) {
        ENTRIES.put(item, entry);
    }
}
