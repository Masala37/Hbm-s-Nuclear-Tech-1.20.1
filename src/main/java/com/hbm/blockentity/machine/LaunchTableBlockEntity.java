package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.DummyGridOffsets;
import com.hbm.blocks.machine.LaunchTableBlock;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.entity.missile.MissileSystemRules;
import com.hbm.inventory.menu.LaunchTableMenu;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaunchTableBlockEntity extends CustomLauncherBlockEntity {
    private PartSize padSize = PartSize.SIZE_10;
    private Direction facing = Direction.SOUTH;
    public int height = 10;

    public LaunchTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCH_TABLE.get(), pos, state,
                MissileSystemRules.TABLE_TANK, MissileSystemRules.TABLE_SOLID);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LaunchTableBlockEntity be) {
        CustomLauncherBlockEntity.serverTick(level, pos, state, be);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, LaunchTableBlockEntity be) {
        CustomLauncherBlockEntity.clientTick(level, pos, state, be);
    }

    public PartSize getPadSize() {
        return padSize;
    }

    public void setPadSize(PartSize padSize) {
        if (padSize == null || padSize == this.padSize) {
            return;
        }
        this.padSize = padSize;
        onChanged();
    }

    public Direction getFacing() {
        return facing == null ? Direction.SOUTH : facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing.getAxis().isHorizontal() ? facing : Direction.SOUTH;
        setChanged();
        syncToClient();
    }

    @Override
    protected boolean needsDesignatorForCanLaunch() {
        return false;
    }

    @Override
    protected PartSize requiredTop() {
        return padSize;
    }

    @Override
    protected void pullConnections(Level level) {
        BlockPos pos = worldPosition;
        for (int i = -DummyGridOffsets.LARGE.radius; i <= DummyGridOffsets.LARGE.radius; i++) {
            EnergyNetworkHelper.pullFrom(level, pos.offset(i, 0, 5), Direction.NORTH, energy, ENERGY_TRANSFER);
            EnergyNetworkHelper.pullFrom(level, pos.offset(i, 0, -5), Direction.SOUTH, energy, ENERGY_TRANSFER);
            EnergyNetworkHelper.pullFrom(level, pos.offset(5, 0, i), Direction.WEST, energy, ENERGY_TRANSFER);
            EnergyNetworkHelper.pullFrom(level, pos.offset(-5, 0, i), Direction.EAST, energy, ENERGY_TRANSFER);
        }
        for (int dx = -DummyGridOffsets.LARGE.radius; dx <= DummyGridOffsets.LARGE.radius; dx++) {
            for (int dz = -DummyGridOffsets.LARGE.radius; dz <= DummyGridOffsets.LARGE.radius; dz++) {
                if (LaunchTableBlock.isPort(dx, dz, getFacing())) {
                    EnergyNetworkHelper.pullFromNeighbors(level, pos.offset(dx, 0, dz), energy, ENERGY_TRANSFER);
                }
            }
        }
    }

    @Override
    protected boolean canConnectSide(@Nullable Direction side) {
        return side != Direction.UP && side != Direction.DOWN;
    }

    @Override
    protected void tryCompleteStructure(Level level) {
        LaunchTableBlock.tryCompleteStructure(level, worldPosition);
    }

    @Override
    protected float smokeSpread() {
        return 0.65F;
    }

    @Override
    protected int redstoneRadius() {
        return LaunchTableBlock.GRID.radius;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 6.0D, worldPosition.getY(), worldPosition.getZ() - 6.0D,
                worldPosition.getX() + 7.0D, worldPosition.getY() + Math.max(24.0D, height + 8.0D),
                worldPosition.getZ() + 7.0D);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("padSize", padSize.ordinal());
        tag.putString("tableFacing", getFacing().getSerializedName());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        int ordinal = tag.getInt("padSize");
        PartSize[] values = PartSize.values();
        if (ordinal >= 0 && ordinal < values.length) {
            padSize = values[ordinal];
        }
        if (tag.contains("tableFacing")) {
            Direction loaded = Direction.byName(tag.getString("tableFacing"));
            facing = loaded != null && loaded.getAxis().isHorizontal() ? loaded : Direction.SOUTH;
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.launch_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new LaunchTableMenu(id, inv, this);
    }
}
