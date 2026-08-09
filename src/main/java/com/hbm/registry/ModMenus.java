package com.hbm.registry;

import com.hbm.inventory.menu.DieselGeneratorMenu;
import com.hbm.inventory.menu.ElectricFurnaceMenu;
import com.hbm.inventory.menu.FluidBarrelMenu;
import com.hbm.inventory.menu.MachineBatteryMenu;
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

    private ModMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
