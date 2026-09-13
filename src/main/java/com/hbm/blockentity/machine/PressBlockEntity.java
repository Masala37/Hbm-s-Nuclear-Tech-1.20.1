package com.hbm.blockentity.machine;

import com.hbm.inventory.menu.PressMenu;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.items.machine.ItemStamp;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityMachinePress}.
 */
public class PressBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_SPEED = 400;
    public static final int PROGRESS_AT_MAX = 25;
    public static final int MAX_PRESS = 200;
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_STAMP = 1;
    public static final int SLOT_INPUT = 2;
    public static final int SLOT_OUTPUT = 3;

    private final ItemStackHandler items = new ItemStackHandler(13) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_STAMP) {
                return stack.getItem() instanceof ItemStamp;
            }
            if (slot == SLOT_FUEL) {
                return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
            }
            if (slot == SLOT_OUTPUT) {
                return false;
            }
            if (slot == SLOT_INPUT) {
                return !(stack.getItem() instanceof ItemStamp);
            }
            return slot >= 4;
        }
    };

    private final IItemHandler automation = new IItemHandler() {
        @Override
        public int getSlots() {
            return 4;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return items.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot == SLOT_OUTPUT || slot < 0 || slot > SLOT_INPUT) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != SLOT_OUTPUT) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(slot, amount, simulate);
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

    private int speed;
    private int burnTime;
    private int press;
    private boolean retracting;
    private int delay;
    private double renderPress;
    private double lastPress;
    private int syncPress;
    private int turnProgress;
    private ItemStack syncStack = ItemStack.EMPTY;

    public PressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRESS.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getSpeed() {
        return speed;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getPress() {
        return press;
    }

    public double getRenderPress(float partialTick) {
        return lastPress + (renderPress - lastPress) * partialTick;
    }

    public ItemStack getSyncStack() {
        return syncStack;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new PressMenu(id, inv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PressBlockEntity be) {
        if (level.isClientSide) {
            be.clientTick();
        } else {
            be.serverTick(level, pos);
        }
    }

    private void clientTick() {
        lastPress = renderPress;
        if (turnProgress > 0) {
            renderPress = renderPress + ((syncPress - renderPress) / (double) turnProgress);
            turnProgress--;
        } else {
            renderPress = syncPress;
        }
    }

    private void serverTick(Level level, BlockPos pos) {
        boolean preheated = false;
        Block preheater = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("hbm", "press_preheater"));
        if (preheater != null) {
            for (Direction dir : Direction.values()) {
                if (level.getBlockState(pos.relative(dir)).is(preheater)) {
                    preheated = true;
                    break;
                }
            }
        }

        boolean canProcess = canProcess();
        if ((canProcess || retracting) && burnTime >= 200) {
            speed += preheated ? 4 : 1;
            if (speed > MAX_SPEED) {
                speed = MAX_SPEED;
            }
        } else {
            speed = Math.max(0, speed - 1);
        }

        if (delay <= 0) {
            int stampSpeed = speed * PROGRESS_AT_MAX / MAX_SPEED;
            if (retracting) {
                press -= stampSpeed;
                if (press <= 0) {
                    press = 0;
                    retracting = false;
                    delay = 5;
                }
            } else if (canProcess) {
                press += stampSpeed;
                if (press >= MAX_PRESS) {
                    level.playSound(null, pos, ModSounds.require("block.press_operate"), SoundSource.BLOCKS, 1.5F, 1.0F);
                    ItemStack stamp = items.getStackInSlot(SLOT_STAMP);
                    ItemStack output = recipeOutput();
                    ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
                    if (existing.isEmpty()) {
                        items.setStackInSlot(SLOT_OUTPUT, output.copy());
                    } else {
                        existing.grow(output.getCount());
                    }
                    items.getStackInSlot(SLOT_INPUT).shrink(1);
                    if (stamp.getMaxDamage() > 0) {
                        stamp.setDamageValue(stamp.getDamageValue() + 1);
                        if (stamp.getDamageValue() >= stamp.getMaxDamage()) {
                            items.setStackInSlot(SLOT_STAMP, ItemStack.EMPTY);
                        }
                    }
                    retracting = true;
                    delay = 5;
                    if (burnTime >= 200) {
                        burnTime -= 200;
                    }
                }
            } else if (press > 0) {
                retracting = true;
            }
        } else {
            delay--;
        }

        ItemStack fuel = items.getStackInSlot(SLOT_FUEL);
        if (!fuel.isEmpty() && burnTime < 200) {
            int burn = ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);
            if (burn > 0) {
                burnTime += burn;
                ItemStack remainder = fuel.getCraftingRemainingItem();
                fuel.shrink(1);
                if (fuel.isEmpty() && !remainder.isEmpty()) {
                    items.setStackInSlot(SLOT_FUEL, remainder);
                }
            }
        }

        boolean pressMoved = syncPress != press;
        syncPress = press;
        syncStack = items.getStackInSlot(SLOT_INPUT).copy();
        setChanged();
        if (pressMoved) {
            syncToClient();
        }
    }

    public boolean canProcess() {
        if (burnTime < 200) {
            return false;
        }
        ItemStack stamp = items.getStackInSlot(SLOT_STAMP);
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        if (stamp.isEmpty() || input.isEmpty() || !(stamp.getItem() instanceof ItemStamp itemStamp)) {
            return false;
        }
        ItemStack output = PressRecipes.getOutput(input, itemStamp.getStampType());
        if (output.isEmpty()) {
            return false;
        }
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        if (existing.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize();
    }

    private ItemStack recipeOutput() {
        ItemStack stamp = items.getStackInSlot(SLOT_STAMP);
        if (!(stamp.getItem() instanceof ItemStamp itemStamp)) {
            return ItemStack.EMPTY;
        }
        return PressRecipes.getOutput(items.getStackInSlot(SLOT_INPUT), itemStamp.getStampType());
    }

    private void onChanged() {
        setChanged();
        syncToClient();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 3, 1));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putInt("press", press);
        tag.putInt("burnTime", burnTime);
        tag.putInt("speed", speed);
        tag.putBoolean("ret", retracting);
        tag.putInt("delay", delay);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        press = tag.getInt("press");
        burnTime = tag.getInt("burnTime");
        speed = tag.getInt("speed");
        retracting = tag.getBoolean("ret");
        delay = tag.getInt("delay");
        syncPress = press;
        renderPress = press;
        lastPress = press;
        syncStack = items.getStackInSlot(SLOT_INPUT).copy();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = saveWithoutMetadata();
        tag.putInt("syncPress", press);
        tag.put("syncStack", items.getStackInSlot(SLOT_INPUT).save(new CompoundTag()));
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        syncPress = tag.getInt("syncPress");
        turnProgress = 2;
        if (tag.contains("syncStack")) {
            syncStack = ItemStack.of(tag.getCompound("syncStack"));
        }
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
            handleUpdateTag(tag);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemOptional.invalidate();
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
