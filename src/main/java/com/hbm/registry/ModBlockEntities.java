package com.hbm.registry;

import com.hbm.blockentity.rbmk.RBMKPassiveBlockEntity;
import com.hbm.lib.RefStrings;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RefStrings.MODID);

    public static final RegistryObject<BlockEntityType<RBMKPassiveBlockEntity>> RBMK_PASSIVE =
            BLOCK_ENTITIES.register("rbmk_passive", () -> BlockEntityType.Builder.of(
                    RBMKPassiveBlockEntity::new,
                    ModBlocks.RBMK_BLANK.get(),
                    ModBlocks.RBMK_REFLECTOR.get(),
                    ModBlocks.RBMK_ABSORBER.get(),
                    ModBlocks.RBMK_MODERATOR.get()
            ).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
