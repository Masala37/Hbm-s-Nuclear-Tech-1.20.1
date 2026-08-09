package com.hbm.blockentity.machine;

import com.hbm.inventory.menu.FluidBarrelMenu;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Simple 16-bucket steel barrel. First machine proving Forge fluid capabilities.
 */
public class FluidBarrelBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 16_000;

    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> tank);

    public FluidBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_BARREL.get(), pos, state);
    }

    public FluidTank getTank() {
        return tank;
    }

    public IFluidHandler getFluidHandler() {
        return tank;
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
        // Passive storage for now; network/pipe transfer comes later.
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tank.writeToNBT(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag);
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
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
