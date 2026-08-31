package com.hbm.blockentity.machine;

import api.hbm.entity.IRadarDetectableNT.RadarScanParams;
import api.hbm.entity.RadarEntry;
import com.hbm.blocks.machine.RadarCores;
import com.hbm.capability.HbmLivingProps;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.handler.RadarRules;
import com.hbm.handler.RadarScanSystem;
import com.hbm.inventory.menu.HbmMenuHelper;
import com.hbm.inventory.menu.RadarNTMenu;
import com.hbm.items.tool.RadarLinkerItem;
import com.hbm.network.ModMessages;
import com.hbm.network.RadarNTSyncPacket;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModSounds;
import com.hbm.tileentity.IRadarCommandReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RadarNTBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_LINKER = 8;
    public static final int SLOT_BATTERY = 9;
    public static final int ENERGY_TRANSFER = 5_000;

    public boolean scanMissiles = true;
    public boolean scanShells = true;
    public boolean scanPlayers = true;
    public boolean smartMode = true;
    public boolean redMode = true;
    public boolean showMap = false;
    public boolean jammed = false;
    public boolean clearFlag = false;

    public float prevRotation;
    public float rotation;

    public byte[] map = new byte[RadarRules.MAP_SIZE];
    public final List<RadarEntry> entries = new ArrayList<>();

    private int pingTimer;
    private int lastPower;
    private AABB renderBox;

    private final ItemStackHandler items = new ItemStackHandler(10) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final ModEnergyStorage energy = new ModEnergyStorage(
            RadarRules.MAX_POWER, ENERGY_TRANSFER, 0, this::setChanged);

    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);

    public RadarNTBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.RADAR.get(), pos, state);
    }

    public RadarNTBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
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

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public int getRange() {
        return RadarRules.RANGE;
    }

    public int getRedPower() {
        if (entries.isEmpty()) {
            return 0;
        }
        if (redMode) {
            int power = 0;
            for (RadarEntry entry : entries) {
                if (!entry.redstone) {
                    continue;
                }
                power = RadarRules.combineProximity(power,
                        RadarRules.proximityPower(worldPosition.getX(), worldPosition.getZ(), getRange(),
                                entry.posX, entry.posZ));
            }
            return power;
        }
        int power = 0;
        for (RadarEntry entry : entries) {
            if (!entry.redstone) {
                continue;
            }
            power = RadarRules.combineTier(power, RadarRules.tierPower(entry.blipLevel));
        }
        return power;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadarNTBlockEntity be) {
        if (be.map == null || be.map.length != RadarRules.MAP_SIZE) {
            be.map = new byte[RadarRules.MAP_SIZE];
        }
        ItemEnergyHelper.chargeFromItem(be.items.getStackInSlot(SLOT_BATTERY), be.energy, ENERGY_TRANSFER);
        be.pullEnergy(level);
        be.jammed = false;
        be.allocateTargets();

        int red = be.getRedPower();
        if (be.lastPower != red) {
            be.setChanged();
            be.notifyRedstone(level);
        }
        be.lastPower = red;

        be.pingTimer++;
        if (be.energy.getEnergyStored() > 0 && be.pingTimer >= RadarRules.PING_INTERVAL) {
            level.playSound(null, pos, ModSounds.SONAR_PING.get(), SoundSource.BLOCKS, 5.0F, 1.0F);
            be.pingTimer = 0;
        }

        if (be.showMap) {
            be.scanMap((ServerLevel) level);
        }

        ItemStack linker = be.items.getStackInSlot(SLOT_LINKER);
        if (linker.getItem() instanceof RadarLinkerItem && RadarLinkerItem.hasTarget(linker)) {
            BlockPos screenPos = RadarLinkerItem.getTarget(linker);
            BlockEntity tile = RadarCores.core(level, screenPos);
            if (tile instanceof RadarScreenBlockEntity screen) {
                screen.receiveFromRadar(be);
            }
        }

        be.sendSync(50.0D);
        if (be.clearFlag) {
            be.map = new byte[RadarRules.MAP_SIZE];
            be.clearFlag = false;
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, RadarNTBlockEntity be) {
        be.prevRotation = be.rotation;
        if (be.energy.getEnergyStored() > 0) {
            be.rotation += 5.0F;
        }
        if (be.rotation >= 360.0F) {
            be.rotation -= 360.0F;
            be.prevRotation -= 360.0F;
        }
    }

    protected void pullEnergy(Level level) {
        BlockPos pos = worldPosition;
        EnergyNetworkHelper.pullFrom(level, pos.offset(1, 0, 0), Direction.WEST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-1, 0, 0), Direction.EAST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(0, 0, 1), Direction.NORTH, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(0, 0, -1), Direction.SOUTH, energy, ENERGY_TRANSFER);
    }

    protected void notifyRedstone(Level level) {
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            level.updateNeighborsAt(worldPosition.relative(dir), getBlockState().getBlock());
        }
    }

    protected void allocateTargets() {
        entries.clear();
        if (!RadarRules.altitudeOk(worldPosition.getY())) {
            return;
        }
        if (energy.getEnergyStored() < RadarRules.CONSUMPTION) {
            energy.setEnergy(0);
            return;
        }
        energy.consume(RadarRules.CONSUMPTION);

        int scan = getRange();
        RadarScanParams params = new RadarScanParams(scanMissiles, scanShells, scanPlayers, smartMode);
        int rx = worldPosition.getX();
        int ry = worldPosition.getY();
        int rz = worldPosition.getZ();

        for (Entity entity : RadarScanSystem.MATCHING) {
            if (entity.level() != level) {
                continue;
            }
            if (!RadarRules.inScanVolume(rx, ry, rz, scan, entity.getX(), entity.getY(), entity.getZ())) {
                continue;
            }
            if (entity instanceof LivingEntity living && HbmLivingProps.getDigamma(living) > 0.001F) {
                jammed = true;
                entries.clear();
                return;
            }
            RadarEntry entry = RadarScanSystem.convert(entity, this, params);
            if (entry != null) {
                entries.add(entry);
            }
        }
    }

    private void scanMap(ServerLevel level) {
        int chunkLoads = 0;
        long time = level.getGameTime();
        int range = getRange();
        for (int i = 0; i < 100; i++) {
            int index = (int) (time % 400) * 100 + i;
            int iX = (index % 200) * range * 2 / 200;
            int iZ = index / 200 * range * 2 / 200;
            int x = worldPosition.getX() - range + iX;
            int z = worldPosition.getZ() - range + iZ;
            int cx = x >> 4;
            int cz = z >> 4;
            if (level.hasChunk(cx, cz)) {
                map[index] = (byte) Mth.clamp(level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), 50, 128);
            } else if (map[index] == 0 && chunkLoads < RadarRules.CHUNK_LOAD_CAP) {
                level.getChunk(cx, cz, ChunkStatus.FULL, false);
                if (level.hasChunk(cx, cz)) {
                    map[index] = (byte) Mth.clamp(level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), 50, 128);
                    chunkLoads++;
                }
            }
        }
    }

    protected void sendSync(double range) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        boolean sendMap = showMap && !clearFlag;
        int mapIndex = (int) (server.getGameTime() % 400);
        byte[] slice = new byte[0];
        if (sendMap) {
            slice = new byte[100];
            System.arraycopy(map, mapIndex * 100, slice, 0, 100);
        }
        ModMessages.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D,
                        range, server.dimension())),
                new RadarNTSyncPacket(worldPosition, energy.getEnergyStored(), scanMissiles, scanShells, scanPlayers,
                        smartMode, redMode, showMap, jammed, new ArrayList<>(entries), clearFlag, sendMap, mapIndex, slice));
    }

    public void applySync(int power, boolean scanMissiles, boolean scanShells, boolean scanPlayers,
                          boolean smartMode, boolean redMode, boolean showMap, boolean jammed,
                          List<RadarEntry> entries, boolean clearMap, boolean mapSlice, int mapIndex, byte[] mapBytes) {
        energy.setEnergy(power);
        this.scanMissiles = scanMissiles;
        this.scanShells = scanShells;
        this.scanPlayers = scanPlayers;
        this.smartMode = smartMode;
        this.redMode = redMode;
        this.showMap = showMap;
        this.jammed = jammed;
        this.entries.clear();
        this.entries.addAll(entries);
        if (this.map == null || this.map.length != RadarRules.MAP_SIZE) {
            this.map = new byte[RadarRules.MAP_SIZE];
        }
        if (clearMap) {
            this.map = new byte[RadarRules.MAP_SIZE];
        } else if (mapSlice && mapBytes != null && mapIndex >= 0 && mapIndex < 400) {
            System.arraycopy(mapBytes, 0, this.map, mapIndex * 100, Math.min(100, mapBytes.length));
        }
    }

    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (data.contains("missiles")) {
            scanMissiles = !scanMissiles;
        }
        if (data.contains("shells")) {
            scanShells = !scanShells;
        }
        if (data.contains("players")) {
            scanPlayers = !scanPlayers;
        }
        if (data.contains("smart")) {
            smartMode = !smartMode;
        }
        if (data.contains("red")) {
            redMode = !redMode;
        }
        if (data.contains("map")) {
            showMap = !showMap;
        }
        if (data.contains("clear")) {
            clearFlag = true;
        }
        if (data.contains("gui1")) {
            HbmMenuHelper.open(player, this, worldPosition);
        }
        if (data.contains("link")) {
            int id = data.getInt("link");
            if (id < 0 || id > 7) {
                return;
            }
            ItemStack link = items.getStackInSlot(id);
            if (!(link.getItem() instanceof RadarLinkerItem) || !RadarLinkerItem.hasTarget(link)) {
                return;
            }
            BlockPos targetPos = RadarLinkerItem.getTarget(link);
            BlockEntity tile = RadarCores.core(level, targetPos);
            if (!(tile instanceof IRadarCommandReceiver rec)) {
                return;
            }
            boolean ok = false;
            if (data.contains("launchEntity")) {
                Entity entity = level.getEntity(data.getInt("launchEntity"));
                if (entity != null) {
                    ok = rec.sendCommandEntity(entity);
                }
            } else if (data.contains("launchPosX")) {
                ok = rec.sendCommandPosition(data.getInt("launchPosX"), worldPosition.getY(), data.getInt("launchPosZ"));
            }
            if (ok) {
                level.playSound(null, player.blockPosition(), ModSounds.TECH_BLEEP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        setChanged();
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (renderBox == null) {
            renderBox = new AABB(
                    worldPosition.getX() - 1.0D, worldPosition.getY(), worldPosition.getZ() - 1.0D,
                    worldPosition.getX() + 2.0D, worldPosition.getY() + 3.0D, worldPosition.getZ() + 2.0D);
        }
        return renderBox;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.radar");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new RadarNTMenu(id, inv, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        tag.putBoolean("scanMissiles", scanMissiles);
        tag.putBoolean("scanShells", scanShells);
        tag.putBoolean("scanPlayers", scanPlayers);
        tag.putBoolean("smartMode", smartMode);
        tag.putBoolean("redMode", redMode);
        tag.putBoolean("showMap", showMap);
        tag.putByteArray("map", map);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        scanMissiles = tag.getBoolean("scanMissiles");
        scanShells = tag.getBoolean("scanShells");
        scanPlayers = tag.getBoolean("scanPlayers");
        smartMode = !tag.contains("smartMode") || tag.getBoolean("smartMode");
        redMode = !tag.contains("redMode") || tag.getBoolean("redMode");
        showMap = tag.getBoolean("showMap");
        if (tag.contains("map")) {
            map = tag.getByteArray("map");
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY && (side == null || side.getAxis().isHorizontal())) {
            return energyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }
}
