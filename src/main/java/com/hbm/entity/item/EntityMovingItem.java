package com.hbm.entity.item;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

/**
 * 1.7.10 {@code EntityMovingItem} / {@code EntityMovingConveyorObject}.
 */
public class EntityMovingItem extends Entity implements IConveyorItem {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM =
            SynchedEntityData.defineId(EntityMovingItem.class, EntityDataSerializers.ITEM_STACK);

    public EntityMovingItem(EntityType<? extends EntityMovingItem> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntityMovingItem(Level level) {
        this(ModEntities.MOVING_ITEM.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_ITEM, ItemStack.EMPTY);
    }

    public void setItemStack(ItemStack stack) {
        entityData.set(DATA_ITEM, stack.copy());
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack stack = entityData.get(DATA_ITEM);
        return stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        knockOff();
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        knockOff();
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide && !isRemoved() && player.getInventory().add(getItemStack().copy())) {
            discard();
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private void knockOff() {
        if (level().isClientSide || isRemoved()) {
            return;
        }
        ItemStack stack = getItemStack();
        discard();
        if (!stack.isEmpty()) {
            level().addFreshEntity(new ItemEntity(level(), getX(), getY(), getZ(), stack));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (tickCount <= 5) {
            return;
        }
        if ((tickCount + getId()) % 400 == 0) {
            AABB box = getBoundingBox().inflate(0.125D);
            List<EntityMovingItem> crowded = level().getEntitiesOfClass(EntityMovingItem.class, box);
            if (crowded.size() >= 25) {
                for (EntityMovingItem obj : crowded) {
                    obj.discard();
                }
                BlockPos beltPos = BlockPos.containing(getX(), getY(), getZ());
                if (level().getBlockState(beltPos).getBlock() instanceof IConveyorBelt) {
                    level().destroyBlock(beltPos, false);
                }
                return;
            }
        }

        BlockPos blockPos = BlockPos.containing(getX(), getY(), getZ());
        BlockState state = level().getBlockState(blockPos);
        boolean onBelt = state.getBlock() instanceof IConveyorBelt belt
                && belt.canItemStay(level(), blockPos, position());
        if (!onBelt) {
            leaveConveyor();
            return;
        }

        Vec3 target = ((IConveyorBelt) state.getBlock())
                .getTravelLocation(level(), blockPos, position(), getMoveSpeed());
        BlockPos lastPos = blockPos;
        setPos(target.x, target.y, target.z);
        BlockPos newPos = BlockPos.containing(getX(), getY(), getZ());
        if (lastPos.equals(newPos)) {
            return;
        }

        BlockState entered = level().getBlockState(newPos);
        if (entered.getBlock() instanceof IEnterableBlock enterable) {
            Direction dir = enterDirection(lastPos, newPos);
            enterBlock(enterable, newPos, dir);
        } else if (entered.isAir() || !entered.blocksMotion()) {
            BlockPos below = newPos.below();
            BlockState belowState = level().getBlockState(below);
            if (belowState.getBlock() instanceof IEnterableBlock enterable) {
                enterBlock(enterable, below, Direction.UP);
            }
        }
    }

    private static Direction enterDirection(BlockPos last, BlockPos next) {
        if (last.getX() > next.getX()) {
            return Direction.EAST;
        }
        if (last.getX() < next.getX()) {
            return Direction.WEST;
        }
        if (last.getY() > next.getY()) {
            return Direction.UP;
        }
        if (last.getY() < next.getY()) {
            return Direction.DOWN;
        }
        if (last.getZ() > next.getZ()) {
            return Direction.SOUTH;
        }
        if (last.getZ() < next.getZ()) {
            return Direction.NORTH;
        }
        return Direction.NORTH;
    }

    private void enterBlock(IEnterableBlock enterable, BlockPos pos, Direction dir) {
        if (isRemoved()) {
            return;
        }
        if (enterable.canItemEnter(level(), pos, dir, this)) {
            enterable.onItemEnter(level(), pos, dir, this);
            discard();
        }
    }

    private void leaveConveyor() {
        if (isRemoved()) {
            return;
        }
        ItemStack stack = getItemStack();
        discard();
        if (stack.isEmpty()) {
            return;
        }
        ItemEntity item = new ItemEntity(level(),
                getX() + getDeltaMovement().x * 2.0D,
                getY() + getDeltaMovement().y * 2.0D,
                getZ() + getDeltaMovement().z * 2.0D,
                stack);
        item.setDeltaMovement(getDeltaMovement().x * 2.0D, 0.1D, getDeltaMovement().z * 2.0D);
        item.setUnlimitedLifetime();
        item.lifespan = 60 * 20;
        level().addFreshEntity(item);
    }

    public double getMoveSpeed() {
        return 0.0625D;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Item")) {
            setItemStack(ItemStack.of(tag.getCompound("Item")));
        }
        if (getItemStack().isEmpty()) {
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (!getItemStack().isEmpty()) {
            tag.put("Item", getItemStack().save(new CompoundTag()));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
