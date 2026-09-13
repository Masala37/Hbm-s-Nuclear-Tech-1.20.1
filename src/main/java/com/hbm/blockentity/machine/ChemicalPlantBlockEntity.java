package com.hbm.blockentity.machine;

import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.ChemicalPlantMenu;
import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import com.hbm.inventory.recipes.GenericMachineRecipe;
import com.hbm.inventory.recipes.GenericRecipeMatch;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityMachineChemicalPlant}.
 */
public class ChemicalPlantBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_BLUEPRINT = 1;
    public static final int SLOT_UPGRADE_A = 2;
    public static final int SLOT_UPGRADE_B = 3;
    public static final int SLOT_INPUT_START = 4;
    public static final int SLOT_OUTPUT_START = 7;
    public static final int SLOT_FLUID_IN_START = 10;
    public static final int SLOT_FLUID_IN_EMPTY = 13;
    public static final int SLOT_FLUID_OUT_START = 16;
    public static final int SLOT_FLUID_OUT_EMPTY = 19;
    public static final int SLOT_COUNT = 22;
    public static final int[] INPUT_SLOTS = {4, 5, 6};
    public static final int[] OUTPUT_SLOTS = {7, 8, 9};
    public static final int TANK_CAPACITY = 24_000;
    public static final int POWER_FLOOR = 100_000;
    public static final int POWER_CAP = 1_000_000;
    public static final int TANK_COUNT = 3;

    private final ModEnergyStorage energy = new ModEnergyStorage(POWER_CAP, POWER_CAP, 0, this::setChanged);
    private final FluidTank[] inputTanks = new FluidTank[TANK_COUNT];
    private final FluidTank[] outputTanks = new FluidTank[TANK_COUNT];
    private final IFluidHandler fluids = new IFluidHandler() {
        @Override
        public int getTanks() {
            return TANK_COUNT * 2;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if (tank < TANK_COUNT) {
                return inputTanks[tank].getFluid();
            }
            int out = tank - TANK_COUNT;
            return out >= 0 && out < TANK_COUNT ? outputTanks[out].getFluid() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank < TANK_COUNT;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return 0;
            }
            int remaining = resource.getAmount();
            for (int i = 0; i < TANK_COUNT && remaining > 0; i++) {
                if (!acceptsInput(i, resource)) {
                    continue;
                }
                FluidStack slice = resource.copy();
                slice.setAmount(remaining);
                remaining -= inputTanks[i].fill(slice, action);
            }
            return resource.getAmount() - remaining;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            for (FluidTank tank : outputTanks) {
                FluidStack stored = tank.getFluid();
                if (stored.isEmpty() || stored.getFluid() != resource.getFluid()) {
                    continue;
                }
                return tank.drain(resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            for (FluidTank tank : outputTanks) {
                if (tank.getFluid().isEmpty()) {
                    continue;
                }
                return tank.drain(maxDrain, action);
            }
            return FluidStack.EMPTY;
        }
    };
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ChemicalPlantBlockEntity.this.isItemValid(slot, stack);
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
            if (slot < SLOT_INPUT_START || slot >= SLOT_OUTPUT_START) {
                return stack;
            }
            if (!isItemValid(slot, stack)) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= SLOT_OUTPUT_START && slot < SLOT_FLUID_IN_START) {
                return items.extractItem(slot, amount, simulate);
            }
            if (isSlotClogged(slot)) {
                return items.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ChemicalPlantBlockEntity.this.isItemValid(slot, stack);
        }
    };

    private LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);
    private LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> fluids);

    private String recipeName = "null";
    private double progress;
    private boolean didProcess;
    private boolean frame;
    private int maxPower = POWER_FLOOR;
    public int anim;
    public int prevAnim;

    public ChemicalPlantBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_PLANT.get(), pos, state);
        for (int i = 0; i < TANK_COUNT; i++) {
            inputTanks[i] = new FluidTank(TANK_CAPACITY) {
                @Override
                protected void onContentsChanged() {
                    setChanged();
                }
            };
            outputTanks[i] = new FluidTank(TANK_CAPACITY) {
                @Override
                protected void onContentsChanged() {
                    setChanged();
                }
            };
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public FluidTank[] getInputTanks() {
        return inputTanks;
    }

    public FluidTank[] getOutputTanks() {
        return outputTanks;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public GenericMachineRecipe getRecipe() {
        return ChemicalPlantRecipes.byName(recipeName);
    }

    public double getProgress() {
        return progress;
    }

    public boolean didProcess() {
        return didProcess;
    }

    public boolean hasFrame() {
        return frame;
    }

    public int displayedMaxPower() {
        return Math.max(POWER_FLOOR, maxPower);
    }

    public void setRecipe(String name) {
        this.recipeName = name == null || name.isEmpty() ? "null" : name;
        this.progress = 0.0D;
        syncToClient();
    }

    private void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        if (slot == SLOT_BATTERY) {
            return true;
        }
        if (slot == SLOT_BLUEPRINT) {
            return AssemblyMachineBlockEntity.isBlueprint(stack);
        }
        if (slot == SLOT_UPGRADE_A || slot == SLOT_UPGRADE_B) {
            return AssemblyMachineBlockEntity.isUpgradeItem(stack);
        }
        if (slot >= SLOT_OUTPUT_START && slot < SLOT_FLUID_IN_START) {
            return false;
        }
        if (slot >= SLOT_FLUID_IN_EMPTY && slot < SLOT_FLUID_OUT_START) {
            return false;
        }
        if (slot >= SLOT_FLUID_OUT_EMPTY) {
            return false;
        }
        if (slot >= SLOT_FLUID_IN_START && slot < SLOT_FLUID_IN_EMPTY) {
            return FluidUtil.getFluidHandler(stack).isPresent();
        }
        if (slot >= SLOT_FLUID_OUT_START && slot < SLOT_FLUID_OUT_EMPTY) {
            return FluidUtil.getFluidHandler(stack).isPresent();
        }
        GenericMachineRecipe recipe = getRecipe();
        if (recipe == null) {
            return false;
        }
        for (int i = 0; i < Math.min(recipe.inputItem().size(), INPUT_SLOTS.length); i++) {
            if (INPUT_SLOTS[i] == slot && GenericRecipeMatch.matchesItem(recipe.inputItem().get(i), stack, true)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSlotClogged(int slot) {
        boolean input = false;
        for (int inputSlot : INPUT_SLOTS) {
            if (inputSlot == slot) {
                input = true;
                break;
            }
        }
        if (!input) {
            return false;
        }
        ItemStack stack = items.getStackInSlot(slot);
        return !stack.isEmpty() && !isItemValid(slot, stack);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChemicalPlantBlockEntity be) {
        if (level.isClientSide) {
            be.clientTick();
            return;
        }
        be.serverTick();
    }

    private void serverTick() {
        ItemEnergyHelper.dischargeItemIntoBuffer(items.getStackInSlot(SLOT_BATTERY), energy);
        GenericMachineRecipe recipe = getRecipe();
        if (recipe != null && ChemicalPlantRecipes.isPooled(recipe)
                && !ChemicalPlantRecipes.isPartOfPool(recipe, AssemblyMachineBlockEntity.grabPool(items.getStackInSlot(SLOT_BLUEPRINT)))) {
            didProcess = false;
            progress = 0.0D;
            recipeName = "null";
            syncToClient();
            return;
        }

        maxPower = POWER_FLOOR;
        if (recipe != null) {
            maxPower = (int) Math.min(POWER_CAP, Math.max(POWER_FLOOR, recipe.power() * 100L));
        }
        maxPower = Math.max(maxPower, energy.getEnergyStored());

        for (int i = 0; i < TANK_COUNT; i++) {
            LauncherFluidTransfer.emptyContainer(items, SLOT_FLUID_IN_START + i, SLOT_FLUID_IN_EMPTY + i, inputTanks[i]);
            LauncherFluidTransfer.fillContainer(items, SLOT_FLUID_OUT_START + i, SLOT_FLUID_OUT_EMPTY + i, outputTanks[i]);
        }

        double speed = 1.0D;
        double pow = 1.0D;
        int speedLvl = Math.min(3, upgradeSpeed(items.getStackInSlot(SLOT_UPGRADE_A))
                + upgradeSpeed(items.getStackInSlot(SLOT_UPGRADE_B)));
        int powerLvl = Math.min(3, upgradePower(items.getStackInSlot(SLOT_UPGRADE_A))
                + upgradePower(items.getStackInSlot(SLOT_UPGRADE_B)));
        int overdrive = Math.min(3, upgradeOverdrive(items.getStackInSlot(SLOT_UPGRADE_A))
                + upgradeOverdrive(items.getStackInSlot(SLOT_UPGRADE_B)));
        speed += speedLvl / 3.0D;
        speed += overdrive;
        pow -= powerLvl * 0.25D;
        pow += speedLvl * 1.0D;
        pow += overdrive * 10.0D / 3.0D;

        boolean wasProcessing = didProcess;
        didProcess = false;
        if (recipe != null && canProcess(recipe, speed, pow)) {
            process(recipe, speed, pow);
            didProcess = true;
            treatMeteoriteSword();
        } else {
            progress = 0.0D;
        }
        if (wasProcessing != didProcess) {
            syncToClient();
        }
    }

    private boolean acceptsInput(int tank, FluidStack resource) {
        GenericMachineRecipe recipe = getRecipe();
        if (recipe != null && tank < recipe.inputFluid().size()) {
            var expected = GenericRecipeMatch.fluid(recipe.inputFluid().get(tank).fluid());
            return expected != null && resource.getFluid() == expected;
        }
        if (recipe != null) {
            return false;
        }
        FluidStack stored = inputTanks[tank].getFluid();
        return stored.isEmpty() || stored.getFluid() == resource.getFluid();
    }

    private boolean canProcess(GenericMachineRecipe recipe, double speed, double power) {
        long need = power == 1.0D ? recipe.power() : (long) (recipe.power() * power);
        if (energy.getEnergyStored() < need) {
            return false;
        }
        return GenericRecipeMatch.hasItems(recipe, items, INPUT_SLOTS)
                && GenericRecipeMatch.canFitOutputs(recipe, items, OUTPUT_SLOTS)
                && GenericRecipeMatch.hasFluids(recipe, inputTanks)
                && GenericRecipeMatch.canFitFluidOutputs(recipe, outputTanks);
    }

    private void process(GenericMachineRecipe recipe, double speed, double power) {
        long cost = power == 1.0D ? recipe.power() : (long) (recipe.power() * power);
        energy.consume((int) Math.min(Integer.MAX_VALUE, cost));
        double step = Math.min(speed / Math.max(1, recipe.duration()), 1.0D);
        progress += step;
        if (progress >= 1.0D) {
            GenericRecipeMatch.consumeItems(recipe, items, INPUT_SLOTS);
            GenericRecipeMatch.consumeFluids(recipe, inputTanks);
            GenericRecipeMatch.produceItems(recipe, items, OUTPUT_SLOTS);
            GenericRecipeMatch.produceFluids(recipe, outputTanks);
            if (canProcess(recipe, speed, power)) {
                progress -= 1.0D;
            } else {
                progress = 0.0D;
            }
            setChanged();
        }
    }

    private void treatMeteoriteSword() {
        ItemStack stack = items.getStackInSlot(SLOT_BATTERY);
        if (stack.isEmpty()) {
            return;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !RefStrings.MODID.equals(id.getNamespace()) || !"meteorite_sword_machined".equals(id.getPath())) {
            return;
        }
        Item treated = ForgeRegistries.ITEMS.getValue(new ResourceLocation(RefStrings.MODID, "meteorite_sword_treated"));
        if (treated == null || treated == Items.AIR) {
            return;
        }
        items.setStackInSlot(SLOT_BATTERY, new ItemStack(treated));
    }

    private void clientTick() {
        prevAnim = anim;
        if (didProcess) {
            anim++;
        }
        if (level != null && level.getGameTime() % 20L == 0L) {
            frame = !level.getBlockState(worldPosition.above(3)).isAir();
        }
        com.hbm.HbmNuclearTechMod.proxy.tickChemplant(level, worldPosition, didProcess);
    }

    private static int upgradeSpeed(ItemStack stack) {
        return upgradeLevel(stack, "upgrade_speed_");
    }

    private static int upgradePower(ItemStack stack) {
        return upgradeLevel(stack, "upgrade_power_");
    }

    private static int upgradeOverdrive(ItemStack stack) {
        return upgradeLevel(stack, "upgrade_overdrive_");
    }

    private static int upgradeLevel(ItemStack stack, String prefix) {
        if (stack.isEmpty()) {
            return 0;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !RefStrings.MODID.equals(id.getNamespace()) || !id.getPath().startsWith(prefix)) {
            return 0;
        }
        try {
            return Math.min(3, Integer.parseInt(id.getPath().substring(prefix.length())));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.machineChemicalPlant");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new ChemicalPlantMenu(id, inv, this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 3, 2));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        for (int i = 0; i < TANK_COUNT; i++) {
            tag.put("i" + i, inputTanks[i].writeToNBT(new CompoundTag()));
            tag.put("o" + i, outputTanks[i].writeToNBT(new CompoundTag()));
        }
        tag.putString("recipe", recipeName);
        tag.putDouble("progress", progress);
        tag.putBoolean("didProcess", didProcess);
        tag.putInt("maxPower", maxPower);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        for (int i = 0; i < TANK_COUNT; i++) {
            if (tag.contains("i" + i)) {
                inputTanks[i].readFromNBT(tag.getCompound("i" + i));
            }
            if (tag.contains("o" + i)) {
                outputTanks[i].readFromNBT(tag.getCompound("o" + i));
            }
        }
        recipeName = tag.getString("recipe");
        if (recipeName.isEmpty()) {
            recipeName = "null";
        }
        progress = tag.getDouble("progress");
        didProcess = tag.getBoolean("didProcess");
        maxPower = Math.max(POWER_FLOOR, tag.getInt("maxPower"));
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
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        itemOptional.invalidate();
        fluidOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyOptional = LazyOptional.of(() -> energy);
        itemOptional = LazyOptional.of(() -> automation);
        fluidOptional = LazyOptional.of(() -> fluids);
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
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
