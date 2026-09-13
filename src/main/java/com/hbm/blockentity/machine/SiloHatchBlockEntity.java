package com.hbm.blockentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.blocks.generic.SiloHatchBlock;
import com.hbm.blocks.generic.SiloHatchLogic;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;

/**
 * 1.7 {@code TileEntityDoorGeneric} for silo hatches: open ticks, extras, redstone, sounds.
 */
public class SiloHatchBlockEntity extends BlockEntity {
    private byte state = SiloHatchLogic.STATE_CLOSED;
    private int openTicks;
    private int redstonePower;
    private final Set<BlockPos> activatedBlocks = new HashSet<>();
    public long animStartTime;
    private byte clientState = -1;

    public SiloHatchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SILO_HATCH.get(), pos, state);
    }

    public boolean large() {
        return getBlockState().getBlock() instanceof SiloHatchBlock hatch && hatch.large();
    }

    public byte doorState() {
        return state;
    }

    public float renderOpenTicks() {
        if (level != null && level.isClientSide) {
            return SiloHatchLogic.clientOpenTicks(state, animStartTime, System.currentTimeMillis());
        }
        return openTicks;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SiloHatchBlockEntity be) {
        if (level.isClientSide) {
            be.clientTick();
        } else {
            be.serverTick();
        }
    }

    private void serverTick() {
        if (!DummyableMeta.isCore(getBlockState().getValue(BlockDummyable.META))) {
            return;
        }
        if (state == SiloHatchLogic.STATE_OPENING) {
            openTicks++;
            if (openTicks >= SiloHatchLogic.TIME_TO_OPEN) {
                openTicks = SiloHatchLogic.TIME_TO_OPEN;
            }
            applyRanges(true);
            if (openTicks == SiloHatchLogic.TIME_TO_OPEN) {
                state = SiloHatchLogic.STATE_OPEN;
                sync();
            }
        } else if (state == SiloHatchLogic.STATE_CLOSING) {
            openTicks--;
            if (openTicks <= 0) {
                openTicks = 0;
            }
            applyRanges(false);
            if (openTicks == 0) {
                state = SiloHatchLogic.STATE_CLOSED;
                sync();
            }
        }

        if (redstonePower == -1 && state == SiloHatchLogic.STATE_OPEN) {
            tryToggle(null, true);
        } else if (redstonePower > 0 && state == SiloHatchLogic.STATE_CLOSED) {
            tryToggle(null, true);
        }
        if (redstonePower == -1) {
            redstonePower = 0;
        }
    }

    private void clientTick() {
        com.hbm.HbmNuclearTechMod.proxy.tickSiloHatch(level, worldPosition, state, SiloHatchLogic.SOUND_VOLUME);
    }

    private void applyRanges(boolean opening) {
        if (!(getBlockState().getBlock() instanceof BlockDummyable dummyable)) {
            return;
        }
        int facing = DummyableMeta.coreFacing(getBlockState().getValue(BlockDummyable.META));
        int[][] ranges = SiloHatchLogic.ranges(large());
        for (int i = 0; i < ranges.length; i++) {
            int[] range = ranges[i];
            float time = SiloHatchLogic.rangeOpenTime(openTicks);
            int span = Math.abs(range[3]);
            if (opening) {
                for (int j = 0; j < span; j++) {
                    if (span > 1 && (float) j / (Math.abs(range[3] - 1)) > time) {
                        break;
                    }
                    stampRange(dummyable, facing, range, j, true);
                }
            } else {
                for (int j = span - 1; j >= 0; j--) {
                    if (span > 1 && (float) j / (Math.abs(range[3] - 1)) < time) {
                        break;
                    }
                    stampRange(dummyable, facing, range, j, false);
                }
            }
        }
    }

    private void stampRange(BlockDummyable dummyable, int facing, int[] range, int j, boolean extra) {
        for (int k = 0; k < range[4]; k++) {
            BlockPos offset = SiloHatchLogic.rangeCell(facing, range, j, k);
            BlockPos at = worldPosition.offset(offset);
            if (at.equals(worldPosition)) {
                continue;
            }
            if (extra) {
                dummyable.makeExtra(level, at);
            } else {
                dummyable.removeExtra(level, at);
            }
        }
    }

    public boolean tryToggle(Player player, boolean remote) {
        if (level == null || level.isClientSide) {
            return state == SiloHatchLogic.STATE_CLOSED || state == SiloHatchLogic.STATE_OPEN;
        }
        if (!remote && state == SiloHatchLogic.STATE_CLOSED && redstonePower > 0) {
            return false;
        }
        if (state == SiloHatchLogic.STATE_CLOSED) {
            state = SiloHatchLogic.STATE_OPENING;
            sync();
            return true;
        }
        if (state == SiloHatchLogic.STATE_OPEN) {
            state = SiloHatchLogic.STATE_CLOSING;
            sync();
            return true;
        }
        return false;
    }

    public void updateRedstonePower(BlockPos pos) {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockPos key = pos.immutable();
        boolean powered = level.hasNeighborSignal(key);
        boolean contained = activatedBlocks.contains(key);
        if (!contained && powered) {
            activatedBlocks.add(key);
            if (redstonePower == -1) {
                redstonePower = 0;
            }
            redstonePower++;
            setChanged();
        } else if (contained && !powered) {
            activatedBlocks.remove(key);
            redstonePower--;
            if (redstonePower == 0) {
                redstonePower = -1;
            }
            setChanged();
        }
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        int r = large() ? 4 : 3;
        return new AABB(worldPosition.offset(-r, -1, -r), worldPosition.offset(r + 1, 8, r + 1));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte("state", state);
        tag.putInt("openTicks", openTicks);
        tag.putInt("redstoned", redstonePower);
        CompoundTag activated = new CompoundTag();
        int i = 0;
        for (BlockPos p : activatedBlocks) {
            activated.putInt("x" + i, p.getX());
            activated.putInt("y" + i, p.getY());
            activated.putInt("z" + i, p.getZ());
            i++;
        }
        tag.put("activatedBlocks", activated);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        state = tag.getByte("state");
        openTicks = tag.getInt("openTicks");
        redstonePower = tag.getInt("redstoned");
        activatedBlocks.clear();
        CompoundTag activated = tag.getCompound("activatedBlocks");
        for (int i = 0; activated.contains("x" + i); i++) {
            activatedBlocks.add(new BlockPos(activated.getInt("x" + i), activated.getInt("y" + i),
                    activated.getInt("z" + i)));
        }
        animStartTime = System.currentTimeMillis() - (long) openTicks * 50L;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putByte("state", state);
        tag.putInt("openTicks", openTicks);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        byte next = tag.getByte("state");
        openTicks = tag.getInt("openTicks");
        if (clientState != next) {
            if (SiloHatchLogic.moving(next)) {
                animStartTime = System.currentTimeMillis() - (long) openTicks * 50L;
            }
            clientState = next;
        }
        state = next;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag() == null ? new CompoundTag() : pkt.getTag());
    }
}
