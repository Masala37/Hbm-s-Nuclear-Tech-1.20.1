package com.hbm.blockentity.network;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.blocks.network.PylonKindBlock;
import com.hbm.blocks.network.RedConnectorBlock;
import com.hbm.energy.HeCableNet;
import com.hbm.energy.IEnergyConductor;
import com.hbm.energy.PylonConnectionType;
import com.hbm.energy.PylonDye;
import com.hbm.energy.PylonLinks;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 {@code TileEntityPylonBase}: stored wire links, dye color, HE node.
 */
public class PylonBlockEntity extends BlockEntity implements IEnergyConductor {
    private final List<BlockPos> connected = new ArrayList<>();
    private int color;
    private long lastEnergyNetTick = Long.MIN_VALUE;

    public PylonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PYLON.get(), pos, state);
    }

    public PylonKind kind() {
        if (getBlockState().getBlock() instanceof PylonKindBlock block) {
            return block.pylonKind();
        }
        return PylonKind.SMALL;
    }

    public Direction facing() {
        BlockState state = getBlockState();
        if (state.hasProperty(RedConnectorBlock.FACING)) {
            return state.getValue(RedConnectorBlock.FACING);
        }
        if (state.hasProperty(BlockDummyable.META)) {
            int meta = state.getValue(BlockDummyable.META);
            if (DummyableMeta.isCore(meta)) {
                return Direction.from3DDataValue(DummyableMeta.coreFacing(meta));
            }
        }
        return Direction.SOUTH;
    }

    public PylonConnectionType connectionType() {
        return kind().connectionType();
    }

    public double maxWireLength() {
        return kind().maxWireLength();
    }

    public Vec3[] mountPos() {
        return kind().mounts(facing());
    }

    public Vec3 connectionPoint() {
        return kind().connectionPoint(facing()).add(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
    }

    public int color() {
        return color;
    }

    public List<BlockPos> getConnected() {
        return connected;
    }

    public int canConnectTo(PylonBlockEntity other) {
        if (other == null) {
            return PylonLinks.TYPE;
        }
        boolean same = this == other || worldPosition.equals(other.worldPosition);
        double dist = connectionPoint().distanceTo(other.connectionPoint());
        return PylonLinks.canConnect(connectionType(), other.connectionType(), same, dist,
                maxWireLength(), other.maxWireLength());
    }

    public boolean setColor(ItemStack stack) {
        int next = PylonDye.from(stack);
        if (next == 0 || next == color) {
            return false;
        }
        if (!stack.isEmpty()) {
            stack.shrink(1);
        }
        color = next;
        sync();
        return true;
    }

    public void addConnection(BlockPos pos) {
        BlockPos at = pos.immutable();
        if (at.equals(worldPosition) || connected.contains(at)) {
            return;
        }
        connected.add(at);
        sync();
    }

    public void disconnectAll() {
        if (level == null) {
            connected.clear();
            return;
        }
        for (BlockPos pos : new ArrayList<>(connected)) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te == this || !(te instanceof PylonBlockEntity pylon)) {
                continue;
            }
            pylon.connected.removeIf(worldPosition::equals);
            pylon.sync();
        }
        connected.clear();
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean isEnergyConductor() {
        return true;
    }

    @Override
    public long lastEnergyNetTick() {
        return lastEnergyNetTick;
    }

    @Override
    public void markEnergyNetTick(long gameTime) {
        lastEnergyNetTick = gameTime;
    }

    @Override
    public boolean connectsEnergy(Direction dir) {
        return kind().connectsEnergy(facing(), dir);
    }

    @Override
    public List<BlockPos> extraEnergyLinks() {
        List<BlockPos> links = new ArrayList<>(connected);
        if (kind() == PylonKind.SUBSTATION) {
            links.add(worldPosition.offset(1, 0, 1));
            links.add(worldPosition.offset(1, 0, -1));
            links.add(worldPosition.offset(-1, 0, 1));
            links.add(worldPosition.offset(-1, 0, -1));
        }
        return links;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PylonBlockEntity be) {
        be.pruneDeadLinks();
        HeCableNet.tick(level, pos);
    }

    /**
     * Drop stored wires when the other pylon is gone in a loaded chunk.
     * Unloaded chunks stay linked (1.7 also kept NBT across unload).
     */
    private void pruneDeadLinks() {
        if (level == null || level.isClientSide) {
            return;
        }
        boolean changed = connected.removeIf(pos ->
                level.isLoaded(pos) && !(level.getBlockEntity(pos) instanceof PylonBlockEntity));
        if (changed) {
            sync();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("conCount", connected.size());
        tag.putInt("color", color);
        for (int i = 0; i < connected.size(); i++) {
            BlockPos pos = connected.get(i);
            tag.putIntArray("con" + i, new int[]{pos.getX(), pos.getY(), pos.getZ()});
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        color = tag.getInt("color");
        connected.clear();
        int count = tag.getInt("conCount");
        for (int i = 0; i < count; i++) {
            int[] raw = tag.getIntArray("con" + i);
            if (raw.length >= 3) {
                connected.add(new BlockPos(raw[0], raw[1], raw[2]));
            }
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

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(Math.max(32.0D, kind().maxWireLength()));
    }
}
