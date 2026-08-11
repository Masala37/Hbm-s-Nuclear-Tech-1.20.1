package com.hbm.blockentity.machine;

import com.hbm.entity.missile.MissileAssemblyRecipes;
import com.hbm.inventory.menu.MissileAssemblyMenu;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Size/preset missile assembly — chip + warhead + fuselage + fins + thruster → preset missile.
 * <p>
 * Slot layout / matching adapted from HBM-Modernized (GPL-3.0).
 */
public class MissileAssemblyBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_CHIP = 0;
    public static final int SLOT_WARHEAD = 1;
    public static final int SLOT_FUSELAGE = 2;
    public static final int SLOT_FINS = 3;
    public static final int SLOT_THRUSTER = 4;
    public static final int SLOT_OUTPUT = 5;

    private final ItemStackHandler items = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_CHIP -> MissileAssemblyRecipes.isChip(stack);
                case SLOT_WARHEAD -> MissileAssemblyRecipes.isWarhead(stack);
                case SLOT_FUSELAGE -> MissileAssemblyRecipes.isFuselage(stack);
                case SLOT_FINS -> MissileAssemblyRecipes.isFins(stack);
                case SLOT_THRUSTER -> MissileAssemblyRecipes.isThruster(stack);
                case SLOT_OUTPUT -> false;
                default -> false;
            };
        }
    };

    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> items);

    public MissileAssemblyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MISSILE_ASSEMBLY.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean canAssemble() {
        if (!items.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            return false;
        }
        ItemStack result = MissileAssemblyRecipes.resolve(
                items.getStackInSlot(SLOT_CHIP),
                items.getStackInSlot(SLOT_WARHEAD),
                items.getStackInSlot(SLOT_FUSELAGE),
                items.getStackInSlot(SLOT_FINS),
                items.getStackInSlot(SLOT_THRUSTER));
        return !result.isEmpty();
    }

    public boolean tryAssemble() {
        if (!canAssemble()) {
            return false;
        }
        ItemStack result = MissileAssemblyRecipes.resolve(
                items.getStackInSlot(SLOT_CHIP),
                items.getStackInSlot(SLOT_WARHEAD),
                items.getStackInSlot(SLOT_FUSELAGE),
                items.getStackInSlot(SLOT_FINS),
                items.getStackInSlot(SLOT_THRUSTER));
        if (result.isEmpty()) {
            return false;
        }
        items.extractItem(SLOT_CHIP, 1, false);
        items.extractItem(SLOT_WARHEAD, 1, false);
        items.extractItem(SLOT_FUSELAGE, 1, false);
        if (!items.getStackInSlot(SLOT_FINS).isEmpty()) {
            items.extractItem(SLOT_FINS, 1, false);
        }
        items.extractItem(SLOT_THRUSTER, 1, false);
        items.setStackInSlot(SLOT_OUTPUT, result);
        setChanged();
        return true;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.machine_missile_assembly");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new MissileAssemblyMenu(id, inv, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemOptional.invalidate();
    }
}
