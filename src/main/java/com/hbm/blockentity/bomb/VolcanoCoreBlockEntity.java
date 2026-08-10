package com.hbm.blockentity.bomb;

import com.hbm.blocks.bomb.VolcanoBlock;
import com.hbm.blocks.bomb.VolcanoMode;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.explosion.ExplosionNT;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Ticking volcano core (legacy {@code TileEntityVolcanoCore}).
 */
public class VolcanoCoreBlockEntity extends BlockEntity {
    private static final List<ExplosionNT.ExAttrib> VOLCANO_EXPLOSION = List.of(
            ExplosionNT.ExAttrib.NODROP,
            ExplosionNT.ExAttrib.LAVA_V,
            ExplosionNT.ExAttrib.NOSOUND,
            ExplosionNT.ExAttrib.ALLMOD,
            ExplosionNT.ExAttrib.NOHURT);
    private static final List<ExplosionNT.ExAttrib> VOLCANO_RAD_EXPLOSION = List.of(
            ExplosionNT.ExAttrib.NODROP,
            ExplosionNT.ExAttrib.LAVA_R,
            ExplosionNT.ExAttrib.NOSOUND,
            ExplosionNT.ExAttrib.ALLMOD,
            ExplosionNT.ExAttrib.NOHURT);

    private int volcanoTimer;

