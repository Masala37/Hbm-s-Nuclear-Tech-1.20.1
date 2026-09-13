package com.hbm.blockentity.machine;

import com.hbm.inventory.menu.SilexMenu;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.inventory.recipes.SILEXRecipes.SILEXRecipe;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModFluids;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntitySILEX}. OpenComputers / energy-control skipped.
 */
public class SilexBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FLUID_ID = 1;
    public static final int SLOT_FLUID_IN = 2;
    public static final int SLOT_FLUID_OUT = 3;
    public static final int SLOT_OUTPUT = 4;
    public static final int SLOT_QUEUE_0 = 5;
    public static final int SLOT_QUEUE_5 = 10;
    public static final int SLOT_COUNT = 11;

    public static final int TANK_CAPACITY = 16_000;
    public static final int MAX_FILL = 16_000;
    public static final int PROCESS_TIME = 100;
    public static final int PRIME = 137;

    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return !stack.isEmpty() && stack.getFluid() == tankType;
        }
    };
    private final IFluidHandler fluids = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluid();
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tankIndex, @NotNull FluidStack stack) {
            return tank.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return tank.fill(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    };
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return SilexBlockEntity.this.mayPlace(slot, stack);
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
            if (slot != SLOT_INPUT) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < SLOT_QUEUE_0 || slot > SLOT_QUEUE_5) {
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

    private LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> fluids);
    private LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);

    private Fluid tankType;
    private EnumWavelengths mode = EnumWavelengths.NULL;
    private String currentKey = "";
    private int currentFill;
    private int progress;
    private int recipeIndex;
    private int loadDelay;
    private boolean hadIdentifier;

    public SilexBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_SILEX.get(), pos, state);
        tankType = ModFluids.PEROXIDE.source.get();
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public FluidTank getTank() {
        return tank;
    }

    public Fluid getTankType() {
        return tankType != null ? tankType : ModFluids.PEROXIDE.source.get();
    }

    public void setTankType(Fluid type) {
        if (type == null || !SILEXRecipes.isTankFluid(type) || type == tankType) {
            return;
        }
        tankType = type;
        tank.setFluid(FluidStack.EMPTY);
        onChanged();
    }

    public EnumWavelengths getMode() {
        return mode == null ? EnumWavelengths.NULL : mode;
    }

    public void setMode(EnumWavelengths mode) {
        this.mode = mode == null ? EnumWavelengths.NULL : mode;
        onChanged();
    }

    public int getCurrentFill() {
        return currentFill;
    }

    public int getProgress() {
        return progress;
    }

    public String getCurrentKey() {
        return currentKey == null ? "" : currentKey;
    }

    public ItemStack currentStack() {
        String key = getCurrentKey();
        if (key.isEmpty() || key.startsWith("fluid:")) {
            return ItemStack.EMPTY;
        }
        ResourceLocation id = ResourceLocation.tryParse(key);
        if (id == null || !ForgeRegistries.ITEMS.containsKey(id)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(ForgeRegistries.ITEMS.getValue(id));
    }

    public int getProgressScaled(int i) {
        return (progress * i) / PROCESS_TIME;
    }

    public int getFluidScaled(int i) {
        return (tank.getFluidAmount() * i) / TANK_CAPACITY;
    }

    public int getFillScaled(int i) {
        return (currentFill * i) / MAX_FILL;
    }

    public void voidContents() {
        currentFill = 0;
        currentKey = "";
        progress = 0;
        onChanged();
    }

    public boolean mayPlace(int slot, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case SLOT_INPUT -> SILEXRecipes.getOutput(stack) != null;
            case SLOT_FLUID_ID -> stack.is(ModItems.FLUID_IDENTIFIER.get());
            case SLOT_FLUID_IN -> true;
            default -> false;
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SilexBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        be.serverTick();
    }

    private void serverTick() {
        applyIdentifier();
        LauncherFluidTransfer.emptyContainer(items, SLOT_FLUID_IN, SLOT_FLUID_OUT, tank);
        loadFluid();
        if (!process()) {
            progress = 0;
        }
        dequeue();
        if (currentFill <= 0) {
            currentKey = "";
        }
        onChanged();
        mode = EnumWavelengths.NULL;
    }

    private void applyIdentifier() {
        boolean now = items.getStackInSlot(SLOT_FLUID_ID).is(ModItems.FLUID_IDENTIFIER.get());
        if (now && !hadIdentifier) {
            setTankType(SILEXRecipes.cycleNext(getTankType()));
        }
        hadIdentifier = now;
        if (!SILEXRecipes.isTankFluid(getTankType())) {
            Fluid first = SILEXRecipes.cycleNext(null);
            if (first != null) {
                setTankType(first);
            }
        }
    }

    private void loadFluid() {
        Fluid type = getTankType();
        SILEXRecipe fluidRecipe = SILEXRecipes.getOutput(type);
        if (SILEXRecipes.isConversionFluid(type) || fluidRecipe != null) {
            String key = SILEXRecipes.currentKey(type);
            if (currentFill == 0) {
                currentKey = key;
            }
            if (key.equals(currentKey)) {
                int toFill = Math.min(50, Math.min(MAX_FILL - currentFill, tank.getFluidAmount()));
                if (toFill > 0) {
                    currentFill += toFill;
                    tank.drain(toFill, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }

        loadDelay++;
        if (loadDelay > 20) {
            loadDelay = 0;
        }
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        if (loadDelay == 0 && !input.isEmpty() && type == ModFluids.PEROXIDE.source.get()
                && (currentKey.isEmpty() || currentKey.equals(SILEXRecipes.currentKey(input)))) {
            SILEXRecipe recipe = SILEXRecipes.getOutput(input);
            if (recipe == null) {
                return;
            }
            int load = recipe.fluidProduced();
            if (load <= MAX_FILL - currentFill && load <= tank.getFluidAmount()) {
                currentFill += load;
                currentKey = SILEXRecipes.currentKey(input);
                tank.drain(load, IFluidHandler.FluidAction.EXECUTE);
                input.shrink(1);
                if (input.isEmpty()) {
                    items.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
                }
            }
        }
    }

    private boolean process() {
        if (currentKey.isEmpty() || currentFill <= 0) {
            return false;
        }
        SILEXRecipe recipe = SILEXRecipes.getOutput(currentKey);
        if (recipe == null) {
            return false;
        }
        if (recipe.laser().ordinal() > getMode().ordinal()) {
            return false;
        }
        if (currentFill < recipe.fluidConsumed()) {
            return false;
        }
        if (!items.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            return false;
        }
        int progressSpeed = (int) Math.pow(2, getMode().ordinal() - recipe.laser().ordinal() + 1) / 2;
        progress += Math.max(1, progressSpeed);
        if (progress >= PROCESS_TIME) {
            currentFill -= recipe.fluidConsumed();
            items.setStackInSlot(SLOT_OUTPUT, SILEXRecipes.pickOutput(recipe, recipeIndex));
            progress = 0;
            recipeIndex += PRIME;
            setChanged();
        }
        return true;
    }

    private void dequeue() {
        ItemStack output = items.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return;
        }
        for (int i = SLOT_QUEUE_0; i <= SLOT_QUEUE_5; i++) {
            ItemStack slot = items.getStackInSlot(i);
            if (!slot.isEmpty() && slot.getCount() < slot.getMaxStackSize()
                    && ItemStack.isSameItemSameTags(output, slot)) {
                slot.grow(1);
                output.shrink(1);
                if (output.isEmpty()) {
                    items.setStackInSlot(SLOT_OUTPUT, ItemStack.EMPTY);
                }
                return;
            }
        }
        for (int i = SLOT_QUEUE_0; i <= SLOT_QUEUE_5; i++) {
            if (items.getStackInSlot(i).isEmpty()) {
                items.setStackInSlot(i, output.copy());
                items.setStackInSlot(SLOT_OUTPUT, ItemStack.EMPTY);
                return;
            }
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.machineSILEX");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new SilexMenu(id, inv, this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 3, 2));
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidOptional.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidOptional.invalidate();
        itemOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        fluidOptional = LazyOptional.of(() -> fluids);
        itemOptional = LazyOptional.of(() -> automation);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        ResourceLocation typeId = ForgeRegistries.FLUIDS.getKey(getTankType());
        if (typeId != null) {
            tag.putString("TankType", typeId.toString());
        }
        tag.putInt("fill", currentFill);
        tag.putInt("recipeIndex", recipeIndex);
        tag.putString("mode", getMode().name());
        tag.putString("current", currentKey == null ? "" : currentKey);
        tag.putInt("Progress", progress);
        tag.putBoolean("HadIdentifier", hadIdentifier);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        tank.readFromNBT(tag.getCompound("Tank"));
        if (tag.contains("TankType")) {
            Fluid loaded = BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(tag.getString("TankType")));
            tankType = loaded;
        }
        currentFill = tag.getInt("fill");
        recipeIndex = tag.getInt("recipeIndex");
        mode = EnumWavelengths.byName(tag.getString("mode"));
        currentKey = tag.getString("current");
        progress = tag.getInt("Progress");
        hadIdentifier = tag.getBoolean("HadIdentifier");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
