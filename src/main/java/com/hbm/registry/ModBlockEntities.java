package com.hbm.registry;

import com.hbm.blockentity.bomb.NukeBoyBlockEntity;
import com.hbm.blockentity.bomb.NukeManBlockEntity;
import com.hbm.blockentity.machine.CombustionGeneratorBlockEntity;
import com.hbm.blockentity.machine.ElectricFurnaceBlockEntity;
import com.hbm.blockentity.machine.FluidBarrelBlockEntity;
import com.hbm.blockentity.machine.MachineBatteryBlockEntity;
import com.hbm.blockentity.machine.RedCableBlockEntity;
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

    public static final RegistryObject<BlockEntityType<FluidBarrelBlockEntity>> FLUID_BARREL =
            BLOCK_ENTITIES.register("fluid_barrel", () -> BlockEntityType.Builder.of(
                    FluidBarrelBlockEntity::new,
                    ModBlocks.FLUID_BARREL.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MachineBatteryBlockEntity>> MACHINE_BATTERY =
            BLOCK_ENTITIES.register("machine_battery", () -> BlockEntityType.Builder.of(
                    MachineBatteryBlockEntity::new,
                    ModBlocks.MACHINE_BATTERY.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CombustionGeneratorBlockEntity>> COMBUSTION_GENERATOR =
            BLOCK_ENTITIES.register("combustion_generator", () -> BlockEntityType.Builder.of(
                    CombustionGeneratorBlockEntity::new,
                    ModBlocks.COMBUSTION_GENERATOR.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<ElectricFurnaceBlockEntity>> ELECTRIC_FURNACE =
            BLOCK_ENTITIES.register("electric_furnace", () -> BlockEntityType.Builder.of(
                    ElectricFurnaceBlockEntity::new,
                    ModBlocks.ELECTRIC_FURNACE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<RedCableBlockEntity>> RED_CABLE =
            BLOCK_ENTITIES.register("red_cable", () -> BlockEntityType.Builder.of(
                    RedCableBlockEntity::new,
                    ModBlocks.RED_CABLE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeBoyBlockEntity>> NUKE_BOY =
            BLOCK_ENTITIES.register("nuke_boy", () -> BlockEntityType.Builder.of(
                    NukeBoyBlockEntity::new,
                    ModBlocks.NUKE_BOY.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeManBlockEntity>> NUKE_MAN =
            BLOCK_ENTITIES.register("nuke_man", () -> BlockEntityType.Builder.of(
                    NukeManBlockEntity::new,
                    ModBlocks.NUKE_MAN.get()
            ).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
