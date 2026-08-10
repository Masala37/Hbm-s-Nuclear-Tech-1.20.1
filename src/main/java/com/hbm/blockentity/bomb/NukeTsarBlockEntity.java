package com.hbm.blockentity.bomb;

import com.hbm.config.BombConfig;
import com.hbm.inventory.menu.NukeTsarMenu;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
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
 * Tsar Bomba: primary (4 lenses + man core) or full secondary (+ tsar core).
 */
public class NukeTsarBlockEntity extends BlockEntity implements AssembledNuke, MenuProvider {
    public static final int SLOT_LENS_1 = 0;
    public static final int SLOT_LENS_2 = 1;
    public static final int SLOT_LENS_3 = 2;
    public static final int SLOT_LENS_4 = 3;
    public static final int SLOT_MAN_CORE = 4;
    public static final int SLOT_TSAR_CORE = 5;
    public static final int SLOT_COUNT = 6;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(requiredItem(slot));
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> items);

    public NukeTsarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUKE_TSAR.get(), pos, state);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.nuke_tsar");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new NukeTsarMenu(id, inv, this);
    }

    @Override
    public ItemStackHandler getItems() {
        return items;
    }

    public static Item requiredItem(int slot) {
        return switch (slot) {
            case SLOT_LENS_1, SLOT_LENS_2, SLOT_LENS_3, SLOT_LENS_4 -> ModItems.EXPLOSIVE_LENSES.get();
            case SLOT_MAN_CORE -> ModItems.MAN_CORE.get();
            case SLOT_TSAR_CORE -> ModItems.TSAR_CORE.get();
            default -> throw new IllegalArgumentException("Invalid Tsar slot: " + slot);
        };
    }

    public boolean isPrimaryReady() {
        for (int i = SLOT_LENS_1; i <= SLOT_MAN_CORE; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty() || !stack.is(requiredItem(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean isFilled() {
        if (!isPrimaryReady()) {
            return false;
        }
        ItemStack core = items.getStackInSlot(SLOT_TSAR_CORE);
        return !core.isEmpty() && core.is(ModItems.TSAR_CORE.get());
    }

    @Override
    public boolean isReady() {
        return isPrimaryReady();
    }

    @Override
    public int resolveBlastRadius(int configuredRadius) {
        return isFilled() ? BombConfig.tsarRadius.get() : BombConfig.manRadius.get();
    }

    @Override
    public int findInsertSlot(Item item) {
        if (item == ModItems.EXPLOSIVE_LENSES.get()) {
            for (int slot = SLOT_LENS_1; slot <= SLOT_LENS_4; slot++) {
                if (items.getStackInSlot(slot).isEmpty()) {
                    return slot;
                }
            }
            return -1;
        }
        if (item == ModItems.MAN_CORE.get() && items.getStackInSlot(SLOT_MAN_CORE).isEmpty()) {
            return SLOT_MAN_CORE;
        }
        if (item == ModItems.TSAR_CORE.get() && items.getStackInSlot(SLOT_TSAR_CORE).isEmpty()) {
            return SLOT_TSAR_CORE;
        }
        return -1;
    }

    @Override
    public void clearSlots() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public Component statusMessage() {
        if (isFilled()) {
            return Component.translatable("block.hbm.nuke_tsar.ready_full");
        }
        if (isPrimaryReady()) {
            return Component.translatable("block.hbm.nuke_tsar.ready_primary");
        }
        StringBuilder missing = new StringBuilder();
        int missingLenses = 0;
        for (int i = SLOT_LENS_1; i <= SLOT_LENS_4; i++) {
            if (items.getStackInSlot(i).isEmpty()) {
                missingLenses++;
            }
        }
        if (missingLenses > 0) {
            missing.append(missingLenses).append("x ")
                    .append(Component.translatable(ModItems.EXPLOSIVE_LENSES.get().getDescriptionId()).getString());
        }
        if (items.getStackInSlot(SLOT_MAN_CORE).isEmpty()) {
            append(missing, ModItems.MAN_CORE.get());
        }
        if (items.getStackInSlot(SLOT_TSAR_CORE).isEmpty()) {
            append(missing, ModItems.TSAR_CORE.get());
        }
        return Component.translatable("block.hbm.nuke_tsar.missing", missing.toString());
    }

    private static void append(StringBuilder missing, Item item) {
        if (!missing.isEmpty()) {
            missing.append(", ");
        }
        missing.append(Component.translatable(item.getDescriptionId()).getString());
    }

    @Override
    public void dropContents() {
        if (level == null) {
            return;
        }
        Containers.dropContents(level, worldPosition, new net.minecraft.world.SimpleContainer(copyStacks()));
        clearSlots();
    }

    @Override
    public ItemStack[] copyStacks() {
        ItemStack[] stacks = new ItemStack[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            stacks[i] = items.getStackInSlot(i).copy();
        }
        return stacks;
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
    public void invalidateCaps() {
        super.invalidateCaps();
        itemOptional.invalidate();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
