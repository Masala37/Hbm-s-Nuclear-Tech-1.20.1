package com.hbm.blocks.bomb;

import com.hbm.api.bomb.IBomb;
import com.hbm.blockentity.bomb.BombMultiBlockEntity;
import com.hbm.entity.effect.EntityMist;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.inventory.menu.HbmMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Multi-Purpose Bomb — 4× TNT + optional dual warheads (HE / cluster / fire / poison / gas).
 */
public class BombMultiBlock extends BaseEntityBlock implements IBomb {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    public BombMultiBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 200.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BombMultiBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof MenuProvider provider ? provider : null;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock()) && level.hasNeighborSignal(pos)) {
            explode(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && level.hasNeighborSignal(pos)) {
            explode(level, pos);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BombMultiBlockEntity bomb)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            bomb.dropContents();
            player.displayClientMessage(Component.translatable("block.hbm.bomb_multi.ejected"), true);
            return InteractionResult.CONSUME;
        }

        if (player instanceof ServerPlayer sp) {
            try {
                HbmMenuHelper.open(sp, bomb, pos);
            } catch (Throwable t) {
                net.minecraftforge.network.NetworkHooks.openScreen(sp, bomb, pos);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BombMultiBlockEntity bomb) {
                Containers.dropContents(level, pos, new net.minecraft.world.SimpleContainer(bomb.copyStacks()));
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BombMultiBlockEntity bomb) || !bomb.isLoaded()) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }

        float explosionValue = BombMultiBlockEntity.EXPLOSION_BASE;
        int clusterCount = 0;
        int fireRadius = 0;
        int poisonRadius = 0;
        int gasCloud = 0;

        for (int type : new int[] {bomb.return2type(), bomb.return5type()}) {
            switch (type) {
                case 1 -> explosionValue += 1.0F;
                case 2 -> explosionValue += 4.0F;
                case 3 -> clusterCount += 50;
                case 4 -> fireRadius += 10;
                case 5 -> poisonRadius += 15;
                case 6 -> gasCloud += 50;
                default -> {
                }
            }
        }

        bomb.clearSlots();
        level.removeBlock(pos, false);

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);

        ExplosionNT blast = new ExplosionNT(level, null, x, y, z, explosionValue);
        if (fireRadius > 0) {
            blast.addAttrib(ExplosionNT.ExAttrib.FIRE);
        }
        blast.explode();

        if (clusterCount > 0) {
            ExplosionChaos.cluster(level, x, y, z, clusterCount,
                    0.0F, (float) Math.PI * 0.5F, (float) Math.PI * 2.0F, (float) Math.PI * 0.125F, 0.375F);
        }
        if (fireRadius > 0) {
            igniteRadius(level, pos, fireRadius);
        }
        if (poisonRadius > 0) {
            ExplosionNukeGeneric.wasteNoSchrab(level, pos.getX(), pos.getY(), pos.getZ(), poisonRadius);
            poisonRadius(level, x, y, z, poisonRadius);
        }
        if (gasCloud > 0) {
            spawnGasCloud(level, x, y, z, gasCloud);
        }

        return BombReturnCode.DETONATED;
    }

    private static void igniteRadius(Level level, BlockPos origin, int radius) {
        int r = Math.min(radius, 16);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy + dz * dz > r * r) {
                        continue;
                    }
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.isAir()) {
                        BlockPos below = cursor.below();
                        if (!level.getBlockState(below).isAir()) {
                            level.setBlock(cursor, Blocks.FIRE.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    private static void poisonRadius(Level level, double x, double y, double z, int radius) {
        AABB box = new AABB(x, y, z, x, y, z).inflate(radius);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box)) {
            double dist = Math.sqrt(living.distanceToSqr(x, y, z));
            if (dist > radius) {
                continue;
            }
            int duration = (int) ((radius - dist) * 40);
            living.addEffect(new MobEffectInstance(MobEffects.POISON, Math.max(60, duration), 1));
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, Math.max(40, duration / 2), 0));
        }
    }

    private static void spawnGasCloud(Level level, double x, double y, double z, int gasCloud) {
        if (level.isClientSide) {
            return;
        }
        float width = gasCloud * 15.0F / 50.0F;
        float height = gasCloud * 7.5F / 50.0F;
        EntityMist mist = new EntityMist(level)
                .setChlorine()
                .setArea(Math.max(2.0F, width), Math.max(2.0F, height))
                .setDuration(150);
        mist.setPos(x, y, z);
        level.addFreshEntity(mist);
    }
}
