package com.hbm.registry;

import com.hbm.HbmNuclearTechMod;
import com.hbm.lib.RefStrings;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RefStrings.MODID);

    public static final RegistryObject<CreativeModeTab> PARTS_TAB = CREATIVE_TABS.register("parts",
            () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.hbm.parts"))
                    .icon(() -> ModItems.URANIUM_INGOT.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(ModItems.URANIUM_INGOT.get());
                        output.accept(ModBlocks.ORE_URANIUM.get());
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> BLOCK_TAB = CREATIVE_TABS.register("blocks",
            () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.hbm.blocks"))
                    .icon(() -> ModBlocks.ORE_URANIUM.get().asItem().getDefaultInstance())
                    .displayItems((params, output) -> output.accept(ModBlocks.ORE_URANIUM.get()))
                    .build());

    public static final RegistryObject<CreativeModeTab> MACHINE_TAB = CREATIVE_TABS.register("machines",
            () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.hbm.machines"))
                    .icon(() -> ModItems.RBMK_BLANK.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(ModItems.DECO_RBMK.get());
                        output.accept(ModItems.DECO_RBMK_SMOOTH.get());
                        output.accept(ModItems.RBMK_BLANK.get());
                        output.accept(ModItems.RBMK_REFLECTOR.get());
                        output.accept(ModItems.RBMK_ABSORBER.get());
                        output.accept(ModItems.RBMK_MODERATOR.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        CREATIVE_TABS.register(modBus);
    }

    public static void registerItemIcons() {
        HbmNuclearTechMod.LOGGER.debug("Client creative tab icons registered.");
    }
}
