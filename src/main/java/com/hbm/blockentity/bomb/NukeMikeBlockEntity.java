package com.hbm.blockentity.bomb;

import com.hbm.config.BombConfig;
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
 * Ivy Mike: primary (4 lenses + man core) or full secondary (mike core/deut/cooling).
 */
public class NukeMikeBlockEntity extends BlockEntity implements AssembledNuke {
    public static final int SLOT_LENS_1 = 0;
    public static final int SLOT_LENS_2 = 1;
    public static final int SLOT_LENS_3 = 2;
    public static final int SLOT_LENS_4 = 3;
    public static final int SLOT_MAN_CORE = 4;
    public static final int SLOT_MIKE_CORE = 5;
    public static final int SLOT_MIKE_DEUT = 6;
    public static final int SLOT_COOLING = 7;
    public static final int SLOT_COUNT = 8;

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

    public NukeMikeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUKE_MIKE.get(), pos, state);
    }

    @Override
    public ItemStackHandler getItems() {
        return items;
    }

    public static Item requiredItem(int slot) {
        return switch (slot) {
            case SLOT_LENS_1, SLOT_LENS_2, SLOT_LENS_3, SLOT_LENS_4 -> ModItems.EXPLOSIVE_LENSES.get();
            case SLOT_MAN_CORE -> ModItems.MAN_CORE.get();
            case SLOT_MIKE_CORE -> ModItems.MIKE_CORE.get();
            case SLOT_MIKE_DEUT -> ModItems.MIKE_DEUT.get();
            case SLOT_COOLING -> ModItems.MIKE_COOLING_UNIT.get();
            default -> throw new IllegalArgumentException("Invalid Ivy Mike slot: " + slot);
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
        for (int i = SLOT_MIKE_CORE; i <= SLOT_COOLING; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty() || !stack.is(requiredItem(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isReady() {
        return isPrimaryReady();
    }

    @Override
    public int resolveBlastRadius(int configuredRadius) {
        return isFilled() ? BombConfig.mikeRadius.get() : BombConfig.manRadius.get();
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
        if (item == ModItems.MIKE_CORE.get() && items.getStackInSlot(SLOT_MIKE_CORE).isEmpty()) {
            return SLOT_MIKE_CORE;
        }
        if (item == ModItems.MIKE_DEUT.get() && items.getStackInSlot(SLOT_MIKE_DEUT).isEmpty()) {
            return SLOT_MIKE_DEUT;
        }
        if (item == ModItems.MIKE_COOLING_UNIT.get() && items.getStackInSlot(SLOT_COOLING).isEmpty()) {
            return SLOT_COOLING;
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
            return Component.translatable("block.hbm.nuke_mike.ready_full");
        }
        if (isPrimaryReady()) {
            return Component.translatable("block.hbm.nuke_mike.ready_primary");
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
        if (items.getStackInSlot(SLOT_MIKE_CORE).isEmpty()) {
            append(missing, ModItems.MIKE_CORE.get());
        }
        if (items.getStackInSlot(SLOT_MIKE_DEUT).isEmpty()) {
            append(missing, ModItems.MIKE_DEUT.get());
        }
        if (items.getStackInSlot(SLOT_COOLING).isEmpty()) {
            append(missing, ModItems.MIKE_COOLING_UNIT.get());
        }
        return Component.translatable("block.hbm.nuke_mike.missing", missing.toString());
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
