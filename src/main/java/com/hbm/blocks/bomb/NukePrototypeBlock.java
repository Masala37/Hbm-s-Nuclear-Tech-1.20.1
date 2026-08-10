package com.hbm.blocks.bomb;

import com.hbm.blockentity.bomb.AssembledNuke;
import com.hbm.blockentity.bomb.NukePrototypeBlockEntity;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Nina's Prototype — SAS3 cells + breeding rods; igniter or redstone detonates.
 * Uses FLEIJA MK3 dig + cloud like legacy.
 */
public class NukePrototypeBlock extends AssembledNukeBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NukePrototypeBlockEntity(pos, state);
    }

    @Override
    protected int blastRadius() {
        return BombConfig.prototypeRadius.get();
    }

    @Override
    protected String langKey() {
        return "block.hbm.nuke_prototype";
    }

    @Override
    protected void detonateBlast(Level level, BlockPos pos, int radius) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);

        EntityNukeExplosionMK3 dig = EntityNukeExplosionMK3.statFacFleija(level, x, y, z, radius);
        level.addFreshEntity(dig);
        EntityCloudFleija cloud = new EntityCloudFleija(level, radius);
        cloud.setPos(x, y, z);
        level.addFreshEntity(cloud);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.is(ModItems.IGNITER.get())) {
                AssembledNuke nuke = asAssembly(level.getBlockEntity(pos));
                if (nuke != null && nuke.isReady()) {
                    explode(level, pos);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
