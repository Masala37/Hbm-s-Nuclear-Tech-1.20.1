package com.hbm.blockentity.machine;

import com.hbm.inventory.menu.FluidBarrelMenu;
import com.hbm.items.machine.InfiniteFluidBarrelItem;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7 {@code TileEntityBarrel} (steel): 16 buckets, fill/empty slots, I/O mode.
 */
public class FluidBarrelBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_ID_IN = 0;
    public static final int SLOT_ID_OUT = 1;
    public static final int SLOT_FILL_IN = 2;
    public static final int SLOT_FILL_OUT = 3;
    public static final int SLOT_EMPTY_IN = 4;
    public static final int SLOT_EMPTY_OUT = 5;
    public static final int SLOT_COUNT = 6;
    public static final int CAPACITY = 16_000;
    public static final int MODES = 4;

    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            onChanged();
        }
    };
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_ID_IN -> stack.is(ModItems.FLUID_IDENTIFIER.get());
                case SLOT_FILL_IN, SLOT_EMPTY_IN -> isFluidItem(stack);
                default -> false;
            };
        }
    };
    private final IItemHandler automation = new IItemHandler() {
        @Override
        public int getSlots() {
            return items.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return items.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot != SLOT_FILL_IN && slot != SLOT_EMPTY_IN) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != SLOT_FILL_OUT && slot != SLOT_EMPTY_OUT) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return items.isItemValid(slot, stack);
        }
    };
    private final IFluidHandler networked = new IFluidHandler() {
        @Override
        public int getTanks() {
            return tank.getTanks();
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluidInTank(tankIndex);
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return tank.getTankCapacity(tankIndex);
        }

        @Override
        public boolean isFluidValid(int tankIndex, @NotNull FluidStack stack) {
            return mode == 0 || mode == 1;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (mode == 2 || mode == 3) {
                return 0;
            }
            return tank.fill(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (mode == 0 || mode == 3) {
                return FluidStack.EMPTY;
            }
            return tank.drain(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (mode == 0 || mode == 3) {
                return FluidStack.EMPTY;
            }
            return tank.drain(maxDrain, action);
        }
    };

    private final LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> networked);
    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);

    private short mode;

    public FluidBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_BARREL.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public FluidTank getTank() {
        return tank;
    }

    public IFluidHandler getFluidHandler() {
        return tank;
    }

    public int getMode() {
        return mode;
    }

    public void cycleMode() {
        mode = (short) ((mode + 1) % MODES);
        onChanged();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.fluid_barrel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new FluidBarrelMenu(id, inv, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidBarrelBlockEntity be) {
        if (be.items.getStackInSlot(SLOT_FILL_IN).getItem() instanceof InfiniteFluidBarrelItem) {
            InfiniteFluidBarrelItem.fillInto(be.tank, InfiniteFluidBarrelItem.FILL_AMOUNT);
        } else {
            LauncherFluidTransfer.emptyContainer(be.items, SLOT_FILL_IN, SLOT_FILL_OUT, be.tank);
        }
        LauncherFluidTransfer.fillContainer(be.items, SLOT_EMPTY_IN, SLOT_EMPTY_OUT, be.tank);
    }

    static boolean isFluidItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof InfiniteFluidBarrelItem) {
            return true;
        }
        return FluidUtil.getFluidHandler(stack).isPresent();
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tank.writeToNBT(tag);
        tag.put("Items", items.serializeNBT());
        tag.putShort("Mode", mode);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        mode = tag.getShort("Mode");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidOptional.invalidate();
        itemOptional.invalidate();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidOptional.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
