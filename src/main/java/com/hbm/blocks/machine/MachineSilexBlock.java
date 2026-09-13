package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.DummyableProxyBlockEntity;
import com.hbm.blockentity.machine.MachineDrops;
import com.hbm.blockentity.machine.SilexBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code MachineSILEX}: Dummyable {@code {2,0,1,1,1,1}}, offset 1.
 * Two item/fluid extras at core y+1 on the sides.
 */
public class MachineSilexBlock extends BlockDummyable {
    public static final int[] DIM = {2, 0, 1, 1, 1, 1};

    public MachineSilexBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
    }

    @Override
    public int[] getDimensions() {
        return DIM;
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new SilexBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (DummyableMeta.isCore(state.getValue(META))) {
            return newCoreEntity(pos, state);
        }
        if (DummyableMeta.isExtra(state.getValue(META))) {
            return new DummyableProxyBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    protected void fillExtras(Level level, BlockPos core, int facing) {
        if (facing == DummyableMeta.NORTH || facing == DummyableMeta.SOUTH) {
            makeExtra(level, core.offset(1, 1, 0));
            DummyableProxyBlockEntity.spawn(level, core.offset(1, 1, 0));
            makeExtra(level, core.offset(-1, 1, 0));
            DummyableProxyBlockEntity.spawn(level, core.offset(-1, 1, 0));
        } else {
            makeExtra(level, core.offset(0, 1, 1));
            DummyableProxyBlockEntity.spawn(level, core.offset(0, 1, 1));
            makeExtra(level, core.offset(0, 1, -1));
            DummyableProxyBlockEntity.spawn(level, core.offset(0, 1, -1));
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state)) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MACHINE_SILEX.get(), SilexBlockEntity::tick);
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
        if (!(be instanceof SilexBlockEntity silex)) {
            return super.use(state, level, pos, player, hand, hit);
        }
        ItemStack held = player.getItemInHand(hand);
        Fluid fromItem = fluidFromHeld(held);
        if (held.is(ModItems.FLUID_IDENTIFIER.get())) {
            silex.setTankType(SILEXRecipes.cycleNext(silex.getTankType()));
            player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
                    .append(new FluidStack(silex.getTankType(), 1).getDisplayName())
                    .append(Component.literal("!")));
        } else if (fromItem != null && fromItem != silex.getTankType()) {
            silex.setTankType(fromItem);
            player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
                    .append(new FluidStack(silex.getTankType(), 1).getDisplayName())
                    .append(Component.literal("!")));
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isCore(state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SilexBlockEntity silex) {
                MachineDrops.drop(level, pos, silex.getItems());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private static @Nullable Fluid fluidFromHeld(ItemStack held) {
        return FluidUtil.getFluidContained(held)
                .filter(stack -> !stack.isEmpty() && SILEXRecipes.isTankFluid(stack.getFluid()))
                .map(FluidStack::getFluid)
                .orElse(null);
    }
}
