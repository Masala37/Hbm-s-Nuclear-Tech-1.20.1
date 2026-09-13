package com.hbm.blockentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.blocks.machine.MachineFelBlock;
import com.hbm.blocks.machine.MachineSilexBlock;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.FelMenu;
import com.hbm.items.machine.ItemFELCrystal;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.registry.ModBlockEntities;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.7.10 {@code TileEntityFEL}. OpenComputers skipped. Looped FEL audio skipped.
 */
public class FelBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_CRYSTAL = 1;
    public static final int SLOT_COUNT = 2;

    public static final int MAX_POWER = 20_000_000;
    public static final int POWER_REQ = 1250;
    public static final int RANGE = 24;

    private final ModEnergyStorage energy = new ModEnergyStorage(MAX_POWER, MAX_POWER, 0, this::onChanged);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return FelBlockEntity.this.mayPlace(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_CRYSTAL ? 1 : super.getSlotLimit(slot);
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
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
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

    private LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);

    private EnumWavelengths mode = EnumWavelengths.NULL;
    private boolean isOn;
    private boolean missingValidSilex = true;
    private int distance;

    public FelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_FEL.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public EnumWavelengths getMode() {
        return mode == null ? EnumWavelengths.NULL : mode;
    }

    public boolean isOn() {
        return isOn;
    }

    public boolean missingValidSilex() {
        return missingValidSilex;
    }

    public int getDistance() {
        return distance;
    }

    public void toggle() {
        isOn = !isOn;
        onChanged();
    }

    public boolean mayPlace(int slot, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case SLOT_BATTERY -> ItemEnergyHelper.isEnergyItem(stack);
            case SLOT_CRYSTAL -> stack.getItem() instanceof ItemFELCrystal;
            default -> false;
        };
    }

    public int powerReq() {
        int ordinal = getMode().ordinal();
        if (ordinal == 0) {
            return 0;
        }
        return (int) (POWER_REQ * Math.pow(3, ordinal));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FelBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        be.serverTick();
    }

    private void serverTick() {
        int facing = DummyableMeta.coreFacing(getBlockState().getValue(BlockDummyable.META));
        BlockPos extra = MachineFelBlock.extraPos(worldPosition, facing);
        EnergyNetworkHelper.pullFromNeighbors(level, extra, energy, MAX_POWER);
        EnergyNetworkHelper.pullFromNeighbors(level, worldPosition, energy, MAX_POWER);
        ItemEnergyHelper.dischargeItemIntoBuffer(items.getStackInSlot(SLOT_BATTERY), energy);

        if (isOn && items.getStackInSlot(SLOT_CRYSTAL).getItem() instanceof ItemFELCrystal crystal) {
            mode = crystal.wavelength();
        } else {
            mode = EnumWavelengths.NULL;
        }

        int req = powerReq();
        if (isOn && mode != EnumWavelengths.NULL && energy.getEnergyStored() < req) {
            energy.setEnergy(0);
        }

        if (isOn && energy.getEnergyStored() >= req && mode != EnumWavelengths.NULL) {
            fireBeam(facing, req);
        }

        onChanged();
    }

    private void fireBeam(int facing, int req) {
        int dx = DummyableMeta.offsetX(facing);
        int dz = DummyableMeta.offsetZ(facing);
        int dist = Math.max(0, distance - 1);
        double blx = Math.min(worldPosition.getX(), worldPosition.getX() + dx * dist) + 0.2;
        double bux = Math.max(worldPosition.getX(), worldPosition.getX() + dx * dist) + 0.8;
        double bly = worldPosition.getY() + 1.2;
        double buy = worldPosition.getY() + 1.8;
        double blz = Math.min(worldPosition.getZ(), worldPosition.getZ() + dz * dist) + 0.2;
        double buz = Math.max(worldPosition.getZ(), worldPosition.getZ() + dz * dist) + 0.8;
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, new AABB(blx, bly, blz, bux, buy, buz));
        for (LivingEntity entity : list) {
            switch (mode) {
                case VISIBLE -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60 * 60 * 65536, 0));
                    entity.setSecondsOnFire(10);
                }
                case IR, UV -> entity.setSecondsOnFire(10);
                case GAMMA -> ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, 25);
                case DRX -> ContaminationUtil.applyDigammaData(entity, 0.1F);
                default -> {
                }
            }
        }

        energy.consume(req);
        boolean silexSpacing = false;
        for (int i = 3; i < RANGE; i++) {
            BlockPos at = worldPosition.offset(dx * i, 1, dz * i);
            BlockState hit = level.getBlockState(at);
            Block block = hit.getBlock();
            // Dummyables use noOcclusion(); SILEX must be handled before the air skip.
            if (block instanceof MachineSilexBlock silexBlock) {
                BlockPos core = silexBlock.findCore(level, at);
                BlockEntity te = core == null ? null : level.getBlockEntity(core);
                if (te instanceof SilexBlockEntity silex) {
                    int silexFacing = DummyableMeta.coreFacing(silex.getBlockState().getValue(BlockDummyable.META));
                    if (rotationIsValid(silexFacing, facing) && i >= 5 && !silexSpacing) {
                        if (silex.getMode() != mode) {
                            silex.setMode(mode);
                            missingValidSilex = false;
                            silexSpacing = true;
                        }
                    } else {
                        level.destroyBlock(silex.getBlockPos(), true);
                    }
                }
                continue;
            }
            if (!hit.canOcclude() && !hit.is(Blocks.TNT)) {
                distance = RANGE;
                silexSpacing = false;
                continue;
            }
            if (hit.canOcclude() || hit.is(Blocks.TNT)) {
                distance = i;
                if (hit.liquid()) {
                    level.playSound(null, at, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.removeBlock(at, false);
                    break;
                }
                float hardness = block.getExplosionResistance();
                if (hardness < 75 && level.random.nextInt(5) == 0) {
                    level.playSound(null, at, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                    Block fire = mode == EnumWavelengths.DRX ? digammaFire() : Blocks.FIRE;
                    level.setBlock(at, fire.defaultBlockState(), Block.UPDATE_ALL);
                    if (mode == EnumWavelengths.DRX) {
                        Block ash = BuiltInRegistries.BLOCK.get(new ResourceLocation("hbm", "ash_digamma"));
                        if (ash != Blocks.AIR) {
                            level.setBlock(at.below(), ash.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
                break;
            }
        }
    }

    private static Block digammaFire() {
        Block fire = BuiltInRegistries.BLOCK.get(new ResourceLocation("hbm", "fire_digamma"));
        return fire == Blocks.AIR ? Blocks.FIRE : fire;
    }

    public static boolean rotationIsValid(int silexFacing, int felFacing) {
        return silexFacing == felFacing || silexFacing == DummyableMeta.opposite(felFacing);
    }

    public long getPowerScaled(long i) {
        return (energy.getEnergyStored() * i) / MAX_POWER;
    }

    /** 1.7 TESR / GUI beam: {@code power > powerReq * 2^ordinal}, not the 3^ordinal consume cost. */
    public boolean beamVisible() {
        if (!isOn || getMode() == EnumWavelengths.NULL || distance <= 0) {
            return false;
        }
        return energy.getEnergyStored() > POWER_REQ * Math.pow(2, getMode().ordinal());
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.machineFEL");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new FelMenu(id, inv, this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-24, 0, -24), worldPosition.offset(25, 4, 25));
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        itemOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyOptional = LazyOptional.of(() -> energy);
        itemOptional = LazyOptional.of(() -> automation);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        tag.putString("mode", getMode().name());
        tag.putBoolean("isOn", isOn);
        tag.putBoolean("valid", missingValidSilex);
        tag.putInt("distance", distance);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        mode = EnumWavelengths.byName(tag.getString("mode"));
        isOn = tag.getBoolean("isOn");
        missingValidSilex = tag.getBoolean("valid");
        distance = tag.getInt("distance");
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
