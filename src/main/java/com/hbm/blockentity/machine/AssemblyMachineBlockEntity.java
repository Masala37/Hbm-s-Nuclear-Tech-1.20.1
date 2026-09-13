package com.hbm.blockentity.machine;

import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.AssemblyMachineMenu;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.GenericMachineRecipe;
import com.hbm.inventory.recipes.GenericRecipeMatch;
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
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * 1.7.10 {@code TileEntityMachineAssemblyMachine}.
 */
public class AssemblyMachineBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_BLUEPRINT = 1;
    public static final int SLOT_UPGRADE_A = 2;
    public static final int SLOT_UPGRADE_B = 3;
    public static final int SLOT_INPUT_START = 4;
    public static final int SLOT_OUTPUT = 16;
    public static final int SLOT_COUNT = 17;
    public static final int[] INPUT_SLOTS = {4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    public static final int TANK_CAPACITY = 4_000;
    public static final int POWER_FLOOR = 100_000;
    public static final int POWER_CAP = 1_000_000;

    private final ModEnergyStorage energy = new ModEnergyStorage(POWER_CAP, POWER_CAP, 0, this::setChanged);
    private final FluidTank inputTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final FluidTank outputTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final IFluidHandler fluids = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return tank == 0 ? inputTank.getFluid() : outputTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank == 0;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return inputTank.fill(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return outputTank.drain(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return outputTank.drain(maxDrain, action);
        }
    };
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return AssemblyMachineBlockEntity.this.isItemValid(slot, stack);
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
            if (slot < SLOT_INPUT_START || slot >= SLOT_OUTPUT) {
                return stack;
            }
            if (!isItemValid(slot, stack)) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != SLOT_OUTPUT && !isSlotClogged(slot)) {
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
            return AssemblyMachineBlockEntity.this.isItemValid(slot, stack);
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

    public final AssemblerArm[] arms = {new AssemblerArm(), new AssemblerArm()};
    public double prevRing;
    public double ring;
    public double ringSpeed;
    public double ringTarget;
    public int ringDelay;
    private boolean clientWasProcess;

    public AssemblyMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASSEMBLY_MACHINE.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public FluidTank getInputTank() {
        return inputTank;
    }

    public FluidTank getOutputTank() {
        return outputTank;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public GenericMachineRecipe getRecipe() {
        return AssemblyMachineRecipes.byName(recipeName);
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
            return isBlueprint(stack);
        }
        if (slot == SLOT_UPGRADE_A || slot == SLOT_UPGRADE_B) {
            return isUpgradeItem(stack);
        }
        if (slot == SLOT_OUTPUT) {
            return false;
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

    public static void tick(Level level, BlockPos pos, BlockState state, AssemblyMachineBlockEntity be) {
        if (level.isClientSide) {
            be.clientTick();
            return;
        }
        be.serverTick();
    }

    private void serverTick() {
        ItemEnergyHelper.dischargeItemIntoBuffer(items.getStackInSlot(SLOT_BATTERY), energy);
        GenericMachineRecipe recipe = getRecipe();
        if (recipe != null && AssemblyMachineRecipes.isPooled(recipe)
                && !AssemblyMachineRecipes.isPartOfPool(recipe, grabPool(items.getStackInSlot(SLOT_BLUEPRINT)))) {
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
        } else {
            progress = 0.0D;
        }
        if (wasProcessing != didProcess) {
            syncToClient();
        }
    }

    private boolean canProcess(GenericMachineRecipe recipe, double speed, double power) {
        long need = power == 1.0D ? recipe.power() : (long) (recipe.power() * power);
        if (energy.getEnergyStored() < need) {
            return false;
        }
        return GenericRecipeMatch.hasItems(recipe, items, INPUT_SLOTS)
                && GenericRecipeMatch.canFitOutput(recipe, items, SLOT_OUTPUT)
                && GenericRecipeMatch.hasFluids(recipe, inputTank)
                && GenericRecipeMatch.canFitFluidOutput(recipe, outputTank);
    }

    private void process(GenericMachineRecipe recipe, double speed, double power) {
        long cost = power == 1.0D ? recipe.power() : (long) (recipe.power() * power);
        energy.consume((int) Math.min(Integer.MAX_VALUE, cost));
        double step = Math.min(speed / Math.max(1, recipe.duration()), 1.0D);
        progress += step;
        if (progress >= 1.0D) {
            GenericRecipeMatch.consumeItems(recipe, items, INPUT_SLOTS);
            GenericRecipeMatch.consumeFluid(recipe, inputTank);
            GenericRecipeMatch.produceItem(recipe, items, SLOT_OUTPUT);
            GenericRecipeMatch.produceFluid(recipe, outputTank);
            if (canProcess(recipe, speed, power)) {
                progress -= 1.0D;
            } else {
                progress = 0.0D;
            }
            setChanged();
        }
    }

    private void clientTick() {
        if (clientWasProcess && !didProcess && level != null) {
            level.playLocalSound(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D, ModSounds.require("block.assembler_stop"),
                    SoundSource.BLOCKS, 0.25F, 1.5F, false);
        }
        clientWasProcess = didProcess;
        if (level != null && level.getGameTime() % 20L == 0L) {
            frame = !level.getBlockState(worldPosition.above(3)).isAir();
        }
        for (AssemblerArm arm : arms) {
            arm.updateInterp();
            if (didProcess) {
                arm.updateArm();
            } else {
                arm.returnToNullPos();
            }
            if (arm.prevAngles[3] != arm.angles[3] && arm.angles[3] == -0.75D && level != null) {
                level.playLocalSound(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D, ModSounds.require("block.assembler_strike"),
                        SoundSource.BLOCKS, 0.5F, 1.0F, false);
            }
        }
        prevRing = ring;
        if (!didProcess) {
            return;
        }
        if (ring != ringTarget) {
            double delta = Math.abs(ringTarget - ring);
            if (delta <= ringSpeed) {
                ring = ringTarget;
            } else if (ringTarget > ring) {
                ring += ringSpeed;
            } else {
                ring -= ringSpeed;
            }
            if (ringTarget == ring) {
                double sub = ringTarget >= 360.0D ? -360.0D : 360.0D;
                ringTarget += sub;
                ring += sub;
                prevRing += sub;
                ringDelay = 20 + (level != null ? level.random.nextInt(21) : 20);
            }
        } else if (ringDelay > 0) {
            ringDelay--;
        } else {
            ringTarget += (Math.random() * 2.0D - 1.0D) * 135.0D;
            ringSpeed = 10.0D + Math.random() * 5.0D;
            if (level != null) {
                level.playLocalSound(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D, ModSounds.require("block.assembler_start"),
                        SoundSource.BLOCKS, 0.25F, 1.25F, false);
            }
        }
    }

    public static String grabPool(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !"blueprints".equals(id.getPath())) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("pool")) {
            return null;
        }
        return tag.getString("pool");
    }

    public static boolean isBlueprint(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && RefStrings.MODID.equals(id.getNamespace()) && id.getPath().startsWith("blueprints");
    }

    public static boolean isUpgradeItem(ItemStack stack) {
        return upgradeSpeed(stack) > 0 || upgradePower(stack) > 0 || upgradeOverdrive(stack) > 0;
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
        return Component.translatable("container.machineAssemblyMachine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new AssemblyMachineMenu(id, inv, this);
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
        tag.put("InputTank", inputTank.writeToNBT(new CompoundTag()));
        tag.put("OutputTank", outputTank.writeToNBT(new CompoundTag()));
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
        if (tag.contains("InputTank")) {
            inputTank.readFromNBT(tag.getCompound("InputTank"));
        }
        if (tag.contains("OutputTank")) {
            outputTank.readFromNBT(tag.getCompound("OutputTank"));
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

    public static final class AssemblerArm {
        public final double[] angles = new double[4];
        public final double[] prevAngles = new double[4];
        public final double[] targetAngles = new double[4];
        public final double[] speed = new double[4];
        private final Random rand = new Random();
        private ArmState state = ArmState.ASSUME_POSITION;
        private int actionDelay;
        private static final double[][] POS = {
                {45, -15, -5},
                {15, 15, -15},
                {25, 10, -15},
                {30, 0, -10},
                {70, -10, -25}
        };

        public AssemblerArm() {
            resetSpeed();
        }

        public void updateInterp() {
            System.arraycopy(angles, 0, prevAngles, 0, angles.length);
        }

        public void returnToNullPos() {
            for (int i = 0; i < 4; i++) {
                targetAngles[i] = 0;
            }
            speed[0] = 3;
            speed[1] = 3;
            speed[2] = 3;
            speed[3] = 0.25;
            state = ArmState.RETRACT_STRIKER;
            move();
        }

        public void updateArm() {
            resetSpeed();
            if (actionDelay > 0) {
                actionDelay--;
                return;
            }
            switch (state) {
                case ASSUME_POSITION -> {
                    if (move()) {
                        actionDelay = 2;
                        state = ArmState.EXTEND_STRIKER;
                        targetAngles[3] = -0.75D;
                    }
                }
                case EXTEND_STRIKER -> {
                    if (move()) {
                        state = ArmState.RETRACT_STRIKER;
                        targetAngles[3] = 0.0D;
                    }
                }
                case RETRACT_STRIKER -> {
                    if (move()) {
                        actionDelay = 2 + rand.nextInt(5);
                        int chosen = rand.nextInt(POS.length);
                        targetAngles[0] = POS[chosen][0];
                        targetAngles[1] = POS[chosen][1];
                        targetAngles[2] = POS[chosen][2];
                        state = ArmState.ASSUME_POSITION;
                    }
                }
            }
        }

        public double[] getPositions(float interp) {
            return new double[]{
                    Mth.lerp(interp, prevAngles[0], angles[0]),
                    Mth.lerp(interp, prevAngles[1], angles[1]),
                    Mth.lerp(interp, prevAngles[2], angles[2]),
                    Mth.lerp(interp, prevAngles[3], angles[3])
            };
        }

        private void resetSpeed() {
            speed[0] = 15;
            speed[1] = 15;
            speed[2] = 15;
            speed[3] = 0.5;
        }

        private boolean move() {
            boolean didMove = false;
            for (int i = 0; i < angles.length; i++) {
                if (angles[i] == targetAngles[i]) {
                    continue;
                }
                didMove = true;
                double delta = Math.abs(angles[i] - targetAngles[i]);
                if (delta <= speed[i]) {
                    angles[i] = targetAngles[i];
                    continue;
                }
                if (angles[i] < targetAngles[i]) {
                    angles[i] += speed[i];
                } else {
                    angles[i] -= speed[i];
                }
            }
            return !didMove;
        }

        private enum ArmState {
            ASSUME_POSITION, EXTEND_STRIKER, RETRACT_STRIKER
        }
    }
}
