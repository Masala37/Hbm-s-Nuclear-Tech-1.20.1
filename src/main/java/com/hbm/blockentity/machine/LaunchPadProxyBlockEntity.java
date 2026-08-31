package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.CompactLauncherBlock;
import com.hbm.blocks.machine.DummyGridOffsets;
import com.hbm.blocks.machine.LaunchPadBlock;
import com.hbm.blocks.machine.LaunchPadLargeBlock;
import com.hbm.blocks.machine.LaunchPadOffsets;
import com.hbm.blocks.machine.LaunchTableBlock;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Dummy-cell capability forwarder (1.7.10 {@code TileEntityProxyCombo}).
 * Inventory on silo / large-pad dummies; energy/fluid only on ports.
 * Compact launcher and launch table never forward inventory.
 */
public class LaunchPadProxyBlockEntity extends BlockEntity {
    public LaunchPadProxyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCH_PAD_PROXY.get(), pos, state);
    }

    @Nullable
    private BlockEntity core() {
        if (level == null) {
            return null;
        }
        BlockState state = getBlockState();
        if (state.getBlock() instanceof LaunchPadLargeBlock) {
            return LaunchPadLargeBlock.coreEntity(level, worldPosition, state);
        }
        if (state.getBlock() instanceof LaunchPadBlock) {
            return LaunchPadBlock.coreEntity(level, worldPosition, state);
        }
        if (state.getBlock() instanceof CompactLauncherBlock) {
            return CompactLauncherBlock.coreEntity(level, worldPosition, state);
        }
        if (state.getBlock() instanceof LaunchTableBlock) {
            return LaunchTableBlock.coreEntity(level, worldPosition, state);
        }
        return null;
    }

    private boolean powerFluidPort() {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof LaunchPadLargeBlock) {
            return DummyGridOffsets.LARGE.isPowerFluidPort(state.getValue(LaunchPadLargeBlock.OX),
                    state.getValue(LaunchPadLargeBlock.OZ));
        }
        if (state.getBlock() instanceof LaunchPadBlock) {
            return LaunchPadOffsets.isCorner(state.getValue(LaunchPadBlock.OX), state.getValue(LaunchPadBlock.OZ));
        }
        if (state.getBlock() instanceof CompactLauncherBlock) {
            return CompactLauncherBlock.isPort(state);
        }
        if (state.getBlock() instanceof LaunchTableBlock) {
            Direction facing = Direction.SOUTH;
            BlockEntity core = core();
            if (core instanceof LaunchTableBlockEntity table) {
                facing = table.getFacing();
            }
            return LaunchTableBlock.isPort(state, facing);
        }
        return false;
    }

    private boolean forwardInventory() {
        BlockState state = getBlockState();
        return state.getBlock() instanceof LaunchPadBlock || state.getBlock() instanceof LaunchPadLargeBlock;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        BlockEntity core = core();
        if (core != null) {
            if (cap == ForgeCapabilities.ITEM_HANDLER && forwardInventory()) {
                return core.getCapability(cap, side);
            }
            if (powerFluidPort() && (cap == ForgeCapabilities.ENERGY || cap == ForgeCapabilities.FLUID_HANDLER)) {
                return core.getCapability(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }
}
