package com.hbm.blocks.generic;

import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** 1.7 {@code BlockModDoor}: iron door that still opens by hand, with {@code hbm:block.openDoor}. */
public class NtmDoorBlock extends DoorBlock {
    /**
     * Metal sounds, but {@code canOpenByHand} like wood. Vanilla {@link BlockSetType#IRON} ignores clicks.
     */
    public static final BlockSetType NTM_METAL = BlockSetType.register(new BlockSetType(
            "hbm_ntm_metal",
            true,
            SoundType.METAL,
            SoundEvents.IRON_DOOR_CLOSE,
            SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE,
            SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF,
            SoundEvents.STONE_BUTTON_CLICK_ON));

    public NtmDoorBlock(float hardness, float resistance) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops()
                .noOcclusion(), NTM_METAL);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockPos lowerPos = lowerPos(state, pos);
        BlockState lower = level.getBlockState(lowerPos);
        boolean open = (lower.is(this) ? lower : state).getValue(OPEN);
        setOpen(player, level, lower.is(this) ? lower : state, lower.is(this) ? lowerPos : pos, !open);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setOpen(@Nullable Entity entity, Level level, BlockState state, BlockPos pos, boolean open) {
        if (!state.is(this)) {
            return;
        }
        BlockPos lowerPos = lowerPos(state, pos);
        BlockState lower = level.getBlockState(lowerPos);
        if (!lower.is(this)) {
            lower = state;
            lowerPos = pos;
        }
        BlockPos upperPos = lowerPos.above();
        BlockState upper = level.getBlockState(upperPos);
        boolean upperIsDoor = upper.is(this);
        if (lower.getValue(OPEN) == open && (!upperIsDoor || upper.getValue(OPEN) == open)) {
            return;
        }
        Direction facing = lower.getValue(FACING);
        DoorHingeSide hinge = upperIsDoor ? upper.getValue(HINGE) : lower.getValue(HINGE);
        boolean powered = lower.getValue(POWERED) || (upperIsDoor && upper.getValue(POWERED));
        level.setBlock(lowerPos, lower
                .setValue(OPEN, open)
                .setValue(FACING, facing)
                .setValue(HINGE, hinge)
                .setValue(POWERED, powered), 10);
        if (upperIsDoor) {
            level.setBlock(upperPos, upper
                    .setValue(OPEN, open)
                    .setValue(FACING, facing)
                    .setValue(HINGE, hinge)
                    .setValue(POWERED, powered), 10);
        }
        Player except = entity instanceof Player player ? player : null;
        level.playSound(except, pos, ModSounds.require("block.open_door"), SoundSource.BLOCKS,
                1.0F, level.random.nextFloat() * 0.1F + 0.9F);
        level.gameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
    }

    private static BlockPos lowerPos(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
    }
}
