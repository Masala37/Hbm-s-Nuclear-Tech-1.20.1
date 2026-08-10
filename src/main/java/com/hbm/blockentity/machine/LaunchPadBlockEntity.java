package com.hbm.blockentity.machine;

import com.hbm.entity.missile.EntityMissileGeneric;
import com.hbm.items.tool.DesignatorItem;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
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
 * Simple launch pad — 1 missile slot + stored target from designator.
 */
public class LaunchPadBlockEntity extends BlockEntity {
    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(ModItems.MISSILE_GENERIC.get());
        }
    };

    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> items);

    private boolean hasTarget;
    private int targetX;
    private int targetY;
    private int targetZ;
    private boolean wasPowered;

    public LaunchPadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCH_PAD.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    public BlockPos getTarget() {
        return new BlockPos(targetX, targetY, targetZ);
    }

    public void setTarget(BlockPos pos) {
        this.hasTarget = true;
        this.targetX = pos.getX();
        this.targetY = pos.getY();
        this.targetZ = pos.getZ();
        setChanged();
    }

    public boolean tryInsertMissile(ItemStack stack) {
        if (!stack.is(ModItems.MISSILE_GENERIC.get()) || !items.getStackInSlot(0).isEmpty()) {
            return false;
        }
        items.setStackInSlot(0, stack.split(1));
        return true;
    }

    public boolean trySetTargetFromDesignator(ItemStack stack) {
        if (!(stack.getItem() instanceof DesignatorItem) || !DesignatorItem.hasTarget(stack)) {
            return false;
        }
        setTarget(DesignatorItem.getTarget(stack));
        return true;
    }

    public boolean canLaunch() {
        return hasTarget && items.getStackInSlot(0).is(ModItems.MISSILE_GENERIC.get());
    }

    public boolean launch() {
        if (level == null || level.isClientSide || !canLaunch()) {
            return false;
        }
        items.setStackInSlot(0, ItemStack.EMPTY);
        BlockPos pos = worldPosition;
        EntityMissileGeneric missile = new EntityMissileGeneric(
                level,
                pos.getX() + 0.5D,
                pos.getY() + 1.5D,
                pos.getZ() + 0.5D,
                targetX, targetY, targetZ);
        level.addFreshEntity(missile);
        setChanged();
        return true;
    }

    public void checkRedstone(boolean powered) {
        if (powered && !wasPowered) {
            launch();
        }
        wasPowered = powered;
    }

    public void dropContents() {
        if (level != null && !level.isClientSide) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    items.getStackInSlot(0));
            items.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    public Component statusMessage() {
        if (!hasTarget) {
            return Component.literal("No target — use designator on pad");
        }
        if (items.getStackInSlot(0).isEmpty()) {
            return Component.literal("Target: " + targetX + ", " + targetY + ", " + targetZ + " — insert missile");
        }
        return Component.literal("Ready — empty hand or redstone to launch → "
                + targetX + ", " + targetY + ", " + targetZ);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putBoolean("hasTarget", hasTarget);
        tag.putInt("targetX", targetX);
        tag.putInt("targetY", targetY);
        tag.putInt("targetZ", targetZ);
        tag.putBoolean("wasPowered", wasPowered);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        hasTarget = tag.getBoolean("hasTarget");
        targetX = tag.getInt("targetX");
        targetY = tag.getInt("targetY");
        targetZ = tag.getInt("targetZ");
        wasPowered = tag.getBoolean("wasPowered");
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
