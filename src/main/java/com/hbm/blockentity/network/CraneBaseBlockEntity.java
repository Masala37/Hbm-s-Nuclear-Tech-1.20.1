package com.hbm.blockentity.network;

import com.hbm.blocks.network.CraneBaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityCraneBase}: input from block FACING, optional output override.
 */
public abstract class CraneBaseBlockEntity extends BlockEntity implements MenuProvider {
    public static final int OUTPUT_UNSET = 6;

    private int outputOverride = OUTPUT_UNSET;

    protected CraneBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction getInputSide() {
        BlockState state = getBlockState();
        return state.hasProperty(CraneBaseBlock.FACING) ? state.getValue(CraneBaseBlock.FACING) : Direction.NORTH;
    }

    public Direction getOutputSide() {
        if (outputOverride >= 0 && outputOverride < 6) {
            return Direction.from3DDataValue(outputOverride);
        }
        return getInputSide().getOpposite();
    }

    public void setOutputOverride(Direction direction) {
        Direction oldOut = getOutputSide();
        if (oldOut == direction) {
            direction = direction.getOpposite();
        }
        outputOverride = direction.get3DDataValue();
        if (direction == getInputSide()) {
            setInput(oldOut);
        } else {
            onChanged();
        }
    }

    public void setInput(Direction direction) {
        Direction oldIn = getInputSide();
        if (oldIn == direction) {
            direction = direction.getOpposite();
        }
        outputOverride = getOutputSide().get3DDataValue();
        boolean swap = direction == getOutputSide();
        if (level != null) {
            BlockState state = getBlockState();
            if (state.hasProperty(CraneBaseBlock.FACING)) {
                level.setBlock(worldPosition, state.setValue(CraneBaseBlock.FACING, direction),
                        swap ? Block.UPDATE_CLIENTS : Block.UPDATE_ALL);
            }
        }
        if (swap) {
            setOutputOverride(oldIn);
        } else {
            onChanged();
        }
    }

    protected void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte("CraneOutputOverride", (byte) outputOverride);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("CraneOutputOverride")) {
            outputOverride = tag.getByte("CraneOutputOverride") & 0xFF;
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
