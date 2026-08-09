package com.hbm.blocks.bomb;

import com.hbm.api.bomb.IBomb;
import com.hbm.entity.bomb.PrimedBombEntity;
import com.hbm.explosion.ExplosionNT;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Conventional fuse bomb (dynamite / NTM TNT / semtex / C4).
 */
public class BombBlock extends Block implements IBomb {
    private final float power;
    private final boolean causesFire;

    public BombBlock(float power) {
        this(power, false);
    }

    public BombBlock(float power, boolean causesFire) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.FIRE)
                .instabreak()
                .sound(SoundType.GRASS)
                .ignitedByLava());
        this.power = power;
        this.causesFire = causesFire;
    }

    public float getPower() {
        return power;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            if (level.hasNeighborSignal(pos) || isAdjacentFire(level, pos)) {
                prime(level, pos, null);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.hasNeighborSignal(pos) || isAdjacentFire(level, pos)) {
            prime(level, pos, null);
        }
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide) {
            LivingEntity igniter = explosion.getIndirectSourceEntity();
            PrimedBombEntity primed = new PrimedBombEntity(level, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, igniter, this);
            primed.setFuse(level.random.nextInt(Math.max(1, primed.getFuse() / 4)) + primed.getFuse() / 8);
            level.addFreshEntity(primed);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) {
            prime(level, pos, player);
            if (!player.getAbilities().instabuild) {
                if (stack.is(Items.FLINT_AND_STEEL)) {
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                } else {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof AbstractArrow arrow && arrow.isOnFire()) {
            LivingEntity owner = arrow.getOwner() instanceof LivingEntity living ? living : null;
            prime(level, pos, owner);
        }
    }

    public void prime(Level level, BlockPos pos, @Nullable LivingEntity igniter) {
        if (level.isClientSide) {
            return;
        }
        level.removeBlock(pos, false);
        PrimedBombEntity primed = new PrimedBombEntity(level, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, igniter, this);
        level.addFreshEntity(primed);
        level.playSound(null, primed.getX(), primed.getY(), primed.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        prime(level, pos, null);
        return BombReturnCode.DETONATED;
    }

    @Override
    public void explodeEntity(Level level, double x, double y, double z, PrimedBombEntity entity) {
        ExplosionNT explosion = new ExplosionNT(level, entity, x, y, z, power);
        if (causesFire) {
            explosion.addAttrib(ExplosionNT.ExAttrib.FIRE);
        }
        explosion.explode();
    }

    private static boolean isAdjacentFire(BlockGetter level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(net.minecraft.world.level.block.Blocks.FIRE)
                    || level.getBlockState(pos.relative(direction)).is(net.minecraft.world.level.block.Blocks.SOUL_FIRE)) {
                return true;
            }
        }
        return false;
    }
}
