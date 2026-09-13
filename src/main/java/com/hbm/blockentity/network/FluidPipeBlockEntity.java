package com.hbm.blockentity.network;

import com.hbm.fluid.FluidPipeNet;
import com.hbm.fluid.IFluidPipe;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityPipeBaseNT}: typed duct node, default none.
 */
public class FluidPipeBlockEntity extends BlockEntity implements IFluidPipe {
    private ResourceLocation fluid;
    private long lastFluidNetTick = Long.MIN_VALUE;

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.FLUID_DUCT.get(), pos, state);
    }

    public FluidPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidPipeBlockEntity be) {
        FluidPipeNet.tick(level, pos);
    }

    @Override
    public boolean isFluidPipe() {
        return true;
    }

    @Override
    public ResourceLocation pipeFluid() {
        return fluid;
    }

    @Override
    public void setPipeFluid(ResourceLocation fluid) {
        if (fluid == this.fluid || (fluid != null && fluid.equals(this.fluid))) {
            return;
        }
        this.fluid = fluid;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public long lastFluidNetTick() {
        return lastFluidNetTick;
    }

    @Override
    public void markFluidNetTick(long gameTime) {
        lastFluidNetTick = gameTime;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (fluid != null) {
            tag.putString("Fluid", fluid.toString());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Fluid")) {
            fluid = ResourceLocation.tryParse(tag.getString("Fluid"));
        } else {
            fluid = null;
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
