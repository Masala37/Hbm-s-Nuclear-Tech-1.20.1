package com.hbm.blockentity.bomb;

import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
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
 * Fat Man assembly: igniter, four early explosive lenses, plutonium core.
 */
public class NukeManBlockEntity extends BlockEntity implements AssembledNuke {
    public static final int SLOT_IGNITER = 0;
    public static final int SLOT_LENS_1 = 1;
    public static final int SLOT_LENS_2 = 2;
    public static final int SLOT_LENS_3 = 3;
    public static final int SLOT_LENS_4 = 4;
    public static final int SLOT_CORE = 5;
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

    public NukeManBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUKE_MAN.get(), pos, state);
    }

    @Override
    public ItemStackHandler getItems() {
        return items;
    }

    public static Item requiredItem(int slot) {
        return switch (slot) {
            case SLOT_IGNITER -> ModItems.MAN_IGNITER.get();
            case SLOT_LENS_1, SLOT_LENS_2, SLOT_LENS_3, SLOT_LENS_4 -> ModItems.EARLY_EXPLOSIVE_LENSES.get();
            case SLOT_CORE -> ModItems.MAN_CORE.get();
            default -> throw new IllegalArgumentException("Invalid Fat Man slot: " + slot);
        };
    }

    /**
     * First empty slot that accepts this item, or -1.
     */
    @Override
    public int findInsertSlot(Item item) {
        if (item == ModItems.MAN_IGNITER.get() && items.getStackInSlot(SLOT_IGNITER).isEmpty()) {
            return SLOT_IGNITER;
        }
        if (item == ModItems.MAN_CORE.get() && items.getStackInSlot(SLOT_CORE).isEmpty()) {
            return SLOT_CORE;
        }
        if (item == ModItems.EARLY_EXPLOSIVE_LENSES.get()) {
            for (int slot = SLOT_LENS_1; slot <= SLOT_LENS_4; slot++) {
                if (items.getStackInSlot(slot).isEmpty()) {
                    return slot;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean isReady() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty() || !stack.is(requiredItem(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clearSlots() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public Component statusMessage() {
        if (isReady()) {
            return Component.translatable("block.hbm.nuke_man.ready");
        }
        StringBuilder missing = new StringBuilder();
        int missingLenses = 0;
        for (int i = SLOT_LENS_1; i <= SLOT_LENS_4; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty() || !stack.is(requiredItem(i))) {
                missingLenses++;
            }
        }
        if (items.getStackInSlot(SLOT_IGNITER).isEmpty()) {
            appendMissing(missing, ModItems.MAN_IGNITER.get());
        }
        if (missingLenses > 0) {
            if (!missing.isEmpty()) {
                missing.append(", ");
            }
            missing.append(missingLenses).append("x ")
                    .append(Component.translatable(ModItems.EARLY_EXPLOSIVE_LENSES.get().getDescriptionId()).getString());
        }
        if (items.getStackInSlot(SLOT_CORE).isEmpty()) {
            appendMissing(missing, ModItems.MAN_CORE.get());
        }
        return Component.translatable("block.hbm.nuke_man.missing", missing.toString());
    }

    private static void appendMissing(StringBuilder missing, Item item) {
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
