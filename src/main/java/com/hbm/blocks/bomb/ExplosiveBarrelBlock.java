package com.hbm.blocks.bomb;

import com.hbm.api.bomb.IBomb;
import com.hbm.blocks.generic.TaintBlock;
import com.hbm.entity.bomb.PrimedBombEntity;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionThermo;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Explosive barrels (legacy {@code RedBarrel} / {@code YellowBarrel}).
 * Antimatter / steel / iron storage barrels stay decorative.
 */
public class ExplosiveBarrelBlock extends Block implements IBomb {
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public enum BarrelKind {
        /** Red / pink: HE + fire; flammable and shot-sensitive. */
        FIRE,
        /** LOX: small blast + freezer. */
        LOX,
        /** Taint: small blast + taint scatter. */
        TAINT,
        /** Yellow / vitrified: toxic spill or HE blast + waste + radon. */
        YELLOW
    }

    private final BarrelKind kind;
    private final boolean flammable;
    private final boolean detonateOnShot;
    private final int popFuse;
    private final boolean detonateOnCollision;

    public ExplosiveBarrelBlock(BarrelKind kind, boolean flammable, boolean detonateOnShot) {
        super(BlockBehaviour.Properties.of()
                .mapColor(mapColor(kind))
                .strength(0.5F, 2.5F)
                .sound(SoundType.METAL)
                .noOcclusion()
                .ignitedByLava());
        this.kind = kind;
        this.flammable = flammable;
        this.detonateOnShot = detonateOnShot;
        this.popFuse = 100;
        this.detonateOnCollision = true;
    }

    public static ExplosiveBarrelBlock fire() {
        return new ExplosiveBarrelBlock(BarrelKind.FIRE, true, true);
    }

    public static ExplosiveBarrelBlock lox() {
        return new ExplosiveBarrelBlock(BarrelKind.LOX, false, false);
    }

    public static ExplosiveBarrelBlock taint() {
        return new ExplosiveBarrelBlock(BarrelKind.TAINT, false, false);
    }

    public static ExplosiveBarrelBlock yellow() {
        return new ExplosiveBarrelBlock(BarrelKind.YELLOW, false, false);
    }

    public BarrelKind getKind() {
        return kind;
    }

    public boolean isShotSensitive() {
        return detonateOnShot;
    }

