package com.hbm.registry;

import com.hbm.blocks.HazardBlock;
import com.hbm.blocks.OutgasOreBlock;
import com.hbm.blocks.bomb.BombBlock;
import com.hbm.blocks.bomb.NukeBoyBlock;
import com.hbm.blocks.bomb.NukeGadgetBlock;
import com.hbm.blocks.bomb.NukeManBlock;
import com.hbm.blocks.bomb.NukeMikeBlock;
import com.hbm.blocks.generic.WasteEarthBlock;
import com.hbm.blocks.machine.CombustionGeneratorBlock;
import com.hbm.blocks.machine.DieselGeneratorBlock;
import com.hbm.blocks.machine.ElectricFurnaceBlock;
import com.hbm.blocks.machine.FluidBarrelBlock;
import com.hbm.blocks.machine.MachineBatteryBlock;
import com.hbm.blocks.machine.RedCableBlock;
import com.hbm.blocks.rbmk.RBMKDecoBlock;
import com.hbm.blocks.rbmk.RBMKPassiveBlock;
import com.hbm.lib.RefStrings;
import com.hbm.rbmk.RBMKColumnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, RefStrings.MODID);

    public static final RegistryObject<Block> ORE_URANIUM = registerOre("ore_uranium");
    public static final RegistryObject<Block> ORE_TITANIUM = registerOre("ore_titanium");
    public static final RegistryObject<Block> ORE_TUNGSTEN = registerOre("ore_tungsten");
    public static final RegistryObject<Block> ORE_ALUMINIUM = registerOre("ore_aluminium");
    public static final RegistryObject<Block> ORE_LEAD = registerOre("ore_lead");
    public static final RegistryObject<Block> ORE_BERYLLIUM = registerOre("ore_beryllium");
    public static final RegistryObject<Block> ORE_COPPER = registerOre("ore_copper");
    public static final RegistryObject<Block> ORE_THORIUM = registerOre("ore_thorium");

    public static final RegistryObject<Block> BLOCK_URANIUM = registerMetalBlock("block_uranium", true);
    public static final RegistryObject<Block> BLOCK_TITANIUM = registerMetalBlock("block_titanium", false);
    public static final RegistryObject<Block> BLOCK_TUNGSTEN = registerMetalBlock("block_tungsten", false);
    public static final RegistryObject<Block> BLOCK_ALUMINIUM = registerMetalBlock("block_aluminium", false);
    public static final RegistryObject<Block> BLOCK_LEAD = registerMetalBlock("block_lead", false);
    public static final RegistryObject<Block> BLOCK_BERYLLIUM = registerMetalBlock("block_beryllium", false);
    public static final RegistryObject<Block> BLOCK_STEEL = registerMetalBlock("block_steel", false);
    public static final RegistryObject<Block> BLOCK_COPPER = registerMetalBlock("block_copper", false);
    public static final RegistryObject<Block> BLOCK_RED_COPPER = registerMetalBlock("block_red_copper", false);
    public static final RegistryObject<Block> BLOCK_THORIUM = registerMetalBlock("block_thorium", false);

    public static final RegistryObject<Block> DECO_RBMK = BLOCKS.register("deco_rbmk", RBMKDecoBlock::new);
    public static final RegistryObject<Block> DECO_RBMK_SMOOTH = BLOCKS.register("deco_rbmk_smooth", RBMKDecoBlock::new);

    public static final RegistryObject<Block> FLUID_BARREL = BLOCKS.register("fluid_barrel", FluidBarrelBlock::new);
    public static final RegistryObject<Block> MACHINE_BATTERY = BLOCKS.register("machine_battery", MachineBatteryBlock::new);
    public static final RegistryObject<Block> COMBUSTION_GENERATOR = BLOCKS.register("combustion_generator", CombustionGeneratorBlock::new);
    public static final RegistryObject<Block> DIESEL_GENERATOR = BLOCKS.register("diesel_generator", DieselGeneratorBlock::new);
    public static final RegistryObject<Block> ELECTRIC_FURNACE = BLOCKS.register("electric_furnace", ElectricFurnaceBlock::new);
    public static final RegistryObject<Block> RED_CABLE = BLOCKS.register("red_cable", RedCableBlock::new);

    public static final RegistryObject<Block> DYNAMITE = BLOCKS.register("dynamite", () -> new BombBlock(8.0F));
    public static final RegistryObject<Block> TNT = BLOCKS.register("tnt", () -> new BombBlock(10.0F));
    public static final RegistryObject<Block> SEMTEX = BLOCKS.register("semtex", () -> new BombBlock(12.0F));
    public static final RegistryObject<Block> C4 = BLOCKS.register("c4", () -> new BombBlock(15.0F, true));
    public static final RegistryObject<Block> NUKE_BOY = BLOCKS.register("nuke_boy", NukeBoyBlock::new);
    public static final RegistryObject<Block> NUKE_MAN = BLOCKS.register("nuke_man", NukeManBlock::new);
    public static final RegistryObject<Block> NUKE_GADGET = BLOCKS.register("nuke_gadget", NukeGadgetBlock::new);
    public static final RegistryObject<Block> NUKE_MIKE = BLOCKS.register("nuke_mike", NukeMikeBlock::new);
    public static final RegistryObject<Block> WASTE_EARTH = BLOCKS.register("waste_earth", WasteEarthBlock::new);

    public static final RegistryObject<Block> RBMK_BLANK = BLOCKS.register("rbmk_blank",
            () -> new RBMKPassiveBlock(RBMKColumnType.BLANK));
    public static final RegistryObject<Block> RBMK_REFLECTOR = BLOCKS.register("rbmk_reflector",
            () -> new RBMKPassiveBlock(RBMKColumnType.REFLECTOR));
    public static final RegistryObject<Block> RBMK_ABSORBER = BLOCKS.register("rbmk_absorber",
            () -> new RBMKPassiveBlock(RBMKColumnType.ABSORBER));
    public static final RegistryObject<Block> RBMK_MODERATOR = BLOCKS.register("rbmk_moderator",
            () -> new RBMKPassiveBlock(RBMKColumnType.MODERATOR));

    private ModBlocks() {
    }

    private static RegistryObject<Block> registerOre(String name) {
        return BLOCKS.register(name, () -> new OutgasOreBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> registerMetalBlock(String name, boolean beaconBase) {
        Supplier<Block> factory = () -> new HazardBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 50.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL), beaconBase);
        return BLOCKS.register(name, factory);
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
