package com.hbm.registry;

import com.hbm.blocks.HazardBlock;
import com.hbm.blocks.OutgasOreBlock;
import com.hbm.blocks.rbmk.RBMKDecoBlock;
import com.hbm.blocks.rbmk.RBMKPassiveBlock;
import com.hbm.lib.RefStrings;
import com.hbm.rbmk.RBMKColumnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, RefStrings.MODID);

    public static final RegistryObject<Block> ORE_URANIUM = BLOCKS.register("ore_uranium",
            () -> new OutgasOreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(5.0F, 10.0F)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> BLOCK_URANIUM = BLOCKS.register("block_uranium",
            () -> new HazardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 50.0F)
                    .requiresCorrectToolForDrops(), true));

    public static final RegistryObject<Block> DECO_RBMK = BLOCKS.register("deco_rbmk",
            RBMKDecoBlock::new);

    public static final RegistryObject<Block> DECO_RBMK_SMOOTH = BLOCKS.register("deco_rbmk_smooth",
            RBMKDecoBlock::new);

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

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
