package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.DummyableProxyBlockEntity;
import com.hbm.blockentity.machine.MachineDrops;
import com.hbm.blockentity.machine.WoodBurnerBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.items.machine.InfiniteFluidBarrelItem;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

/**
 * 1.7.10 {@code MachineWoodBurner}: 2×2×2 Dummyable, extras on the two back ground cells.
 */
public class MachineWoodBurnerBlock extends BlockDummyable {
    public MachineWoodBurnerBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
    }

    @Override
    public int[] getDimensions() {
        return new int[]{1, 0, 1, 0, 1, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new WoodBurnerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (DummyableMeta.isCore(state.getValue(META))) {
            return newCoreEntity(pos, state);
        }
        return new DummyableProxyBlockEntity(pos, state);
    }

    @Override
    protected void fillExtras(Level level, BlockPos core, int facing) {
        int rot = DummyableMeta.rotateYClockwise(facing);
        BlockPos a = core.offset(-DummyableMeta.offsetX(facing), 0, -DummyableMeta.offsetZ(facing));
        BlockPos b = a.offset(DummyableMeta.offsetX(rot), 0, DummyableMeta.offsetZ(rot));
        makeExtra(level, a);
        makeExtra(level, b);
        for (MultiblockHandlerXR.Cell cell : MultiblockHandlerXR.dummyCells(
                core.getX(), core.getY(), core.getZ(), getDimensions(), facing)) {
            DummyableProxyBlockEntity.spawn(level, new BlockPos(cell.x(), cell.y(), cell.z()));
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state)) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MACHINE_WOOD_BURNER.get(), WoodBurnerBlockEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos core = findCore(level, pos);
        if (core == null) {
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(core);
        if (be instanceof WoodBurnerBlockEntity burner && player instanceof ServerPlayer
                && InfiniteFluidBarrelItem.interact(player, hand, burner.getFluidHandler(),
                ModFluids.WOODOIL.source.get())) {
            burner.setChanged();
            level.sendBlockUpdated(core, level.getBlockState(core), level.getBlockState(core), 3);
            return InteractionResult.CONSUME;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isCore(state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WoodBurnerBlockEntity burner) {
                MachineDrops.drop(level, pos, burner.getItems());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
