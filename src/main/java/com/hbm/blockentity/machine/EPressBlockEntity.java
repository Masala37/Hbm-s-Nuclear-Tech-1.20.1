package com.hbm.blockentity.machine;

import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.EPressMenu;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.items.machine.ItemStamp;
import com.hbm.lib.RefStrings;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityMachineEPress}.
 */
public class EPressBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_POWER = 50_000;
    public static final int ENERGY_PER_TICK = 100;
    public static final int MAX_PRESS = 200;
    public static final int MAX_SPEED_UPGRADE = 3;
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_STAMP = 1;
    public static final int SLOT_INPUT = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_UPGRADE = 4;

    private final ModEnergyStorage energy = new ModEnergyStorage(MAX_POWER, MAX_POWER, 0, this::setChanged);
    private final ItemStackHandler items = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_STAMP) {
                return stack.getItem() instanceof ItemStamp;
            }
            if (slot == SLOT_BATTERY) {
                return ItemEnergyHelper.isEnergyItem(stack);
            }
            if (slot == SLOT_UPGRADE) {
                return isUpgradeItem(stack);
            }
            if (slot == SLOT_OUTPUT) {
                return false;
            }
            return slot == SLOT_INPUT && !(stack.getItem() instanceof ItemStamp);
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot == SLOT_STAMP || slot == SLOT_UPGRADE || slot == SLOT_BATTERY) {
                return 1;
            }
            return super.getSlotLimit(slot);
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
            if (slot != SLOT_STAMP && slot != SLOT_INPUT) {
                return stack;
            }
            if (!items.isItemValid(slot, stack)) {
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

    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);

    private int press;
    private boolean retracting;
    private int delay;
    private double renderPress;
    private double lastPress;
    private int syncPress;
    private int turnProgress;
    private ItemStack syncStack = ItemStack.EMPTY;

    public EPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EPRESS.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ModEnergyStorage getEnergy() {
        return energy;
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
        return Component.translatable("container.epress");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new EPressMenu(id, inv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EPressBlockEntity be) {
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
        ItemEnergyHelper.chargeFromItem(items.getStackInSlot(SLOT_BATTERY), energy, MAX_POWER);
        boolean canProcess = canProcess();
        if ((canProcess || retracting || delay > 0) && energy.getEnergyStored() >= ENERGY_PER_TICK) {
            energy.consume(ENERGY_PER_TICK);
            if (delay <= 0) {
                int speed = 1 + speedUpgradeLevel(items.getStackInSlot(SLOT_UPGRADE));
                int stampSpeed = retracting ? 20 : 45;
                stampSpeed = (int) (stampSpeed * (1.0D + speed / 4.0D));
                if (retracting) {
                    press -= stampSpeed;
                    if (press <= 0) {
                        press = 0;
                        retracting = false;
                        delay = 5 - speed + 1;
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
                        delay = 5 - speed + 1;
                    }
                } else if (press > 0) {
                    retracting = true;
                }
            } else {
                delay--;
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
        if (energy.getEnergyStored() < ENERGY_PER_TICK) {
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

    public static int speedUpgradeLevel(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !RefStrings.MODID.equals(id.getNamespace())) {
            return 0;
        }
        int level = switch (id.getPath()) {
            case "upgrade_speed_1" -> 1;
            case "upgrade_speed_2" -> 2;
            case "upgrade_speed_3" -> 3;
            default -> 0;
        };
        return Math.min(MAX_SPEED_UPGRADE, level);
    }

    public static boolean isUpgradeItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && RefStrings.MODID.equals(id.getNamespace()) && id.getPath().startsWith("upgrade_");
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
        energy.write(tag);
        tag.putInt("press", press);
        tag.putBoolean("ret", retracting);
        tag.putInt("delay", delay);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        energy.read(tag);
        if (!tag.contains("Energy") && tag.contains("power")) {
            energy.setEnergy((int) tag.getLong("power"));
        }
        press = tag.getInt("press");
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
        energyOptional.invalidate();
        itemOptional.invalidate();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
