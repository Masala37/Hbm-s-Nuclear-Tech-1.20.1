package com.hbm.registry;

import com.hbm.inventory.menu.BombMultiMenu;
import com.hbm.inventory.menu.CombustionGeneratorMenu;
import com.hbm.inventory.menu.DieselGeneratorMenu;
import com.hbm.inventory.menu.ElectricFurnaceMenu;
import com.hbm.inventory.menu.FluidBarrelMenu;
import com.hbm.inventory.menu.LaunchPadMenu;
import com.hbm.inventory.menu.MachineBatteryMenu;
import com.hbm.inventory.menu.MissileAssemblyMenu;
import com.hbm.inventory.menu.NukeBoyMenu;
import com.hbm.inventory.menu.NukeCustomMenu;
import com.hbm.inventory.menu.NukeFleijaMenu;
import com.hbm.inventory.menu.NukeFstbmbMenu;
import com.hbm.inventory.menu.NukeGadgetMenu;
import com.hbm.inventory.menu.NukeManMenu;
import com.hbm.inventory.menu.NukeMikeMenu;
import com.hbm.inventory.menu.NukeN2Menu;
import com.hbm.inventory.menu.NukePrototypeMenu;
import com.hbm.inventory.menu.NukeSoliniumMenu;
import com.hbm.inventory.menu.NukeTsarMenu;
import com.hbm.inventory.menu.StorageCrateMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, RefStrings.MODID);

    public static final RegistryObject<MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE =
            MENUS.register("electric_furnace", () -> IForgeMenuType.create(ElectricFurnaceMenu::new));

    public static final RegistryObject<MenuType<MachineBatteryMenu>> MACHINE_BATTERY =
            MENUS.register("machine_battery", () -> IForgeMenuType.create(MachineBatteryMenu::new));

    public static final RegistryObject<MenuType<FluidBarrelMenu>> FLUID_BARREL =
            MENUS.register("fluid_barrel", () -> IForgeMenuType.create(FluidBarrelMenu::new));

    public static final RegistryObject<MenuType<DieselGeneratorMenu>> DIESEL_GENERATOR =
            MENUS.register("diesel_generator", () -> IForgeMenuType.create(DieselGeneratorMenu::new));

    public static final RegistryObject<MenuType<CombustionGeneratorMenu>> COMBUSTION_GENERATOR =
            MENUS.register("combustion_generator", () -> IForgeMenuType.create(CombustionGeneratorMenu::new));

    public static final RegistryObject<MenuType<MissileAssemblyMenu>> MISSILE_ASSEMBLY =
            MENUS.register("machine_missile_assembly", () -> IForgeMenuType.create(MissileAssemblyMenu::new));

    public static final RegistryObject<MenuType<LaunchPadMenu>> LAUNCH_PAD =
            MENUS.register("launch_pad", () -> IForgeMenuType.create(LaunchPadMenu::new));

    public static final RegistryObject<MenuType<NukeBoyMenu>> NUKE_BOY =
            MENUS.register("nuke_boy", () -> IForgeMenuType.create(NukeBoyMenu::new));

    public static final RegistryObject<MenuType<NukeManMenu>> NUKE_MAN =
            MENUS.register("nuke_man", () -> IForgeMenuType.create(NukeManMenu::new));

    public static final RegistryObject<MenuType<NukeGadgetMenu>> NUKE_GADGET =
            MENUS.register("nuke_gadget", () -> IForgeMenuType.create(NukeGadgetMenu::new));

    public static final RegistryObject<MenuType<NukeMikeMenu>> NUKE_MIKE =
            MENUS.register("nuke_mike", () -> IForgeMenuType.create(NukeMikeMenu::new));

    public static final RegistryObject<MenuType<NukeTsarMenu>> NUKE_TSAR =
            MENUS.register("nuke_tsar", () -> IForgeMenuType.create(NukeTsarMenu::new));

    public static final RegistryObject<MenuType<NukeFleijaMenu>> NUKE_FLEIJA =
            MENUS.register("nuke_fleija", () -> IForgeMenuType.create(NukeFleijaMenu::new));

    public static final RegistryObject<MenuType<NukeSoliniumMenu>> NUKE_SOLINIUM =
            MENUS.register("nuke_solinium", () -> IForgeMenuType.create(NukeSoliniumMenu::new));

    public static final RegistryObject<MenuType<NukeFstbmbMenu>> NUKE_FSTBMB =
            MENUS.register("nuke_fstbmb", () -> IForgeMenuType.create(NukeFstbmbMenu::new));

    public static final RegistryObject<MenuType<NukeN2Menu>> NUKE_N2 =
            MENUS.register("nuke_n2", () -> IForgeMenuType.create(NukeN2Menu::new));

    public static final RegistryObject<MenuType<NukePrototypeMenu>> NUKE_PROTOTYPE =
            MENUS.register("nuke_prototype", () -> IForgeMenuType.create(NukePrototypeMenu::new));

    public static final RegistryObject<MenuType<NukeCustomMenu>> NUKE_CUSTOM =
            MENUS.register("nuke_custom", () -> IForgeMenuType.create(NukeCustomMenu::new));

    public static final RegistryObject<MenuType<BombMultiMenu>> BOMB_MULTI =
            MENUS.register("bomb_multi", () -> IForgeMenuType.create(BombMultiMenu::new));

    public static final RegistryObject<MenuType<StorageCrateMenu>> STORAGE_CRATE =
            MENUS.register("storage_crate", () -> IForgeMenuType.create(StorageCrateMenu::new));

    public static final RegistryObject<MenuType<StorageCrateMenu>> STORAGE_CRATE_LARGE =
            MENUS.register("storage_crate_large", () -> IForgeMenuType.create(StorageCrateMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
