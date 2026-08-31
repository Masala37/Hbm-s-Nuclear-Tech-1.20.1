package com.hbm.blockentity.machine;

import api.hbm.item.IDesignatorItem;
import com.hbm.api.bomb.IBomb;
import com.hbm.entity.missile.EntityMissileDoomsdayRusted;
import com.hbm.inventory.menu.LaunchPadRustedMenu;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaunchPadRustedBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_OUTPUT = 0;
    public static final int SLOT_CODE = 1;
    public static final int SLOT_KEY = 2;
    public static final int SLOT_DESIGNATOR = 3;

    private final ItemStackHandler items = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            syncToClient();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    private boolean missileLoaded;
    private boolean wasPowered;
    private Direction facing = Direction.NORTH;

    public LaunchPadRustedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCH_PAD_RUSTED.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean isMissileLoaded() {
        return missileLoaded;
    }

    public Direction getFacing() {
        return facing == null ? Direction.NORTH : facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
        setChanged();
        syncToClient();
    }

    public void tryRelease() {
        if (missileLoaded && items.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            missileLoaded = false;
            items.setStackInSlot(SLOT_OUTPUT, new ItemStack(ModItems.MISSILE_DOOMSDAY_RUSTED.get()));
            setChanged();
            syncToClient();
        }
    }

    public IBomb.BombReturnCode launch() {
        if (level == null || level.isClientSide) {
            return IBomb.BombReturnCode.UNDEFINED;
        }
        ItemStack code = items.getStackInSlot(SLOT_CODE);
        ItemStack key = items.getStackInSlot(SLOT_KEY);
        ItemStack designator = items.getStackInSlot(SLOT_DESIGNATOR);
        BlockPos pad = worldPosition;
        if (!missileLoaded || code.isEmpty() || key.isEmpty()
                || code.getItem() != ModItems.LAUNCH_CODE.get()
                || key.getItem() != ModItems.LAUNCH_KEY.get()
                || !(designator.getItem() instanceof IDesignatorItem designatorItem)
                || !designatorItem.isReady(level, designator, pad.getX(), pad.getY(), pad.getZ())) {
            return IBomb.BombReturnCode.ERROR_MISSING_COMPONENT;
        }
        Vec3 coords = designatorItem.getCoords(level, designator, pad.getX(), pad.getY(), pad.getZ());
        int targetX = (int) Math.floor(coords.x);
        int targetY = (int) Math.floor(coords.y);
        int targetZ = (int) Math.floor(coords.z);
        level.playSound(null, pad.getX() + 0.5D, pad.getY(), pad.getZ() + 0.5D,
                ModSounds.MISSILE_TAKEOFF.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
        level.addFreshEntity(new EntityMissileDoomsdayRusted(
                level, pad.getX() + 0.5D, pad.getY() + 1.0D, pad.getZ() + 0.5D,
                targetX, targetY, targetZ));
        missileLoaded = false;
        items.setStackInSlot(SLOT_CODE, ItemStack.EMPTY);
        setChanged();
        syncToClient();
        return IBomb.BombReturnCode.LAUNCHED;
    }

    public void checkRedstone(boolean powered) {
        if (powered && !wasPowered) {
            launch();
        }
        wasPowered = powered;
    }

    public void dropContents() {
        if (level != null && !level.isClientSide) {
            for (int i = 0; i < items.getSlots(); i++) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                        items.getStackInSlot(i));
                items.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LaunchPadRustedBlockEntity be) {
        com.hbm.blocks.machine.LaunchPadBlock.tryCompleteStructure(level, pos);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, LaunchPadRustedBlockEntity be) {
        com.hbm.HbmNuclearTechMod.proxy.tickLaunchPadSmoke(level, pos);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 2.0D, worldPosition.getY(), worldPosition.getZ() - 2.0D,
                worldPosition.getX() + 3.0D, worldPosition.getY() + 15.0D, worldPosition.getZ() + 3.0D);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.launch_pad_rusted");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new LaunchPadRustedMenu(id, inv, this);
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putBoolean("missileLoaded", missileLoaded);
        tag.putBoolean("wasPowered", wasPowered);
        tag.putString("padFacing", getFacing().getSerializedName());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        missileLoaded = tag.getBoolean("missileLoaded");
        wasPowered = tag.getBoolean("wasPowered");
        if (tag.contains("padFacing")) {
            Direction loaded = Direction.byName(tag.getString("padFacing"));
            this.facing = loaded != null && loaded.getAxis().isHorizontal() ? loaded : Direction.NORTH;
        }
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
