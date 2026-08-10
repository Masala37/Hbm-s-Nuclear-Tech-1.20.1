package com.hbm.blocks.bomb;

import com.hbm.api.bomb.IBomb;
import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionThermo;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Specialty bombs: float, EMP, flame war, thermo endo/exo (legacy BombFloat / EMP / FlameWar / Thermo).
 */
public class SpecialtyBombBlock extends Block implements IBomb {
    public enum Type {
        FLOAT,
        EMP,
        FLAME_WAR,
        THERM_ENDO,
        THERM_EXO
    }

    private final Type type;

    public SpecialtyBombBlock(Type type) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
        this.type = type;
    }

    public static SpecialtyBombBlock floatBomb() {
        return new SpecialtyBombBlock(Type.FLOAT);
    }

    public static SpecialtyBombBlock emp() {
        return new SpecialtyBombBlock(Type.EMP);
    }

    public static SpecialtyBombBlock flameWar() {
        return new SpecialtyBombBlock(Type.FLAME_WAR);
    }

    public static SpecialtyBombBlock thermEndo() {
        return new SpecialtyBombBlock(Type.THERM_ENDO);
    }

    public static SpecialtyBombBlock thermExo() {
        return new SpecialtyBombBlock(Type.THERM_EXO);
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
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        level.removeBlock(pos, false);

        switch (type) {
            case FLOAT -> {
                level.playSound(null, x, y, z, ModSounds.SPARK_SHOOT.get(), SoundSource.BLOCKS,
                        5.0F, level.random.nextFloat() * 0.2F + 0.9F);
                ExplosionChaos.floater(level, pos, 15, 50);
                ExplosionChaos.move(level, x, y, z, 15, 0, 50, 0);
            }
            case EMP -> {
                level.playSound(null, x, y, z, ModSounds.SPARK_SHOOT.get(), SoundSource.BLOCKS,
                        5.0F, level.random.nextFloat() * 0.2F + 0.9F);
                ExplosionNukeGeneric.empBlast(level, pos, 50);
                EntityEMPBlast.spawn(level, x, y, z, 50);
            }
            case FLAME_WAR -> {
                for (int i = 0; i < 150; i++) {
                    double px = pos.getX() + level.random.nextInt(51) - 25 + 0.5D;
                    double py = pos.getY() + level.random.nextInt(11) - 5 + 0.5D;
                    double pz = pos.getZ() + level.random.nextInt(51) - 25 + 0.5D;
                    new ExplosionNT(level, null, px, py, pz, 4.0F)
                            .addAttrib(ExplosionNT.ExAttrib.NOBLOCK)
                            .addAttrib(ExplosionNT.ExAttrib.NOSOUND)
                            .addAttrib(ExplosionNT.ExAttrib.NOPARTICLE)
                            .explode();
                }
                new ExplosionNT(level, null, x, y, z, 15.0F)
                        .overrideResolution(32)
                        .addAttrib(ExplosionNT.ExAttrib.NODROP)
                        .explode();
            }
            case THERM_ENDO -> {
                ExplosionThermo.freeze(level, pos, 15);
                ExplosionThermo.freezer(level, x, y, z, 20);
                level.explode(null, x, y, z, 5.0F, Level.ExplosionInteraction.TNT);
            }
            case THERM_EXO -> {
                ExplosionThermo.scorch(level, pos, 15);
                ExplosionThermo.setEntitiesOnFire(level, x, y, z, 20);
                level.explode(null, x, y, z, 5.0F, Level.ExplosionInteraction.TNT);
            }
        }

        return BombReturnCode.DETONATED;
    }
}
