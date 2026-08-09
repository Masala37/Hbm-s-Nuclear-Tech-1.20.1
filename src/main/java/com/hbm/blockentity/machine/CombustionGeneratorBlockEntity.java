package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.CombustionGeneratorBlock;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Single-block solid-fuel generator. Burns vanilla furnace fuels at 100 FE/t.
 */
public class CombustionGeneratorBlockEntity extends BlockEntity {
    public static final int CAPACITY = 100_000;
    public static final int MAX_EXTRACT = 500;
    public static final int GENERATION_RATE = 100;

    private final ModEnergyStorage energy = new ModEnergyStorage(CAPACITY, 0, MAX_EXTRACT, this::onChanged);
    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ForgeHooks.getBurnTime(stack, null) > 0;
        }
    };

    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> items);

    private int burnTime;
    private int burnTimeTotal;

    public CombustionGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMBUSTION_GENERATOR.get(), pos, state);
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getBurnTimeTotal() {
        return burnTimeTotal;
    }

    public boolean isBurning() {
        return burnTime > 0;
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CombustionGeneratorBlockEntity be) {
        boolean wasBurning = be.isBurning();

        if (be.burnTime <= 0) {
            ItemStack fuel = be.items.getStackInSlot(0);
            int burn = ForgeHooks.getBurnTime(fuel, null);
            if (burn > 0 && be.energy.getEnergyStored() < be.energy.getMaxEnergyStored()) {
                be.burnTime = burn;
                be.burnTimeTotal = burn;

                ItemStack remainder = fuel.hasCraftingRemainingItem() ? fuel.getCraftingRemainingItem() : ItemStack.EMPTY;
                fuel.shrink(1);
                if (fuel.isEmpty()) {
                    be.items.setStackInSlot(0, remainder);
                } else {
                    be.items.setStackInSlot(0, fuel);
                }
            }
        }

        if (be.burnTime > 0) {
            be.burnTime--;
            int room = be.energy.getMaxEnergyStored() - be.energy.getEnergyStored();
            if (room > 0) {
                be.energy.setEnergy(be.energy.getEnergyStored() + Math.min(GENERATION_RATE, room));
            }
        }

        EnergyNetworkHelper.pushToNeighbors(level, pos, be.energy, MAX_EXTRACT);

        boolean burning = be.isBurning();
        if (state.getValue(CombustionGeneratorBlock.LIT) != burning) {
            level.setBlock(pos, state.setValue(CombustionGeneratorBlock.LIT, burning), Block.UPDATE_CLIENTS);
        }

        if (wasBurning != burning) {
            be.onChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        burnTime = tag.getInt("BurnTime");
        burnTimeTotal = tag.getInt("BurnTimeTotal");
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
        energyOptional.invalidate();
        itemOptional.invalidate();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
