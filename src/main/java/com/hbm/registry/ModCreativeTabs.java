package com.hbm.registry;

import com.hbm.HbmNuclearTechMod;
import com.hbm.blocks.bomb.CrashedBombBlock;
import com.hbm.blocks.bomb.DudType;
import com.hbm.blocks.bomb.VolcanoBlock;
import com.hbm.blocks.bomb.VolcanoMode;
import com.hbm.items.tool.BombCallerItem;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.lib.RefStrings;
import com.hbm.port.PortContentRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Nine legacy HBM creative tabs. Contents are classified via {@link CreativeTabClassifier}
 * so every registered HBM item appears in exactly one tab.
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RefStrings.MODID);

    public static final RegistryObject<CreativeModeTab> PARTS_TAB = tab(
            "parts", "itemGroup.hbm.parts", () -> iconItem("ingot_uranium", "ingot_steel"),
            CreativeTabClassifier.Kind.PARTS);

    public static final RegistryObject<CreativeModeTab> CONTROL_TAB = tab(
            "control", "itemGroup.hbm.control", () -> iconItem("pellet_rtg", "upgrade_speed_1"),
            CreativeTabClassifier.Kind.CONTROL);

    public static final RegistryObject<CreativeModeTab> TEMPLATE_TAB = tab(
            "template", "itemGroup.hbm.template", () -> iconItem("blueprints", "upgrade_template", "template_folder"),
            CreativeTabClassifier.Kind.TEMPLATE, true);

    public static final RegistryObject<CreativeModeTab> BLOCK_TAB = tab(
            "blocks", "itemGroup.hbm.blocks", () -> iconBlock("ore_uranium", "ore_titanium"),
            CreativeTabClassifier.Kind.BLOCKS);

    public static final RegistryObject<CreativeModeTab> MACHINE_TAB = tab(
            "machines", "itemGroup.hbm.machines", () -> iconBlock("pwr_controller", "electric_furnace"),
            CreativeTabClassifier.Kind.MACHINE);

    public static final RegistryObject<CreativeModeTab> NUKE_TAB = tab(
            "nukes", "itemGroup.hbm.nukes", () -> iconBlock("nuke_man", "nuke_gadget"),
            CreativeTabClassifier.Kind.NUKE);

    public static final RegistryObject<CreativeModeTab> MISSILE_TAB = tab(
            "missiles", "itemGroup.hbm.missiles", () -> iconItem("missile_nuclear", "missile_generic"),
            CreativeTabClassifier.Kind.MISSILE);

    public static final RegistryObject<CreativeModeTab> WEAPON_TAB = tab(
            "weapons", "itemGroup.hbm.weapons", () -> iconItem("ammo_45", "ammo_shell", "detonator"),
            CreativeTabClassifier.Kind.WEAPON);

    public static final RegistryObject<CreativeModeTab> CONSUMABLE_TAB = tab(
            "consumables", "itemGroup.hbm.consumables", () -> iconItem("bottle_nuka", "cap_nuka"),
            CreativeTabClassifier.Kind.CONSUMABLE);

    private ModCreativeTabs() {
    }

    private static RegistryObject<CreativeModeTab> tab(String id, String titleKey, SupplierStack icon,
                                                       CreativeTabClassifier.Kind kind) {
        return tab(id, titleKey, icon, kind, false);
    }

    private static RegistryObject<CreativeModeTab> tab(String id, String titleKey, SupplierStack icon,
                                                       CreativeTabClassifier.Kind kind, boolean search) {
        return CREATIVE_TABS.register(id, () -> {
            CreativeModeTab.Builder builder = CreativeModeTab.builder()
                    .title(Component.translatable(titleKey))
                    .icon(icon::get)
                    .displayItems((params, output) -> fill(output, kind));
            if (search) {
                builder.withSearchBar();
            }
            return builder.build();
        });
    }

    private static void fill(CreativeModeTab.Output output, CreativeTabClassifier.Kind kind) {
        List<Item> items = new ArrayList<>();
        for (RegistryObject<Item> entry : ModItems.ITEMS.getEntries()) {
            Item item = entry.get();
            if (CreativeTabClassifier.classify(item) == kind) {
                items.add(item);
            }
        }
        if (kind == CreativeTabClassifier.Kind.MISSILE) {
            items.sort(Comparator
                    .comparingInt((Item item) -> {
                        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
                        return LegacyMissileTabOrder.rank(key != null ? key.getPath() : "");
                    })
                    .thenComparing(item -> {
                        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
                        return key != null ? key.getPath() : "";
                    }));
        } else {
            // Working first, then WIP, then unimplemented stubs — easier for alpha players
            items.sort(Comparator
                    .comparingInt((Item item) -> PortContentRegistry.status(item).sortKey)
                    .thenComparing(item -> {
                        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
                        return key != null ? key.getPath() : "";
                    }));
        }
        for (Item item : items) {
            if (item == ModItems.CRASHED_BOMB.get()) {
                for (DudType type : DudType.values()) {
                    output.accept(CrashedBombBlock.stackFor(type));
                }
            } else if (item == ModItems.VOLCANO_CORE.get()) {
                for (VolcanoMode mode : VolcanoMode.values()) {
                    output.accept(VolcanoBlock.stackFor(ModBlocks.VOLCANO_CORE.get(), mode));
                }
            } else if (item == ModItems.VOLCANO_RAD_CORE.get()) {
                for (VolcanoMode mode : VolcanoMode.values()) {
                    output.accept(VolcanoBlock.stackFor(ModBlocks.VOLCANO_RAD_CORE.get(), mode));
                }
            } else if (item == ModItems.BOMB_CALLER.get()) {
                for (BombCallerItem.StrikeType type : BombCallerItem.StrikeType.values()) {
                    output.accept(BombCallerItem.stack(type));
                }
            } else if (item instanceof ItemCustomMissile
                    || (item instanceof ItemCustomMissilePart part && part.isHiddenFromCreative())) {
                continue;
            } else {
                output.accept(item);
            }
        }
    }

    @FunctionalInterface
    private interface SupplierStack {
        ItemStack get();
    }

    private static ItemStack iconItem(String... paths) {
        for (String path : paths) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(RefStrings.MODID, path));
            if (item != null) {
                return item.getDefaultInstance();
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack iconBlock(String... paths) {
        return iconItem(paths);
    }

    public static void register(IEventBus modBus) {
        CREATIVE_TABS.register(modBus);
    }

    public static void registerItemIcons() {
        HbmNuclearTechMod.LOGGER.debug("Creative tabs ready (9 legacy tabs).");
    }
}