    private static MapColor mapColor(BarrelKind kind) {
        return switch (kind) {
            case FIRE -> MapColor.COLOR_RED;
            case LOX -> MapColor.ICE;
            case TAINT -> MapColor.COLOR_PURPLE;
            case YELLOW -> MapColor.COLOR_YELLOW;
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return flammable ? 15 : 0;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return flammable ? 2 : 0;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock()) && shouldIgnite(level, pos)) {
            prime(level, pos, null);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (shouldIgnite(level, pos)) {
            prime(level, pos, null);
        }
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide) {
            LivingEntity igniter = explosion.getIndirectSourceEntity();
            spawnPrimed(level, pos, igniter);
        }
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Collections.emptyList();
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity be, ItemStack tool) {
        if (!player.isCreative()) {
            popResource(level, pos, new ItemStack(this));
        }
    }

    /** Instant detonation when shot (legacy gun hit). */
    public void onShot(Level level, BlockPos pos) {
        if (!detonateOnShot || level.isClientSide) {
            return;
        }
        level.removeBlock(pos, false);
        explodeAt(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, null);
    }

    public void prime(Level level, BlockPos pos, @Nullable LivingEntity igniter) {
        if (level.isClientSide) {
            return;
        }
        level.removeBlock(pos, false);
        PrimedBombEntity primed = spawnPrimed(level, pos, igniter);
        level.playSound(null, primed.getX(), primed.getY(), primed.getZ(),
                SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private PrimedBombEntity spawnPrimed(Level level, BlockPos pos, @Nullable LivingEntity igniter) {
        PrimedBombEntity primed = new PrimedBombEntity(level,
                pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, igniter, this);
        int fuse = popFuse <= 0 ? 0 : level.random.nextInt(popFuse) + popFuse / 2;
        primed.setFuse(fuse);
        primed.setDetonateOnCollision(detonateOnCollision);
        level.addFreshEntity(primed);
        return primed;
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
        explodeAt(level, x, y, z, entity);
    }

    private void explodeAt(Level level, double x, double y, double z, @Nullable Entity source) {
        if (level.isClientSide) {
            return;
        }
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        int iz = (int) Math.floor(z);
        BlockPos origin = new BlockPos(ix, iy, iz);

        switch (kind) {
            case FIRE -> level.explode(source, x, y, z, 2.5F, true, Level.ExplosionInteraction.TNT);
            case LOX -> {
                level.explode(source, x, y, z, 1.0F, false, Level.ExplosionInteraction.NONE);
                ExplosionThermo.freezer(level, x, y, z, 7);
            }
            case TAINT -> {
                level.explode(source, x, y, z, 1.0F, false, Level.ExplosionInteraction.NONE);
                scatterTaint(level, origin);
            }
            case YELLOW -> {
                // Legacy: 1/3 spill toxic sludge at epicenter, otherwise HE blast
                if (level.random.nextInt(3) == 0) {
                    BlockPos spill = origin;
                    if (level.getBlockState(spill).canBeReplaced()
                            || level.getBlockState(spill).isAir()) {
                        level.setBlock(spill, ModBlocks.TOXIC_BLOCK.get().defaultBlockState(), 3);
                    } else if (level.getBlockState(spill.above()).canBeReplaced()) {
                        level.setBlock(spill.above(), ModBlocks.TOXIC_BLOCK.get().defaultBlockState(), 3);
                    }
                } else {
                    level.explode(source, x, y, z, 12.0F, true, Level.ExplosionInteraction.TNT);
                }
                ExplosionNukeGeneric.waste(level, ix, iy, iz, 35);
                scatterRadonGas(level, origin);
                ChunkRadiationManager.INSTANCE.incrementRad(level, ix, iy, iz, 35.0F);
            }
        }
    }

    private static void scatterTaint(Level level, BlockPos origin) {
        BlockState taint = TaintBlock.fresh();
        for (int i = 0; i < 100; i++) {
            BlockPos target = origin.offset(
                    level.random.nextInt(9) - 4,
                    level.random.nextInt(9) - 4,
                    level.random.nextInt(9) - 4);
            if (!level.isInWorldBounds(target)) {
                continue;
            }
            BlockState state = level.getBlockState(target);
            if (!state.isAir() && state.isSolidRender(level, target)) {
                level.setBlock(target, taint, 2);
            }
        }
    }

    private static void scatterRadonGas(Level level, BlockPos origin) {
        BlockState gas = ModBlocks.GAS_RADON_DENSE.get().defaultBlockState();
        for (int i = -5; i <= 5; i++) {
            for (int j = -5; j <= 5; j++) {
                for (int k = -5; k <= 5; k++) {
                    if (level.random.nextInt(5) != 0) {
                        continue;
                    }
                    BlockPos target = origin.offset(i, j, k);
                    if (level.isInWorldBounds(target) && level.getBlockState(target).isAir()) {
                        level.setBlock(target, gas, 2);
                    }
                }
            }
        }
    }

    private boolean shouldIgnite(Level level, BlockPos pos) {
        if (!flammable) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.is(Blocks.FIRE) || neighbor.is(Blocks.SOUL_FIRE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (kind == BarrelKind.YELLOW) {
            level.addParticle(ParticleTypes.MYCELIUM,
                    pos.getX() + random.nextFloat() * 0.5F + 0.25F,
                    pos.getY() + 1.1F,
                    pos.getZ() + random.nextFloat() * 0.5F + 0.25F,
                    0.0D, 0.0D, 0.0D);
        }
    }
}
