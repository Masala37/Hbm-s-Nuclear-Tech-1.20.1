package com.hbm.blockentity.machine;

import com.hbm.blockentity.network.PylonBlockEntity;
import com.hbm.blockentity.network.PylonKind;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.energy.IEnergyConductor;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extra-cell capability forwarder for {@link BlockDummyable} (1.7 {@code TileEntityProxyCombo}).
 */
public class DummyableProxyBlockEntity extends BlockEntity implements IEnergyConductor {
    private long lastEnergyNetTick = Long.MIN_VALUE;

    public DummyableProxyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DUMMYABLE_PROXY.get(), pos, state);
    }

    public static void spawn(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockDummyable)
                || DummyableMeta.isCore(state.getValue(BlockDummyable.META))) {
            return;
        }
        BlockEntity existing = level.getBlockEntity(pos);
        if (existing instanceof DummyableProxyBlockEntity) {
            return;
        }
        if (existing != null) {
            level.removeBlockEntity(pos);
        }
        level.setBlockEntity(new DummyableProxyBlockEntity(pos, state));
    }

    @Nullable
    private BlockEntity core() {
        if (level == null || !(getBlockState().getBlock() instanceof BlockDummyable dummyable)) {
            return null;
        }
        BlockPos core = dummyable.findCore(level, worldPosition);
        if (core == null || core.equals(worldPosition)) {
            return null;
        }
        return level.getBlockEntity(core);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        BlockEntity core = core();
        if (core == null) {
            return super.getCapability(cap, side);
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return core.getCapability(cap, side);
        }
        // 1.7 extras are TileEntityProxyCombo (inv+power+fluid); other dummies are inventory only.
        if ((cap == ForgeCapabilities.ENERGY || cap == ForgeCapabilities.FLUID_HANDLER)
                && DummyableMeta.isExtra(getBlockState().getValue(BlockDummyable.META))) {
            return core.getCapability(cap, side);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean isEnergyConductor() {
        BlockEntity core = core();
        return core instanceof IEnergyConductor conductor && conductor.isEnergyConductor();
    }

    @Override
    public long lastEnergyNetTick() {
        return lastEnergyNetTick;
    }

    @Override
    public void markEnergyNetTick(long gameTime) {
        lastEnergyNetTick = gameTime;
    }

    @Override
    public boolean connectsEnergy(Direction dir) {
        BlockEntity core = core();
        if (!(core instanceof IEnergyConductor conductor) || !conductor.isEnergyConductor()) {
            return false;
        }
        if (core instanceof PylonBlockEntity pylon && pylon.kind() == PylonKind.SUBSTATION) {
            BlockPos corePos = core.getBlockPos();
            BlockPos neighbor = worldPosition.relative(dir);
            int dx = Math.abs(neighbor.getX() - corePos.getX());
            int dz = Math.abs(neighbor.getZ() - corePos.getZ());
            int dy = Math.abs(neighbor.getY() - corePos.getY());
            return dy == 0 && ((dx == 2 && dz == 1) || (dx == 1 && dz == 2));
        }
        return conductor.connectsEnergy(dir);
    }

    @Override
    public List<BlockPos> extraEnergyLinks() {
        if (!isEnergyConductor()) {
            return List.of();
        }
        if (level == null || !(getBlockState().getBlock() instanceof BlockDummyable dummyable)) {
            return List.of();
        }
        BlockPos core = dummyable.findCore(level, worldPosition);
        if (core == null || core.equals(worldPosition)) {
            return List.of();
        }
        return List.of(core);
    }
}
