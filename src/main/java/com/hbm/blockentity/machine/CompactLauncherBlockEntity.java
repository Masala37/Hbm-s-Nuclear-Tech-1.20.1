package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.CompactLauncherBlock;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.entity.missile.MissileSystemRules;
import com.hbm.inventory.menu.CompactLauncherMenu;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompactLauncherBlockEntity extends CustomLauncherBlockEntity {
    public CompactLauncherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPACT_LAUNCHER.get(), pos, state,
                MissileSystemRules.COMPACT_TANK, MissileSystemRules.COMPACT_SOLID);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CompactLauncherBlockEntity be) {
        CustomLauncherBlockEntity.serverTick(level, pos, state, be);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CompactLauncherBlockEntity be) {
        CustomLauncherBlockEntity.clientTick(level, pos, state, be);
    }

    @Override
    protected boolean needsDesignatorForCanLaunch() {
        return true;
    }

    @Override
    protected PartSize requiredTop() {
        return PartSize.SIZE_10;
    }

    @Override
    protected void pullConnections(Level level) {
        BlockPos pos = worldPosition;
        EnergyNetworkHelper.pullFrom(level, pos.offset(2, 0, 1), Direction.WEST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(2, 0, -1), Direction.WEST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-2, 0, 1), Direction.EAST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-2, 0, -1), Direction.EAST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(1, 0, 2), Direction.NORTH, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-1, 0, 2), Direction.NORTH, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(1, 0, -2), Direction.SOUTH, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-1, 0, -2), Direction.SOUTH, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(1, -1, 1), Direction.UP, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(1, -1, -1), Direction.UP, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-1, -1, 1), Direction.UP, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-1, -1, -1), Direction.UP, energy, ENERGY_TRANSFER);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 && dz != 0) {
                    EnergyNetworkHelper.pullFromNeighbors(level, pos.offset(dx, 0, dz), energy, ENERGY_TRANSFER);
                }
            }
        }
    }

    @Override
    protected boolean canConnectSide(@Nullable Direction side) {
        return side != Direction.UP;
    }

    @Override
    protected void tryCompleteStructure(Level level) {
        CompactLauncherBlock.tryCompleteStructure(level, worldPosition);
    }

    @Override
    protected float smokeSpread() {
        return 0.5F;
    }

    @Override
    protected int redstoneRadius() {
        return CompactLauncherBlock.GRID.radius;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 2.0D, worldPosition.getY(), worldPosition.getZ() - 2.0D,
                worldPosition.getX() + 3.0D, worldPosition.getY() + 16.0D, worldPosition.getZ() + 3.0D);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.compact_launcher");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new CompactLauncherMenu(id, inv, this);
    }
}
