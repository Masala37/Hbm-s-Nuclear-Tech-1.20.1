package com.hbm.blockentity.machine;

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
 * 1.7.10 {@code TileEntityProxyCombo} on {@code machine_difurnace_extension}: forwards inventory
 * to the blast furnace one block below. Not Dummyable.
 */
public class DiFurnaceExtensionBlockEntity extends BlockEntity {
    public DiFurnaceExtensionBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DI_FURNACE_EXTENSION.get(), pos, state);
    }

    @Nullable
    public DiFurnaceBlockEntity core() {
        if (level == null) {
            return null;
        }
        BlockEntity below = level.getBlockEntity(worldPosition.below());
        return below instanceof DiFurnaceBlockEntity furnace ? furnace : null;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        DiFurnaceBlockEntity furnace = core();
        if (furnace != null && cap == ForgeCapabilities.ITEM_HANDLER) {
            return furnace.getCapability(cap, side);
        }
        return super.getCapability(cap, side);
    }
}
