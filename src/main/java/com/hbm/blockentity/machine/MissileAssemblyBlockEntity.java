package com.hbm.blockentity.machine;

import com.hbm.entity.missile.MissileAssemblyRecipes;
import com.hbm.inventory.menu.MissileAssemblyMenu;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy {@code TileEntityMachineMissileAssembly}: chip + warhead + fuselage + optional fins + thruster
 * → {@code missile_custom}. Hoppers cannot insert or extract.
 */
public class MissileAssemblyBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_CHIP = 0;
    public static final int SLOT_WARHEAD = 1;
    public static final int SLOT_FUSELAGE = 2;
    public static final int SLOT_FINS = 3;
    public static final int SLOT_THRUSTER = 4;
    public static final int SLOT_OUTPUT = 5;

    private final ItemStackHandler items = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot != SLOT_OUTPUT;
        }
    };

    public MissileAssemblyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MISSILE_ASSEMBLY.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ItemStack chip() {
        return items.getStackInSlot(SLOT_CHIP);
    }

    public ItemStack warhead() {
        return items.getStackInSlot(SLOT_WARHEAD);
    }

    public ItemStack fuselage() {
        return items.getStackInSlot(SLOT_FUSELAGE);
    }

    public ItemStack fins() {
        return items.getStackInSlot(SLOT_FINS);
    }

    public ItemStack thruster() {
        return items.getStackInSlot(SLOT_THRUSTER);
    }

    public ItemStack output() {
        return items.getStackInSlot(SLOT_OUTPUT);
    }

    public int chipState() {
        return MissileAssemblyRecipes.chipState(chip());
    }

    public int fuselageState() {
        return MissileAssemblyRecipes.fuselageState(fuselage());
    }

    public int warheadState() {
        return MissileAssemblyRecipes.warheadState(warhead(), fuselage(), thruster());
    }

    public int stabilityState() {
        return MissileAssemblyRecipes.stabilityState(fins(), fuselage());
    }

    public int thrusterState() {
        return MissileAssemblyRecipes.thrusterState(thruster(), fuselage());
    }

    public boolean canAssemble() {
        return MissileAssemblyRecipes.canBuild(chip(), warhead(), fuselage(), fins(), thruster(), output());
    }

    public boolean tryAssemble() {
        if (!canAssemble()) {
            return false;
        }
        ItemStack result = MissileAssemblyRecipes.construct(chip(), warhead(), fuselage(), fins(), thruster());
        boolean consumeFins = stabilityState() == 1;
        items.setStackInSlot(SLOT_OUTPUT, result);
        if (consumeFins) {
            items.setStackInSlot(SLOT_FINS, ItemStack.EMPTY);
        }
        items.setStackInSlot(SLOT_CHIP, ItemStack.EMPTY);
        items.setStackInSlot(SLOT_WARHEAD, ItemStack.EMPTY);
        items.setStackInSlot(SLOT_FUSELAGE, ItemStack.EMPTY);
        items.setStackInSlot(SLOT_THRUSTER, ItemStack.EMPTY);
        if (level != null) {
            level.playSound(null, worldPosition, ModSounds.MISSILE_ASSEMBLY.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        setChanged();
        return true;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.missileAssembly");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new MissileAssemblyMenu(id, inv, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(16.0D, 24.0D, 16.0D);
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
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