    public VolcanoCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOLCANO_CORE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VolcanoCoreBlockEntity be) {
        be.tickServer(level, pos, state);
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel server) || !(state.getBlock() instanceof VolcanoBlock volcano)) {
            return;
        }
        VolcanoMode mode = state.getValue(VolcanoBlock.MODE);
        volcanoTimer++;

        if (volcanoTimer % 10 == 0) {
            if (!mode.smoldering()) {
                blastMagmaChannel(server, pos, volcano);
                raiseMagma(server, pos, volcano);
            }

            double chamber = mode.smoldering() ? 15.0D : 0.0D;
            if (chamber > 0.0D) {
                blastMagmaChamber(server, pos, volcano, chamber);
            }

            if (mode.smoldering()) {
                meltSurface(server, pos, volcano, 50, 50.0D, 10.0D);
            }

            if (!mode.smoldering()) {
                spawnBlobs(server, pos, volcano);
                spawnSmoke(server, pos);
            }

            surroundLava(server, pos, volcano);
        }

        if (volcanoTimer >= updateRate(mode)) {
            volcanoTimer = 0;
            if (mode.grows() && pos.getY() < 200) {
                level.setBlock(pos.above(), state, 3);
                level.setBlock(pos, lavaState(volcano), 3);
            } else if (mode.extinguishes()) {
                level.setBlock(pos, lavaState(volcano), 3);
            }
        }
    }

    private static int updateRate(VolcanoMode mode) {
        return switch (mode) {
            case STATIC_EXTINGUISHING -> 60 * 60 * 20; // once per hour
            case GROWING_ACTIVE, GROWING_EXTINGUISHING -> 60 * 60 * 20 / 250; // ~14.4s
            default -> 10;
        };
    }

    private static BlockState lavaState(VolcanoBlock volcano) {
        return volcano.isRadioactive()
                ? ModBlocks.RAD_LAVA.get().defaultBlockState()
                : ModBlocks.VOLCANIC_LAVA.get().defaultBlockState();
    }

    private List<ExplosionNT.ExAttrib> attribs(VolcanoBlock volcano) {
        return volcano.isRadioactive() ? VOLCANO_RAD_EXPLOSION : VOLCANO_EXPLOSION;
    }

    private void blastMagmaChannel(ServerLevel level, BlockPos pos, VolcanoBlock volcano) {
        RandomSource rand = level.random;
        new ExplosionNT(level, null, pos.getX() + 0.5D, pos.getY() + rand.nextInt(15) + 1.5D, pos.getZ() + 0.5D, 7.0F)
                .addAllAttrib(attribs(volcano))
                .explode();
        new ExplosionNT(level, null,
                pos.getX() + 0.5D + rand.nextGaussian() * 3.0D,
                rand.nextInt(Math.max(1, pos.getY() + 1)),
                pos.getZ() + 0.5D + rand.nextGaussian() * 3.0D,
                10.0F)
                .addAllAttrib(attribs(volcano))
                .explode();
    }

    private void blastMagmaChamber(ServerLevel level, BlockPos pos, VolcanoBlock volcano, double size) {
        RandomSource rand = level.random;
        for (int i = 0; i < 2; i++) {
            double dist = size / (double) (i + 1);
            new ExplosionNT(level, null,
                    pos.getX() + 0.5D + rand.nextGaussian() * dist,
                    pos.getY() + 0.5D + rand.nextGaussian() * dist,
                    pos.getZ() + 0.5D + rand.nextGaussian() * dist,
                    7.0F)
                    .addAllAttrib(attribs(volcano))
                    .explode();
        }
    }

    private void meltSurface(ServerLevel level, BlockPos pos, VolcanoBlock volcano, int count, double radius, double depth) {
        RandomSource rand = level.random;
        BlockState lava = lavaState(volcano);
        float obsidianResist = Blocks.OBSIDIAN.getExplosionResistance();
        for (int i = 0; i < count; i++) {
            int x = (int) Math.floor(pos.getX() + rand.nextGaussian() * radius);
            int z = (int) Math.floor(pos.getZ() + rand.nextGaussian() * radius);
            int surface = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
            int y = surface + 1 - (int) Math.floor(Math.abs(rand.nextGaussian() * depth));
            BlockPos target = new BlockPos(x, y, z);
            if (!level.isInWorldBounds(target)) {
                continue;
            }
            BlockState state = level.getBlockState(target);
            if (state.isAir()) {
                continue;
            }
            if (state.getBlock().getExplosionResistance() >= obsidianResist) {
                continue;
            }
            if (state.isSolidRender(level, target)) {
                level.setBlock(target, lava, 3);
            } else {
                level.removeBlock(target, false);
            }
        }
    }

    private void raiseMagma(ServerLevel level, BlockPos pos, VolcanoBlock volcano) {
        RandomSource rand = level.random;
        BlockPos target = pos.offset(rand.nextInt(21) - 10, rand.nextInt(11), rand.nextInt(21) - 10);
        BlockState lava = lavaState(volcano);
        BlockState below = level.getBlockState(target.below());
        if (level.getBlockState(target).isAir()
                && (below.is(Blocks.LAVA) || below.is(ModBlocks.VOLCANIC_LAVA.get()) || below.is(ModBlocks.RAD_LAVA.get()))) {
            level.setBlock(target, lava, 3);
        }
    }

    private void surroundLava(ServerLevel level, BlockPos pos, VolcanoBlock volcano) {
        BlockState lava = lavaState(volcano);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    if (i == 0 && j == 0 && k == 0) {
                        continue;
                    }
                    level.setBlock(pos.offset(i, j, k), lava, 3);
                }
            }
        }
    }

    private void spawnBlobs(ServerLevel level, BlockPos pos, VolcanoBlock volcano) {
        RandomSource rand = level.random;
        boolean rad = volcano.isRadioactive();
        for (int i = 0; i < 3; i++) {
            EntityShrapnel shrapnel = new EntityShrapnel(level,
                    pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D);
            shrapnel.setVolcano(rad);
            shrapnel.setDeltaMovement(
                    rand.nextGaussian() * 0.2D,
                    1.0D + rand.nextDouble(),
                    rand.nextGaussian() * 0.2D);
            level.addFreshEntity(shrapnel);
            level.sendParticles(ParticleTypes.LAVA,
                    pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D,
                    8, 0.2D, 0.3D, 0.2D, 0.02D);
        }
    }

    private void spawnSmoke(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                pos.getX() + 0.5D, pos.getY() + 10.0D, pos.getZ() + 0.5D,
                24, 1.5D, 3.0D, 1.5D, 0.02D);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                pos.getX() + 0.5D, pos.getY() + 8.0D, pos.getZ() + 0.5D,
                12, 1.0D, 2.0D, 1.0D, 0.01D);
        level.sendParticles(ParticleTypes.ASH,
                pos.getX() + 0.5D, pos.getY() + 6.0D, pos.getZ() + 0.5D,
                16, 1.2D, 2.0D, 1.2D, 0.0D);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("timer", volcanoTimer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        volcanoTimer = tag.getInt("timer");
    }
}
