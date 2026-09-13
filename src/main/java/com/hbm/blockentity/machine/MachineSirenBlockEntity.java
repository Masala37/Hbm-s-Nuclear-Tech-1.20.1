package com.hbm.blockentity.machine;

import com.hbm.inventory.menu.MachineSirenMenu;
import com.hbm.items.machine.ItemCassette;
import com.hbm.items.machine.ItemCassette.SoundType;
import com.hbm.items.machine.ItemCassette.TrackType;
import com.hbm.network.ModMessages;
import com.hbm.network.SirenPacket;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Redstone-triggered siren. LOOP tracks play while powered; PASS/SOUND fire once on a rising edge.
 */
public class MachineSirenBlockEntity extends BlockEntity implements MenuProvider {
    public static final int RANGE = 1500;

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof ItemCassette;
        }
    };

    private boolean lock;

    public MachineSirenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_SIREN.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public SimpleContainer asContainer() {
        SimpleContainer container = new SimpleContainer(items.getSlots());
        for (int i = 0; i < items.getSlots(); i++) {
            container.setItem(i, items.getStackInSlot(i).copy());
        }
        return container;
    }

    public TrackType getCurrentType() {
        return ItemCassette.getType(items.getStackInSlot(0));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineSirenBlockEntity siren) {
        if (level.isClientSide) {
            return;
        }
        TrackType type = siren.getCurrentType();
        int id = type.ordinal();
        if (type == TrackType.NULL) {
            siren.send(id, false);
            return;
        }
        boolean active = level.hasNeighborSignal(pos);
        if (type.getType() == SoundType.LOOP) {
            siren.send(id, active);
            return;
        }
        if (!siren.lock && active) {
            siren.lock = true;
            siren.send(id, false);
            siren.send(id, true);
        }
        if (siren.lock && !active) {
            siren.lock = false;
        }
    }

    private void send(int id, boolean active) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        ModMessages.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D,
                        RANGE, server.dimension())),
                new SirenPacket(worldPosition, id, active));
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel) {
            send(0, false);
        }
        super.setRemoved();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm.siren");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new MachineSirenMenu(id, inv, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putBoolean("lock", lock);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        lock = tag.getBoolean("lock");
    }
}
