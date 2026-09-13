package com.hbm.blocks.network;

import com.hbm.blockentity.machine.DummyableProxyBlockEntity;
import com.hbm.blockentity.network.PylonBlockEntity;
import com.hbm.blockentity.network.PylonKind;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.7.10 medium pylons, large pylon, and substation.
 */
public class PylonDummyableBlock extends BlockDummyable implements PylonKindBlock {
    private final PylonKind kind;

    public PylonDummyableBlock(PylonKind kind) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
        this.kind = kind;
    }

    @Override
    public PylonKind pylonKind() {
        return kind;
    }

    @Override
    public int[] getDimensions() {
        return kind.dimensions();
    }

    @Override
    public int getOffset() {
        return kind.offset();
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new PylonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (DummyableMeta.isCore(state.getValue(META))) {
            return newCoreEntity(pos, state);
        }
        if (kind.hasExtras() && DummyableMeta.isExtra(state.getValue(META))) {
            return new DummyableProxyBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    protected void fillExtras(Level level, BlockPos core, int facing) {
        if (!kind.hasExtras()) {
            return;
        }
        for (int x : new int[]{-1, 1}) {
            for (int z : new int[]{-1, 1}) {
                BlockPos extra = core.offset(x, 0, z);
                makeExtra(level, extra);
                DummyableProxyBlockEntity.spawn(level, extra);
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || !isCore(state)) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.PYLON.get(), PylonBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        return PylonBlocks.use(level, pos, player, hand);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isCore(state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PylonBlockEntity pylon) {
                pylon.disconnectAll();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        PylonBlocks.tooltip(this, tooltip);
    }
}
