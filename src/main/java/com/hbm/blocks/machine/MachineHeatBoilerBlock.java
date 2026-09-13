package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.DummyableProxyBlockEntity;
import com.hbm.blockentity.machine.HeatBoilerBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.blocks.ILookOverlay;
import com.hbm.fluid.SteamCycle;
import com.hbm.items.machine.InfiniteFluidBarrelItem;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

import java.util.List;
import java.util.Locale;

/**
 * 1.7.10 {@code MachineHeatBoiler}: 3×3×4 Dummyable, overlay only, no GUI.
 */
public class MachineHeatBoilerBlock extends BlockDummyable implements ILookOverlay {
    public MachineHeatBoilerBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
    }

    @Override
    public int[] getDimensions() {
        return new int[]{3, 0, 1, 1, 1, 1};
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new HeatBoilerBlockEntity(pos, state);
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
        int rot = DummyableMeta.rotateYClockwise(facing);
        BlockPos a = core.offset(DummyableMeta.offsetX(rot), 0, DummyableMeta.offsetZ(rot));
        BlockPos b = core.offset(-DummyableMeta.offsetX(rot), 0, -DummyableMeta.offsetZ(rot));
        BlockPos top = core.above(3);
        makeExtra(level, a);
        DummyableProxyBlockEntity.spawn(level, a);
        makeExtra(level, b);
        DummyableProxyBlockEntity.spawn(level, b);
        makeExtra(level, top);
        DummyableProxyBlockEntity.spawn(level, top);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state) || level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MACHINE_BOILER.get(), HeatBoilerBlockEntity::tick);
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
        if (be instanceof HeatBoilerBlockEntity boiler && player instanceof ServerPlayer
                && InfiniteFluidBarrelItem.interact(player, hand, boiler.getFluidHandler(),
                SteamCycle.hbmWater(), SteamCycle.oil())) {
            boiler.setChanged();
            level.sendBlockUpdated(core, level.getBlockState(core), level.getBlockState(core), 3);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void printHook(Level level, BlockPos pos, List<Component> lines) {
        BlockPos core = findCore(level, pos);
        if (core == null) {
            return;
        }
        BlockEntity be = level.getBlockEntity(core);
        if (!(be instanceof HeatBoilerBlockEntity boiler)) {
            return;
        }
        lines.add(Component.literal(String.format(Locale.US, "%,d", boiler.getHeat()) + "TU"));
        var water = boiler.getWater().getFluid();
        var steam = boiler.getSteam().getFluid();
        String waterName = water.isEmpty() ? "Water" : water.getDisplayName().getString();
        String steamName = steam.isEmpty() ? "Steam" : steam.getDisplayName().getString();
        lines.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(waterName + ": "
                        + String.format(Locale.US, "%,d", boiler.getWater().getFluidAmount())
                        + " / " + String.format(Locale.US, "%,d", boiler.getWater().getCapacity()) + "mB")));
        lines.add(Component.literal("<- ").withStyle(ChatFormatting.RED)
                .append(Component.literal(steamName + ": "
                        + String.format(Locale.US, "%,d", boiler.getSteam().getFluidAmount())
                        + " / " + String.format(Locale.US, "%,d", boiler.getSteam().getCapacity()) + "mB")));
    }
}
