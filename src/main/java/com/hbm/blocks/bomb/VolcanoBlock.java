package com.hbm.blocks.bomb;

import com.hbm.blockentity.bomb.VolcanoCoreBlockEntity;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

/**
 * Volcano core (legacy {@code BlockVolcano} / {@code volcano_core} / {@code volcano_rad_core}).
 */
public class VolcanoBlock extends BaseEntityBlock {
    public static final EnumProperty<VolcanoMode> MODE = EnumProperty.create("mode", VolcanoMode.class);
    private static final String TAG_MODE = "VolcanoMode";

    private final boolean radioactive;

    public VolcanoBlock(boolean radioactive) {
        super(BlockBehaviour.Properties.of()
                .mapColor(radioactive ? MapColor.COLOR_GREEN : MapColor.COLOR_ORANGE)
                .strength(-1.0F, 10_000.0F)
                .sound(SoundType.METAL)
                .lightLevel(state -> 15));
        this.radioactive = radioactive;
        registerDefaultState(stateDefinition.any().setValue(MODE, VolcanoMode.STATIC_ACTIVE));
    }

    public boolean isRadioactive() {
        return radioactive;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VolcanoCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.VOLCANO_CORE.get(), VolcanoCoreBlockEntity::serverTick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(MODE, modeFromStack(context.getItemInHand()));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static VolcanoMode modeFromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_MODE)) {
            int idx = Mth.clamp(tag.getInt(TAG_MODE), 0, VolcanoMode.values().length - 1);
            return VolcanoMode.values()[idx];
        }
        return VolcanoMode.STATIC_ACTIVE;
    }

    public static ItemStack stackFor(Block block, VolcanoMode mode) {
        ItemStack stack = new ItemStack(block);
        stack.getOrCreateTag().putInt(TAG_MODE, mode.ordinal());
        return stack;
    }

    public static Component nameFor(boolean radioactive, VolcanoMode mode) {
        String prefix = radioactive ? "block.hbm.volcano_rad_core" : "block.hbm.volcano_core";
        return Component.translatable(prefix + "." + mode.getSerializedName());
    }
}
