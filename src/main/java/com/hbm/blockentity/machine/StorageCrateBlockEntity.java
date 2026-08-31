package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.StorageCrateBlock;
import com.hbm.inventory.menu.StorageCrateMenu;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageCrateBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler items;
    private final String translationKey;
    private LazyOptional<IItemHandler> itemOptional;

    public StorageCrateBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, resolveSlots(state), resolveKey(state));
    }

    public StorageCrateBlockEntity(BlockPos pos, BlockState state, int slots, String translationKey) {
        super(ModBlockEntities.STORAGE_CRATE.get(), pos, state);
        this.translationKey = translationKey;
        this.items = new ItemStackHandler(slots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        this.itemOptional = LazyOptional.of(() -> items);
    }

    private static int resolveSlots(BlockState state) {
        if (state.getBlock() instanceof StorageCrateBlock crate) {
            return crate.getSlots();
        }
        return 36;
    }

    private static String resolveKey(BlockState state) {
        if (state.getBlock() instanceof StorageCrateBlock crate) {
            return crate.getTranslationKey();
        }
        return "container.hbm.crate_iron";
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

    public void startOpen(Player player) {
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, ModSounds.CRATE_OPEN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    public void stopOpen(Player player) {
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, ModSounds.CRATE_CLOSE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new StorageCrateMenu(id, inv, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putString("TitleKey", translationKey);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemOptional = LazyOptional.of(() -> items);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
