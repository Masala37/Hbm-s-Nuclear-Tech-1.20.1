package com.hbm.registry;

import com.hbm.lib.RefStrings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RefStrings.MODID);

    public static final RegistryObject<Item> URANIUM_INGOT = registerIngot("ingot_uranium");
    public static final RegistryObject<Item> TITANIUM_INGOT = registerIngot("ingot_titanium");
    public static final RegistryObject<Item> TUNGSTEN_INGOT = registerIngot("ingot_tungsten");
    public static final RegistryObject<Item> ALUMINIUM_INGOT = registerIngot("ingot_aluminium");
    public static final RegistryObject<Item> LEAD_INGOT = registerIngot("ingot_lead");
    public static final RegistryObject<Item> BERYLLIUM_INGOT = registerIngot("ingot_beryllium");
    public static final RegistryObject<Item> STEEL_INGOT = registerIngot("ingot_steel");
    public static final RegistryObject<Item> COPPER_INGOT = registerIngot("ingot_copper");
    public static final RegistryObject<Item> RED_COPPER_INGOT = registerIngot("ingot_red_copper");
    public static final RegistryObject<Item> THORIUM_INGOT = registerIngot("ingot_thorium");

    public static final RegistryObject<Item> ORE_URANIUM = registerBlockItem(ModBlocks.ORE_URANIUM);
    public static final RegistryObject<Item> ORE_TITANIUM = registerBlockItem(ModBlocks.ORE_TITANIUM);
    public static final RegistryObject<Item> ORE_TUNGSTEN = registerBlockItem(ModBlocks.ORE_TUNGSTEN);
    public static final RegistryObject<Item> ORE_ALUMINIUM = registerBlockItem(ModBlocks.ORE_ALUMINIUM);
    public static final RegistryObject<Item> ORE_LEAD = registerBlockItem(ModBlocks.ORE_LEAD);
    public static final RegistryObject<Item> ORE_BERYLLIUM = registerBlockItem(ModBlocks.ORE_BERYLLIUM);
    public static final RegistryObject<Item> ORE_COPPER = registerBlockItem(ModBlocks.ORE_COPPER);
    public static final RegistryObject<Item> ORE_THORIUM = registerBlockItem(ModBlocks.ORE_THORIUM);

    public static final RegistryObject<Item> BLOCK_URANIUM = registerBlockItem(ModBlocks.BLOCK_URANIUM);
    public static final RegistryObject<Item> BLOCK_TITANIUM = registerBlockItem(ModBlocks.BLOCK_TITANIUM);
    public static final RegistryObject<Item> BLOCK_TUNGSTEN = registerBlockItem(ModBlocks.BLOCK_TUNGSTEN);
    public static final RegistryObject<Item> BLOCK_ALUMINIUM = registerBlockItem(ModBlocks.BLOCK_ALUMINIUM);
    public static final RegistryObject<Item> BLOCK_LEAD = registerBlockItem(ModBlocks.BLOCK_LEAD);
    public static final RegistryObject<Item> BLOCK_BERYLLIUM = registerBlockItem(ModBlocks.BLOCK_BERYLLIUM);
    public static final RegistryObject<Item> BLOCK_STEEL = registerBlockItem(ModBlocks.BLOCK_STEEL);
    public static final RegistryObject<Item> BLOCK_COPPER = registerBlockItem(ModBlocks.BLOCK_COPPER);
    public static final RegistryObject<Item> BLOCK_RED_COPPER = registerBlockItem(ModBlocks.BLOCK_RED_COPPER);
    public static final RegistryObject<Item> BLOCK_THORIUM = registerBlockItem(ModBlocks.BLOCK_THORIUM);

    public static final RegistryObject<Item> DECO_RBMK = registerBlockItem(ModBlocks.DECO_RBMK);
    public static final RegistryObject<Item> DECO_RBMK_SMOOTH = registerBlockItem(ModBlocks.DECO_RBMK_SMOOTH);
    public static final RegistryObject<Item> RBMK_BLANK = registerBlockItem(ModBlocks.RBMK_BLANK);
    public static final RegistryObject<Item> RBMK_REFLECTOR = registerBlockItem(ModBlocks.RBMK_REFLECTOR);
    public static final RegistryObject<Item> RBMK_ABSORBER = registerBlockItem(ModBlocks.RBMK_ABSORBER);
    public static final RegistryObject<Item> RBMK_MODERATOR = registerBlockItem(ModBlocks.RBMK_MODERATOR);

    private ModItems() {
    }

    private static RegistryObject<Item> registerIngot(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> registerBlockItem(RegistryObject<Block> block) {
        return ITEMS.register(block.getId().getPath(),
                () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
