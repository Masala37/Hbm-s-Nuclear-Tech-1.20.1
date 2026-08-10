package com.hbm.registry;

import com.hbm.blockentity.bomb.BombMultiBlockEntity;
import com.hbm.blockentity.bomb.ChargeBlockEntity;
import com.hbm.blockentity.bomb.CrashedBombBlockEntity;
import com.hbm.blockentity.bomb.VolcanoCoreBlockEntity;
import com.hbm.blockentity.bomb.FireworksBlockEntity;
import com.hbm.blockentity.bomb.LandmineBlockEntity;
import com.hbm.blockentity.bomb.NukeBoyBlockEntity;
import com.hbm.blockentity.bomb.NukeCustomBlockEntity;
import com.hbm.blockentity.bomb.NukeFleijaBlockEntity;
import com.hbm.blockentity.bomb.NukeFstbmbBlockEntity;
import com.hbm.blockentity.bomb.NukeGadgetBlockEntity;
import com.hbm.blockentity.bomb.NukeManBlockEntity;
import com.hbm.blockentity.bomb.NukeMikeBlockEntity;
import com.hbm.blockentity.bomb.NukeN2BlockEntity;
import com.hbm.blockentity.bomb.NukePrototypeBlockEntity;
import com.hbm.blockentity.bomb.NukeSoliniumBlockEntity;
import com.hbm.blockentity.bomb.NukeTsarBlockEntity;
import com.hbm.blockentity.machine.CableDiodeBlockEntity;
import com.hbm.blockentity.machine.CableSwitchBlockEntity;
import com.hbm.blockentity.machine.CombustionGeneratorBlockEntity;
import com.hbm.blockentity.machine.DieselGeneratorBlockEntity;
import com.hbm.blockentity.machine.ElectricFurnaceBlockEntity;
import com.hbm.blockentity.machine.FluidBarrelBlockEntity;
import com.hbm.blockentity.machine.LaunchPadBlockEntity;
import com.hbm.blockentity.machine.MachineBatteryBlockEntity;
import com.hbm.blockentity.machine.RedCableBlockEntity;
import com.hbm.blockentity.machine.StorageCrateBlockEntity;
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

    public static final RegistryObject<BlockEntityType<DieselGeneratorBlockEntity>> DIESEL_GENERATOR =
            BLOCK_ENTITIES.register("diesel_generator", () -> BlockEntityType.Builder.of(
                    DieselGeneratorBlockEntity::new,
                    ModBlocks.DIESEL_GENERATOR.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<ElectricFurnaceBlockEntity>> ELECTRIC_FURNACE =
            BLOCK_ENTITIES.register("electric_furnace", () -> BlockEntityType.Builder.of(
                    ElectricFurnaceBlockEntity::new,
                    ModBlocks.ELECTRIC_FURNACE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<RedCableBlockEntity>> RED_CABLE =
            BLOCK_ENTITIES.register("red_cable", () -> BlockEntityType.Builder.of(
                    RedCableBlockEntity::new,
                    ModBlocks.RED_CABLE.get(),
                    ModBlocks.RED_CABLE_CLASSIC.get(),
                    ModBlocks.RED_WIRE_COATED.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CableSwitchBlockEntity>> CABLE_SWITCH =
            BLOCK_ENTITIES.register("cable_switch", () -> BlockEntityType.Builder.of(
                    CableSwitchBlockEntity::new,
                    ModBlocks.CABLE_SWITCH.get(),
                    ModBlocks.CABLE_DETECTOR.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CableDiodeBlockEntity>> CABLE_DIODE =
            BLOCK_ENTITIES.register("cable_diode", () -> BlockEntityType.Builder.of(
                    CableDiodeBlockEntity::new,
                    ModBlocks.CABLE_DIODE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<StorageCrateBlockEntity>> STORAGE_CRATE =
            BLOCK_ENTITIES.register("storage_crate", () -> BlockEntityType.Builder.of(
                    StorageCrateBlockEntity::new,
                    ModBlocks.CRATE_IRON.get(),
                    ModBlocks.CRATE_STEEL.get()
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

    public static final RegistryObject<BlockEntityType<NukeGadgetBlockEntity>> NUKE_GADGET =
            BLOCK_ENTITIES.register("nuke_gadget", () -> BlockEntityType.Builder.of(
                    NukeGadgetBlockEntity::new,
                    ModBlocks.NUKE_GADGET.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeMikeBlockEntity>> NUKE_MIKE =
            BLOCK_ENTITIES.register("nuke_mike", () -> BlockEntityType.Builder.of(
                    NukeMikeBlockEntity::new,
                    ModBlocks.NUKE_MIKE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeTsarBlockEntity>> NUKE_TSAR =
            BLOCK_ENTITIES.register("nuke_tsar", () -> BlockEntityType.Builder.of(
                    NukeTsarBlockEntity::new,
                    ModBlocks.NUKE_TSAR.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeFleijaBlockEntity>> NUKE_FLEIJA =
            BLOCK_ENTITIES.register("nuke_fleija", () -> BlockEntityType.Builder.of(
                    NukeFleijaBlockEntity::new,
                    ModBlocks.NUKE_FLEIJA.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeSoliniumBlockEntity>> NUKE_SOLINIUM =
            BLOCK_ENTITIES.register("nuke_solinium", () -> BlockEntityType.Builder.of(
                    NukeSoliniumBlockEntity::new,
                    ModBlocks.NUKE_SOLINIUM.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeFstbmbBlockEntity>> NUKE_FSTBMB =
            BLOCK_ENTITIES.register("nuke_fstbmb", () -> BlockEntityType.Builder.of(
                    NukeFstbmbBlockEntity::new,
                    ModBlocks.NUKE_FSTBMB.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeN2BlockEntity>> NUKE_N2 =
            BLOCK_ENTITIES.register("nuke_n2", () -> BlockEntityType.Builder.of(
                    NukeN2BlockEntity::new,
                    ModBlocks.NUKE_N2.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukePrototypeBlockEntity>> NUKE_PROTOTYPE =
            BLOCK_ENTITIES.register("nuke_prototype", () -> BlockEntityType.Builder.of(
                    NukePrototypeBlockEntity::new,
                    ModBlocks.NUKE_PROTOTYPE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeCustomBlockEntity>> NUKE_CUSTOM =
            BLOCK_ENTITIES.register("nuke_custom", () -> BlockEntityType.Builder.of(
                    NukeCustomBlockEntity::new,
                    ModBlocks.NUKE_CUSTOM.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<BombMultiBlockEntity>> BOMB_MULTI =
            BLOCK_ENTITIES.register("bomb_multi", () -> BlockEntityType.Builder.of(
                    BombMultiBlockEntity::new,
                    ModBlocks.BOMB_MULTI.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LaunchPadBlockEntity>> LAUNCH_PAD =
            BLOCK_ENTITIES.register("launch_pad", () -> BlockEntityType.Builder.of(
                    LaunchPadBlockEntity::new,
                    ModBlocks.LAUNCH_PAD.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CrashedBombBlockEntity>> CRASHED_BOMB =
            BLOCK_ENTITIES.register("crashed_bomb", () -> BlockEntityType.Builder.of(
                    CrashedBombBlockEntity::new,
                    ModBlocks.CRASHED_BOMB.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LandmineBlockEntity>> LANDMINE =
            BLOCK_ENTITIES.register("landmine", () -> BlockEntityType.Builder.of(
                    LandmineBlockEntity::new,
                    ModBlocks.MINE_AP.get(),
                    ModBlocks.MINE_HE.get(),
                    ModBlocks.MINE_SHRAP.get(),
                    ModBlocks.MINE_FAT.get(),
                    ModBlocks.MINE_NAVAL.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<ChargeBlockEntity>> CHARGE =
            BLOCK_ENTITIES.register("charge", () -> BlockEntityType.Builder.of(
                    ChargeBlockEntity::new,
                    ModBlocks.CHARGE_DYNAMITE.get(),
                    ModBlocks.CHARGE_C4.get(),
                    ModBlocks.CHARGE_SEMTEX.get(),
                    ModBlocks.CHARGE_MINER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<FireworksBlockEntity>> FIREWORKS =
            BLOCK_ENTITIES.register("fireworks", () -> BlockEntityType.Builder.of(
                    FireworksBlockEntity::new,
                    ModBlocks.FIREWORKS.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<VolcanoCoreBlockEntity>> VOLCANO_CORE =
            BLOCK_ENTITIES.register("volcano_core", () -> BlockEntityType.Builder.of(
                    VolcanoCoreBlockEntity::new,
                    ModBlocks.VOLCANO_CORE.get(),
                    ModBlocks.VOLCANO_RAD_CORE.get()
            ).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
