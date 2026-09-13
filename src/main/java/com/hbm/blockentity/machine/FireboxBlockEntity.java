package com.hbm.blockentity.machine;

import api.hbm.tile.IHeatSource;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.heat.HeatBuffer;
import com.hbm.inventory.menu.FireboxMenu;
import com.hbm.inventory.recipes.FireboxBurnTime;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityHeaterFirebox}.
 */
public class FireboxBlockEntity extends BlockEntity implements MenuProvider, IHeatSource {
    public static final int SLOT_COUNT = 2;
    public static final int MAX_HEAT = 100_000;
    public static final int BASE_HEAT = 100;
    public static final double TIME_MULT = 1.0D;

    private final HeatBuffer heat = new HeatBuffer(MAX_HEAT);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return FireboxBurnTime.burnTime(stack, TIME_MULT) > 0;
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
            if (!items.isItemValid(slot, stack)) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
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
    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);

    private int maxBurnTime;
    private int burnTime;
    private int burnHeat;
    private boolean wasOn;
    private int playersUsing;
    private float doorAngle;
    private float prevDoorAngle;

    public FireboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEATER_FIREBOX.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HeatBuffer getHeat() {
        return heat;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public int getBurnHeat() {
        return burnHeat;
    }

    public boolean wasOn() {
        return wasOn;
    }

    public float getDoorAngle(float partialTick) {
        return Mth.lerp(partialTick, prevDoorAngle, doorAngle);
    }

    public void openLid() {
        if (level != null && !level.isClientSide) {
            playersUsing++;
            onChanged();
        }
    }

    public void closeLid() {
        if (level != null && !level.isClientSide) {
            playersUsing = Math.max(0, playersUsing - 1);
            onChanged();
        }
    }

    @Override
    public int getHeatStored() {
        return heat.getHeatStored();
    }

    @Override
    public void useUpHeat(int amount) {
        heat.useUpHeat(amount);
        onChanged();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.heaterFirebox");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new FireboxMenu(id, inv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FireboxBlockEntity be) {
        if (level.isClientSide) {
            be.clientTick(level, pos, state);
        } else {
            be.serverTick(level, pos);
        }
    }

    private void clientTick(Level level, BlockPos pos, BlockState state) {
        prevDoorAngle = doorAngle;
        float swing = (doorAngle / 10.0F) + 3.0F;
        if (playersUsing > 0) {
            doorAngle += swing;
        } else {
            doorAngle -= swing;
        }
        doorAngle = Mth.clamp(doorAngle, 0.0F, 135.0F);
        if (wasOn && level.getGameTime() % 5 == 0) {
            int facing = DummyableMeta.coreFacing(state.getValue(BlockDummyable.META));
            double x = pos.getX() + 0.5 + DummyableMeta.offsetX(facing);
            double y = pos.getY() + 0.25;
            double z = pos.getZ() + 0.5 + DummyableMeta.offsetZ(facing);
            level.addParticle(ParticleTypes.FLAME,
                    x + level.random.nextDouble() * 0.5 - 0.25,
                    y + level.random.nextDouble() * 0.25,
                    z + level.random.nextDouble() * 0.5 - 0.25,
                    0.0, 0.0, 0.0);
        }
    }

    private void serverTick(Level level, BlockPos pos) {
        wasOn = false;
        if (burnTime <= 0) {
            for (int i = 0; i < SLOT_COUNT; i++) {
                ItemStack stack = items.getStackInSlot(i);
                int fuel = FireboxBurnTime.burnTime(stack, TIME_MULT);
                if (fuel <= 0) {
                    continue;
                }
                maxBurnTime = burnTime = fuel;
                burnHeat = FireboxBurnTime.burnHeat(BASE_HEAT, stack);
                ItemStack extracted = items.extractItem(i, 1, false);
                if (items.getStackInSlot(i).isEmpty() && extracted.hasCraftingRemainingItem()) {
                    items.setStackInSlot(i, extracted.getCraftingRemainingItem());
                }
                wasOn = true;
                break;
            }
        } else {
            if (heat.getHeatStored() < MAX_HEAT) {
                burnTime--;
            }
            wasOn = true;
            if (level.random.nextInt(15) == 0) {
                level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
                        1.0F, 0.5F + level.random.nextFloat() * 0.5F);
            }
        }
        if (wasOn) {
            heat.addHeat(burnHeat);
        } else {
            heat.setHeat(Math.max(heat.getHeatStored() - Math.max(heat.getHeatStored() / 1000, 1), 0));
            burnHeat = 0;
        }
        onChanged();
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 1, 2));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putInt("Heat", heat.getHeatStored());
        tag.putInt("BurnTime", burnTime);
        tag.putInt("MaxBurnTime", maxBurnTime);
        tag.putInt("BurnHeat", burnHeat);
        tag.putBoolean("WasOn", wasOn);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        heat.setHeat(tag.getInt("Heat"));
        burnTime = tag.getInt("BurnTime");
        maxBurnTime = tag.getInt("MaxBurnTime");
        burnHeat = tag.getInt("BurnHeat");
        wasOn = tag.getBoolean("WasOn");
        if (tag.contains("PlayersUsing")) {
            playersUsing = tag.getInt("PlayersUsing");
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = saveWithoutMetadata();
        tag.putInt("PlayersUsing", playersUsing);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemOptional.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
