package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.CustomLauncherBlockEntity;
import com.hbm.blockentity.machine.LaunchTableBlockEntity;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

public class LaunchTableMenu extends CompactLauncherMenu {
    public LaunchTableMenu(int id, Inventory inv, LaunchTableBlockEntity be) {
        super(ModMenus.LAUNCH_TABLE.get(), id, inv, be, createTableData(be));
    }

    public LaunchTableMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, LaunchTableBlockEntity.class,
                LaunchTableBlockEntity::new));
    }

    public LaunchTableBlockEntity table() {
        return (LaunchTableBlockEntity) be;
    }

    public PartSize getPadSize() {
        int ordinal = data.get(9);
        PartSize[] values = PartSize.values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return table().getPadSize();
    }

    private static ContainerData createTableData(CustomLauncherBlockEntity be) {
        ContainerData base = createData(be);
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (index < 9) {
                    return base.get(index);
                }
                if (index == 9 && be instanceof LaunchTableBlockEntity table) {
                    return table.getPadSize().ordinal();
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 10;
            }
        };
    }
}
