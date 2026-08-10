package com.hbm.blocks.bomb;

import com.hbm.blockentity.bomb.FireworksBlockEntity;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
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
 * Fireworks launcher (legacy {@code BlockFireworks}). Not an explosive.
 */
public class FireworksBlock extends BaseEntityBlock {
    public FireworksBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F, 5.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FireworksBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.FIREWORKS.get(), FireworksBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FireworksBlockEntity fireworks)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty()) {
            if (held.is(Items.GUNPOWDER)) {
                fireworks.addCharges(held.getCount() * 3);
                held.setCount(0);
                return InteractionResult.CONSUME;
            }
            if (held.is(ModItems.SULFUR.get())) {
                fireworks.addCharges(held.getCount());
                held.setCount(0);
                return InteractionResult.CONSUME;
            }
            if (held.getItem() instanceof DyeItem dye) {
                fireworks.setColor(dye.getDyeColor().getFireworkColor());
                held.shrink(1);
                return InteractionResult.CONSUME;
            }
            if (held.is(Items.NAME_TAG) && held.hasCustomHoverName()) {
                fireworks.setMessage(held.getHoverName().getString());
                held.shrink(1);
                return InteractionResult.CONSUME;
            }
        }

        player.displayClientMessage(Component.translatable(getDescriptionId()).withStyle(style -> style.withColor(0xFFAA00)), false);
        player.displayClientMessage(Component.translatable("block.hbm.fireworks.charges", fireworks.getCharges())
                .withStyle(style -> style.withColor(0xFFFF55)), false);
        player.displayClientMessage(Component.translatable("block.hbm.fireworks.color",
                        Integer.toHexString(fireworks.getColor()))
                .withStyle(style -> style.withColor(0xFFFF55)), false);
        player.displayClientMessage(Component.translatable("block.hbm.fireworks.message", fireworks.getMessage())
                .withStyle(style -> style.withColor(0xFFFF55)), false);
        return InteractionResult.CONSUME;
    }
}
