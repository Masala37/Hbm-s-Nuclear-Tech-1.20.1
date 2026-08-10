package com.hbm.items.tool;

import com.hbm.api.bomb.IBomb;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

/**
 * Aim and click to detonate a bomb in line of sight (legacy {@code ItemLaserDetonator}).
 */
public class LaserDetonatorItem extends Item {
    private static final double RANGE = 128.0D;

    public LaserDetonatorItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.detonator_laser.desc"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        if (hit.getType() != HitResult.Type.BLOCK) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable(IBomb.BombReturnCode.ERROR_NO_BOMB.getMessageKey()), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.0F, 0.5F);
            }
            return InteractionResultHolder.fail(stack);
        }

        BlockPos pos = hit.getBlockPos();
        if (level.isClientSide) {
            spawnBeam(level, player, Vec3.atCenterOf(pos));
            return InteractionResultHolder.success(stack);
        }

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof IBomb bomb) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            IBomb.BombReturnCode result = bomb.explode(level, pos);
            player.displayClientMessage(Component.translatable(result.getMessageKey()), true);
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.0F, 0.5F);
            player.displayClientMessage(Component.translatable(IBomb.BombReturnCode.ERROR_NO_BOMB.getMessageKey()), true);
        }
        return InteractionResultHolder.consume(stack);
    }

    private static void spawnBeam(Level level, Player player, Vec3 target) {
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 delta = target.subtract(start);
        double len = Math.min(delta.length(), 15.0D);
        if (len < 0.1D) {
            return;
        }
        Vec3 dir = delta.normalize();
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(1.0F, 0.15F, 0.15F), 1.0F);
        for (int i = 0; i < (int) len; i++) {
            double t = level.random.nextDouble() * len + 1.0D;
            Vec3 p = start.add(dir.scale(t));
            level.addParticle(dust, p.x, p.y, p.z, 0.0D, 0.0D, 0.0D);
        }
    }
}
