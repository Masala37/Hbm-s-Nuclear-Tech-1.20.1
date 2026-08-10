package com.hbm.blocks.bomb;

import com.hbm.api.bomb.IBomb;
import com.hbm.blockentity.bomb.CrashedBombBlockEntity;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.ExplosionNT;
import com.hbm.items.tool.DefuserItem;
import com.hbm.lib.RefStrings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * Unbreakable crashed-bomb wreck / dud (legacy {@code BlockCrashedBomb}).
 * Four types: balefire, conventional, nuke, salted.
 */
public class CrashedBombBlock extends BaseEntityBlock implements IBomb {
    public static final EnumProperty<DudType> TYPE = EnumProperty.create("type", DudType.class);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    public CrashedBombBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(-1.0F, 6000.0F)
                .noLootTable()
                .noOcclusion()
                .sound(SoundType.METAL));
        registerDefaultState(stateDefinition.any().setValue(TYPE, DudType.BALEFIRE));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrashedBombBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return createTickerHelper(type, com.hbm.registry.ModBlockEntities.CRASHED_BOMB.get(),
                CrashedBombBlockEntity::serverTick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        DudType type = typeFromStack(stack);
        return defaultBlockState().setValue(TYPE, type);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof DefuserItem)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            DudType type = state.getValue(TYPE);
            dropDefuseLoot(level, pos, type);
            level.removeBlock(pos, false);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CrashedBombBlock)) {
            return BombReturnCode.ERROR_NO_BOMB;
        }

        DudType type = state.getValue(TYPE);
        level.removeBlock(pos, false);

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        switch (type) {
            case BALEFIRE -> {
                int r = (int) (BombConfig.fatmanRadius.get() * 1.25F);
                level.addFreshEntity(EntityBalefire.statFac(level, x, y, z, r));
                EntityNukeTorex.statFacBale(level, x, y + 0.5D, z, r);
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 15.0F, 1.0F);
            }
            case CONVENTIONAL -> {
                new ExplosionNT(level, null, x, y, z, (float) BombConfig.fatmanRadius.get())
                        .overrideResolution(24)
                        .addAttrib(ExplosionNT.ExAttrib.NODROP)
                        .explode();
            }
            case NUKE -> {
                int r = BombConfig.fatmanRadius.get();
                level.addFreshEntity(EntityNukeExplosionMK5.statFac(level, r, x, y, z));
                EntityNukeTorex.statFacStandard(level, x, y + 0.5D, z, r);
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 15.0F, 1.0F);
            }
            case SALTED -> {
                level.addFreshEntity(EntityNukeExplosionMK5.statFac(level, 25, x, y, z).moreFallout(25));
                EntityNukeTorex.statFacStandard(level, x, y + 0.5D, z, 25);
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 15.0F, 1.0F);
            }
        }

        return BombReturnCode.DETONATED;
    }

    public static DudType typeFromStack(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return DudType.BALEFIRE;
        }
        var tag = stack.getTagElement("BlockStateTag");
        if (tag == null || !tag.contains("type", 8)) {
            return DudType.BALEFIRE;
        }
        String name = tag.getString("type");
        for (DudType type : DudType.values()) {
            if (type.getSerializedName().equals(name)) {
                return type;
            }
        }
        return DudType.BALEFIRE;
    }

    public static ItemStack stackFor(DudType type) {
        ItemStack stack = new ItemStack(com.hbm.registry.ModItems.CRASHED_BOMB.get());
        stack.getOrCreateTagElement("BlockStateTag").putString("type", type.getSerializedName());
        return stack;
    }

    public static Component nameFor(DudType type) {
        return Component.translatable("block.hbm.crashed_bomb." + type.getSerializedName());
    }

    private static void dropDefuseLoot(Level level, BlockPos pos, DudType type) {
        switch (type) {
            case BALEFIRE -> drop(level, pos, com.hbm.registry.ModItems.EGG_BALEFIRE_SHARD.get(), 1);
            case CONVENTIONAL -> drop(level, pos, item("ball_tnt"), 16);
            case NUKE -> {
                drop(level, pos, item("ball_tnt"), 8);
                drop(level, pos, com.hbm.registry.ModItems.BILLET_PLUTONIUM.get(), 4);
            }
            case SALTED -> {
                drop(level, pos, item("ball_tnt"), 8);
                drop(level, pos, com.hbm.registry.ModItems.BILLET_PLUTONIUM.get(), 2);
                drop(level, pos, com.hbm.registry.ModItems.COBALT_INGOT.get(), 12);
            }
        }
    }

    @Nullable
    private static Item item(String path) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(RefStrings.MODID, path));
    }

    private static void drop(Level level, BlockPos pos, @Nullable Item item, int count) {
        if (item == null || count <= 0) {
            return;
        }
        ItemEntity entity = new ItemEntity(level,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                new ItemStack(item, count));
        entity.setDefaultPickUpDelay();
        level.addFreshEntity(entity);
    }
}
