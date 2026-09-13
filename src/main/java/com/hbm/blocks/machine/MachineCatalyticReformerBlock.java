package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.CatalyticReformerBlockEntity;
import com.hbm.blockentity.machine.DummyableProxyBlockEntity;
import com.hbm.blockentity.machine.MachineDrops;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.recipes.ReformingRecipes;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 {@code MachineCatalyticReformer}: Dummyable {@code {2,0,1,1,2,2}} plus two extra towers.
 */
public class MachineCatalyticReformerBlock extends BlockDummyable {
    public static final int[] DIM = {2, 0, 1, 1, 2, 2};
    public static final int[] EXTRA_A = {3, -3, 1, 0, -1, 2};
    public static final int[] EXTRA_B = {6, -3, 1, 1, 2, 0};

    public MachineCatalyticReformerBlock() {
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
        return new CatalyticReformerBlockEntity(pos, state);
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
    protected List<MultiblockHandlerXR.Cell> extraDummyCells(BlockPos core, int facing) {
        List<MultiblockHandlerXR.Cell> extra = new ArrayList<>();
        extra.addAll(MultiblockHandlerXR.dummyCells(core.getX(), core.getY(), core.getZ(), EXTRA_A, facing));
        extra.addAll(MultiblockHandlerXR.dummyCells(core.getX(), core.getY(), core.getZ(), EXTRA_B, facing));
        return extra;
    }

    @Override
    protected void fillExtras(Level level, BlockPos core, int facing) {
        int rot = DummyableMeta.rotateYClockwise(facing);
        int rx = DummyableMeta.offsetX(rot);
        int rz = DummyableMeta.offsetZ(rot);
        port(level, core.offset(1, 0, 1));
        port(level, core.offset(1, 0, -1));
        port(level, core.offset(-1, 0, 1));
        port(level, core.offset(-1, 0, -1));
        port(level, core.offset(rx * 2, 0, rz * 2));
        port(level, core.offset(-rx * 2, 0, -rz * 2));
    }

    private void port(Level level, BlockPos at) {
        makeExtra(level, at);
        DummyableProxyBlockEntity.spawn(level, at);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state) || level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MACHINE_CATALYTIC_REFORMER.get(),
                CatalyticReformerBlockEntity::tick);
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
        if (!(be instanceof CatalyticReformerBlockEntity reformer)) {
            return super.use(state, level, pos, player, hand, hit);
        }
        ItemStack held = player.getItemInHand(hand);
        Fluid fromItem = fluidFromHeld(held);
        if (held.is(ModItems.FLUID_IDENTIFIER.get())) {
            reformer.setOilType(ReformingRecipes.cycleNext(reformer.getOilType()));
            player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
                    .append(new FluidStack(reformer.getOilType(), 1).getDisplayName())
                    .append(Component.literal("!")));
        } else if (fromItem != null && fromItem != reformer.getOilType()) {
            reformer.setOilType(fromItem);
            player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
                    .append(new FluidStack(reformer.getOilType(), 1).getDisplayName())
                    .append(Component.literal("!")));
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isCore(state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CatalyticReformerBlockEntity reformer) {
                MachineDrops.drop(level, pos, reformer.getItems());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private static @Nullable Fluid fluidFromHeld(ItemStack held) {
        return FluidUtil.getFluidContained(held)
                .filter(stack -> !stack.isEmpty() && ReformingRecipes.get(stack.getFluid()) != null)
                .map(FluidStack::getFluid)
                .orElse(null);
    }
}
