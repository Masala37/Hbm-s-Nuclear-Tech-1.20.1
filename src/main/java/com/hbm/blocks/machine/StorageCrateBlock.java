package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.StorageCrateBlockEntity;
import com.hbm.inventory.menu.HbmMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Simple storage crate (iron = 36 slots, steel = 54).
 */
public class StorageCrateBlock extends BaseEntityBlock {
    private final int slots;
    private final String translationKey;

    public StorageCrateBlock(int slots, String translationKey) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL));
        this.slots = slots;
        this.translationKey = translationKey;
    }

    public int getSlots() {
        return slots;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StorageCrateBlockEntity(pos, state, slots, translationKey);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof StorageCrateBlockEntity) {
            HbmMenuHelper.open(sp, be);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof StorageCrateBlockEntity crate) {
                Containers.dropContents(level, pos, crate.asContainer());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
