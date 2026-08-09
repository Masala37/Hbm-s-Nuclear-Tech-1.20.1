package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.ElectricFurnaceBlockEntity;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ElectricFurnaceBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ElectricFurnaceBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .lightLevel(state -> state.getValue(LIT) ? 13 : 0));
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricFurnaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.ELECTRIC_FURNACE.get(), ElectricFurnaceBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ElectricFurnaceBlockEntity furnace)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        // Shift-empty-hand: take output, then input
        if (held.isEmpty() && player.isShiftKeyDown()) {
            ItemStack out = furnace.getItems().getStackInSlot(1);
            if (!out.isEmpty()) {
                ItemStack taken = furnace.getItems().extractItem(1, 64, false);
                if (!player.addItem(taken)) {
                    player.drop(taken, false);
                }
                return InteractionResult.CONSUME;
            }
            ItemStack in = furnace.getItems().getStackInSlot(0);
            if (!in.isEmpty()) {
                furnace.getItems().setStackInSlot(0, ItemStack.EMPTY);
                if (!player.addItem(in)) {
                    player.drop(in, false);
                }
                return InteractionResult.CONSUME;
            }
        }

        // Insert into input
        if (!held.isEmpty()) {
            ItemStack slot = furnace.getItems().getStackInSlot(0);
            if (slot.isEmpty()) {
                furnace.getItems().setStackInSlot(0, held.split(1));
                return InteractionResult.CONSUME;
            }
            if (ItemStack.isSameItemSameTags(slot, held) && slot.getCount() < slot.getMaxStackSize()) {
                slot.grow(1);
                held.shrink(1);
                furnace.getItems().setStackInSlot(0, slot);
                return InteractionResult.CONSUME;
            }
        }

        ItemStack in = furnace.getItems().getStackInSlot(0);
        ItemStack out = furnace.getItems().getStackInSlot(1);
        player.displayClientMessage(Component.literal(String.format(
                "E-Furnace: %d/%d FE | cook %d/%d | in: %s | out: %s",
                furnace.getEnergy().getEnergyStored(),
                furnace.getEnergy().getMaxEnergyStored(),
                furnace.getCookProgress(),
                furnace.getCookTimeTotal(),
                in.isEmpty() ? "-" : in.getHoverName().getString(),
                out.isEmpty() ? "-" : out.getHoverName().getString())), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ElectricFurnaceBlockEntity furnace) {
                Containers.dropContents(level, pos, new SimpleContainer(
                        furnace.getItems().getStackInSlot(0),
                        furnace.getItems().getStackInSlot(1)));
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
