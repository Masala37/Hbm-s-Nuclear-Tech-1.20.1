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
 * Little Boy assembly inventory: shielding, target, bullet, propellant, igniter.
 */
public class NukeBoyBlockEntity extends BlockEntity {
    public static final int SLOT_SHIELDING = 0;
    public static final int SLOT_TARGET = 1;
    public static final int SLOT_BULLET = 2;
    public static final int SLOT_PROPELLANT = 3;
    public static final int SLOT_IGNITER = 4;
    public static final int SLOT_COUNT = 5;

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

    public NukeBoyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUKE_BOY.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public static Item requiredItem(int slot) {
        return switch (slot) {
            case SLOT_SHIELDING -> ModItems.BOY_SHIELDING.get();
            case SLOT_TARGET -> ModItems.BOY_TARGET.get();
            case SLOT_BULLET -> ModItems.BOY_BULLET.get();
            case SLOT_PROPELLANT -> ModItems.BOY_PROPELLANT.get();
            case SLOT_IGNITER -> ModItems.BOY_IGNITER.get();
            default -> throw new IllegalArgumentException("Invalid Little Boy slot: " + slot);
        };
    }

    public static int slotForItem(Item item) {
        if (item == ModItems.BOY_SHIELDING.get()) {
            return SLOT_SHIELDING;
        }
        if (item == ModItems.BOY_TARGET.get()) {
            return SLOT_TARGET;
        }
        if (item == ModItems.BOY_BULLET.get()) {
            return SLOT_BULLET;
        }
        if (item == ModItems.BOY_PROPELLANT.get()) {
            return SLOT_PROPELLANT;
        }
        if (item == ModItems.BOY_IGNITER.get()) {
            return SLOT_IGNITER;
        }
        return -1;
    }

    public boolean isReady() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty() || !stack.is(requiredItem(i))) {
                return false;
            }
        }
        return true;
    }

    public void clearSlots() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public Component statusMessage() {
        if (isReady()) {
            return Component.translatable("block.hbm.nuke_boy.ready");
        }
        StringBuilder missing = new StringBuilder();
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty() || !stack.is(requiredItem(i))) {
                if (!missing.isEmpty()) {
                    missing.append(", ");
                }
                missing.append(Component.translatable(requiredItem(i).getDescriptionId()).getString());
            }
        }
        return Component.translatable("block.hbm.nuke_boy.missing", missing.toString());
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        Containers.dropContents(level, worldPosition, new net.minecraft.world.SimpleContainer(copyStacks()));
        clearSlots();
    }

    private ItemStack[] copyStacks() {
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
