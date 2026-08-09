package com.hbm.registry;

import com.hbm.lib.RefStrings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RefStrings.MODID);

    public static final RegistryObject<Item> URANIUM_INGOT = ITEMS.register("ingot_uranium",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ORE_URANIUM = registerBlockItem(ModBlocks.ORE_URANIUM);
    public static final RegistryObject<Item> BLOCK_URANIUM = registerBlockItem(ModBlocks.BLOCK_URANIUM);

    public static final RegistryObject<Item> DECO_RBMK = registerBlockItem(ModBlocks.DECO_RBMK);
    public static final RegistryObject<Item> DECO_RBMK_SMOOTH = registerBlockItem(ModBlocks.DECO_RBMK_SMOOTH);
    public static final RegistryObject<Item> RBMK_BLANK = registerBlockItem(ModBlocks.RBMK_BLANK);
    public static final RegistryObject<Item> RBMK_REFLECTOR = registerBlockItem(ModBlocks.RBMK_REFLECTOR);
    public static final RegistryObject<Item> RBMK_ABSORBER = registerBlockItem(ModBlocks.RBMK_ABSORBER);
    public static final RegistryObject<Item> RBMK_MODERATOR = registerBlockItem(ModBlocks.RBMK_MODERATOR);

    private ModItems() {
    }

    private static RegistryObject<Item> registerBlockItem(RegistryObject<net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(block.getId().getPath(),
                () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
