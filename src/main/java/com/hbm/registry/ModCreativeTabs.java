package com.hbm.registry;

import com.hbm.HbmNuclearTechMod;
import com.hbm.lib.RefStrings;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RefStrings.MODID);

    public static final RegistryObject<CreativeModeTab> PARTS_TAB = CREATIVE_TABS.register("parts",
            () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.hbm.parts"))
                    .icon(() -> ModItems.STEEL_INGOT.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        accept(output,
                                ModItems.URANIUM_INGOT,
                                ModItems.THORIUM_INGOT,
                                ModItems.TITANIUM_INGOT,
                                ModItems.TUNGSTEN_INGOT,
                                ModItems.ALUMINIUM_INGOT,
                                ModItems.COPPER_INGOT,
                                ModItems.RED_COPPER_INGOT,
                                ModItems.LEAD_INGOT,
                                ModItems.BERYLLIUM_INGOT,
                                ModItems.STEEL_INGOT);
                        for (ModFluids.FluidEntry fluid : ModFluids.entries()) {
                            output.accept(fluid.bucket.get());
                        }
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> BLOCK_TAB = CREATIVE_TABS.register("blocks",
            () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.hbm.blocks"))
                    .icon(() -> ModBlocks.ORE_TITANIUM.get().asItem().getDefaultInstance())
                    .displayItems((params, output) -> {
                        accept(output,
                                ModBlocks.ORE_URANIUM,
                                ModBlocks.ORE_THORIUM,
                                ModBlocks.ORE_TITANIUM,
                                ModBlocks.ORE_TUNGSTEN,
                                ModBlocks.ORE_ALUMINIUM,
                                ModBlocks.ORE_COPPER,
                                ModBlocks.ORE_LEAD,
                                ModBlocks.ORE_BERYLLIUM,
                                ModBlocks.BLOCK_URANIUM,
                                ModBlocks.BLOCK_THORIUM,
                                ModBlocks.BLOCK_TITANIUM,
                                ModBlocks.BLOCK_TUNGSTEN,
                                ModBlocks.BLOCK_ALUMINIUM,
                                ModBlocks.BLOCK_COPPER,
                                ModBlocks.BLOCK_RED_COPPER,
                                ModBlocks.BLOCK_LEAD,
                                ModBlocks.BLOCK_BERYLLIUM,
                                ModBlocks.BLOCK_STEEL);
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> MACHINE_TAB = CREATIVE_TABS.register("machines",
            () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.hbm.machines"))
                    .icon(() -> ModItems.RBMK_BLANK.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        accept(output,
                                ModItems.COMBUSTION_GENERATOR,
                                ModItems.MACHINE_BATTERY,
                                ModItems.ELECTRIC_FURNACE,
                                ModItems.RED_CABLE,
                                ModItems.FLUID_BARREL,
                                ModItems.DECO_RBMK,
                                ModItems.DECO_RBMK_SMOOTH,
                                ModItems.RBMK_BLANK,
                                ModItems.RBMK_REFLECTOR,
                                ModItems.RBMK_ABSORBER,
                                ModItems.RBMK_MODERATOR);
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> BOMB_TAB = CREATIVE_TABS.register("bombs",
            () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.hbm.bombs"))
                    .icon(() -> ModItems.NUKE_BOY.get().getDefaultInstance())
                    .displayItems((params, output) -> accept(output,
                            ModItems.DYNAMITE,
                            ModItems.TNT,
                            ModItems.SEMTEX,
                            ModItems.C4,
                            ModItems.NUKE_BOY,
                            ModItems.BOY_SHIELDING,
                            ModItems.BOY_TARGET,
                            ModItems.BOY_BULLET,
                            ModItems.BOY_PROPELLANT,
                            ModItems.BOY_IGNITER))
                    .build());

    private ModCreativeTabs() {
    }

    @SafeVarargs
    private static void accept(CreativeModeTab.Output output, RegistryObject<? extends ItemLike>... items) {
        for (RegistryObject<? extends ItemLike> item : items) {
            output.accept(item.get());
        }
    }

    public static void register(IEventBus modBus) {
        CREATIVE_TABS.register(modBus);
    }

    public static void registerItemIcons() {
        HbmNuclearTechMod.LOGGER.debug("Client creative tab icons registered.");
    }
}
