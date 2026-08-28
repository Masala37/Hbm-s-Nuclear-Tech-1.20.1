package com.hbm.blocks.generic;

import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModMobEffects;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collections;
import java.util.List;

/**
 * Legacy {@code BlockTaint}: aged spread infection (meta 0–15), sticky collision,
 * taint potion on contact, radiation step hazard.
 */
public class TaintBlock extends Block {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 15);
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public TaintBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(0.8F)
                .sound(SoundType.STONE)
                .randomTicks()
                .noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Collections.emptyList();
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int meta = state.getValue(AGE);
        if (meta >= 15) {
            return;
        }

        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                for (int k = -3; k <= 3; k++) {
                    if (Math.abs(i) + Math.abs(j) + Math.abs(k) > 4) {
                        continue;
                    }
                    if (random.nextFloat() > 0.25F) {
                        continue;
                    }
                    BlockPos target = pos.offset(i, j, k);
                    if (!level.isInWorldBounds(target)) {
                        continue;
                    }
                    BlockState other = level.getBlockState(target);
                    if (other.isAir() || other.is(Blocks.BEDROCK)
                            || other.getDestroySpeed(level, target) < 0.0F) {
                        continue;
                    }

                    int targetMeta = meta + 1;
                    boolean hasAir = false;
                    for (Direction dir : Direction.values()) {
                        if (level.getBlockState(target.relative(dir)).isAir()) {
                            hasAir = true;
                            break;
                        }
                    }
                    if (!hasAir) {
                        targetMeta = meta + 3;
                    }
                    if (targetMeta > 15) {
                        continue;
                    }
                    if (other.is(this) && other.getValue(AGE) >= targetMeta) {
                        continue;
                    }

                    BlockState infected = defaultBlockState().setValue(AGE, targetMeta);
                    level.setBlock(target, infected, 3);

                    if (random.nextFloat() < 0.25F
                            && FallingBlock.isFree(level.getBlockState(target.below()))) {
                        FallingBlockEntity.fall(level, target, infected);
                    }
                }
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, new Vec3(0.6D, 1.0D, 0.6D));

        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }

        int meta = state.getValue(AGE);
        int amplifier = 15 - meta;
        if (level.random.nextInt(50) == 0) {
            // Legacy: empty curative list — milk / honey cannot clear taint
            MobEffectInstance effect = new MobEffectInstance(
                    ModMobEffects.TAINT.get(), 15 * 20, amplifier, false, true);
            effect.setCurativeItems(Collections.emptyList());
            living.addEffect(effect);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.HAZMAT, 1.0F);
        }
        super.stepOn(level, pos, state, entity);
    }

    /** Fresh taint placed by missiles / barrels (age 0). */
    public static BlockState fresh() {
        return ModBlocks.TAINT.get().defaultBlockState().setValue(AGE, 0);
    }
}
