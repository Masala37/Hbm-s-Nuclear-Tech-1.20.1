package com.hbm.blockentity.machine;

import com.hbm.inventory.menu.FileCabinetMenu;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7 {@code TileEntityFileCabinet}: 8 slots, no automation, drawer animation.
 */
public class FileCabinetBlockEntity extends BlockEntity implements MenuProvider {
    public static final float MAX_EXTENT = 0.8F;

    private final ItemStackHandler items = new ItemStackHandler(8) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int timer;
    private int playersUsing;
    public float lowerExtent;
    public float prevLowerExtent;
    public float upperExtent;
    public float prevUpperExtent;

    public FileCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FILE_CABINET.get(), pos, state);
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

    public void startOpen() {
        if (level != null && !level.isClientSide) {
            playersUsing++;
            syncUsers();
        }
    }

    public void stopOpen() {
        if (level != null && !level.isClientSide) {
            playersUsing = Math.max(0, playersUsing - 1);
            syncUsers();
        }
    }

    private void syncUsers() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FileCabinetBlockEntity be) {
        if (!level.isClientSide) {
            if (be.playersUsing > 0) {
                if (be.timer < 10) {
                    be.timer++;
                    be.syncUsers();
                }
            } else if (be.timer != 0) {
                be.timer = 0;
                be.syncUsers();
            }
            return;
        }

        be.prevLowerExtent = be.lowerExtent;
        be.prevUpperExtent = be.upperExtent;
        float openSpeed = be.playersUsing > 0 ? 1.0F / 16.0F : 1.0F / 25.0F;
        if (be.playersUsing > 0) {
            if (be.lowerExtent == 0.0F && be.upperExtent == 0.0F) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        ModSounds.CRATE_OPEN.get(), SoundSource.BLOCKS, 0.8F, 1.0F, false);
            } else {
                if (be.upperExtent + openSpeed >= MAX_EXTENT && be.lowerExtent < MAX_EXTENT) {
                    level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            ModSounds.CRATE_OPEN.get(), SoundSource.BLOCKS, 0.5F,
                            level.random.nextFloat() * 0.1F + 0.7F, false);
                }
                if (be.lowerExtent + openSpeed >= MAX_EXTENT && be.lowerExtent < MAX_EXTENT) {
                    level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            ModSounds.CRATE_OPEN.get(), SoundSource.BLOCKS, 0.5F,
                            level.random.nextFloat() * 0.1F + 0.7F, false);
                }
            }
            be.lowerExtent += openSpeed;
            if (be.timer >= 10) {
                be.upperExtent += openSpeed;
            }
        } else if (be.lowerExtent > 0.0F) {
            if (be.upperExtent - openSpeed < MAX_EXTENT / 2.0F && be.upperExtent >= MAX_EXTENT / 2.0F
                    && be.upperExtent != be.lowerExtent) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        ModSounds.CRATE_CLOSE.get(), SoundSource.BLOCKS, 0.8F, 1.0F, false);
            }
            if (be.lowerExtent - openSpeed < MAX_EXTENT / 2.0F && be.lowerExtent >= MAX_EXTENT / 2.0F) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        ModSounds.CRATE_CLOSE.get(), SoundSource.BLOCKS, 0.8F, 1.0F, false);
            }
            be.upperExtent -= openSpeed;
            be.lowerExtent -= openSpeed;
        }
        be.lowerExtent = Mth.clamp(be.lowerExtent, 0.0F, MAX_EXTENT);
        be.upperExtent = Mth.clamp(be.upperExtent, 0.0F, MAX_EXTENT);
    }

    public float getLower(float partialTick) {
        return Mth.lerp(partialTick, prevLowerExtent, lowerExtent);
    }

    public float getUpper(float partialTick) {
        return Mth.lerp(partialTick, prevUpperExtent, upperExtent);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm.file_cabinet");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new FileCabinetMenu(id, inv, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putInt("Timer", timer);
        tag.putInt("Users", playersUsing);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        timer = tag.getInt("Timer");
        playersUsing = tag.getInt("Users");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("Timer", timer);
        tag.putInt("Users", playersUsing);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        timer = tag.getInt("Timer");
        playersUsing = tag.getInt("Users");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag() == null ? new CompoundTag() : pkt.getTag());
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) {
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }
}
