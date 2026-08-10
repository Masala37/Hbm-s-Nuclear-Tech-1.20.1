package com.hbm.blocks.bomb;

import com.hbm.api.bomb.IBomb;
import com.hbm.api.bomb.IDetConnectible;
import com.hbm.config.BombConfig;
import com.hbm.explosion.ExplosionNT;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.Collections;
import java.util.List;

/**
 * Det charge / miner / nuke (legacy {@code ExplosiveCharge} / {@code DetMiner}).
 */
public class DetExplosiveBlock extends Block implements IBomb, IDetConnectible {
    public enum Type {
        CHARGE,
        MINER,
        NUKE
    }

    private final Type type;

    public DetExplosiveBlock(Type type) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.1F, 0.0F)
                .sound(SoundType.METAL)
                .ignitedByLava());
        this.type = type;
    }

    public static DetExplosiveBlock charge() {
        return new DetExplosiveBlock(Type.CHARGE);
    }

    public static DetExplosiveBlock miner() {
        return new DetExplosiveBlock(Type.MINER);
    }

    public static DetExplosiveBlock nuke() {
        return new DetExplosiveBlock(Type.NUKE);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock()) && level.hasNeighborSignal(pos)) {
            explode(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.hasNeighborSignal(pos)) {
            explode(level, pos);
        }
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide) {
            detonateAt(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
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

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        level.removeBlock(pos, false);
        detonateAt(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        return BombReturnCode.DETONATED;
    }

    private void detonateAt(Level level, double x, double y, double z) {
        switch (type) {
            case CHARGE -> {
                new ExplosionNT(level, null, x, y, z, 15.0F)
                        .overrideResolution(64)
                        .explode();
                if (level instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y + 1.0D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    server.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.5D, z, 24, 0.8D, 0.5D, 0.8D, 0.05D);
                }
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                        6.0F, 0.85F + level.random.nextFloat() * 0.2F);
            }
            case MINER -> {
                new ExplosionNT(level, null, x, y, z, 4.0F)
                        .addAttrib(ExplosionNT.ExAttrib.ALLDROP)
                        .addAttrib(ExplosionNT.ExAttrib.NOHURT)
                        .explode();
                if (level instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.CLOUD, x, y + 0.5D, z, 30, 0.6D, 0.4D, 0.6D, 0.08D);
                    server.sendParticles(ParticleTypes.CRIT, x, y + 0.5D, z, 20, 0.5D, 0.4D, 0.5D, 0.2D);
                }
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 3.0F, 1.15F);
            }
            case NUKE -> AssembledNukeBlock.ignite(level,
                    BlockPos.containing(x, y, z), BombConfig.missileRadius.get());
        }
    }
}
