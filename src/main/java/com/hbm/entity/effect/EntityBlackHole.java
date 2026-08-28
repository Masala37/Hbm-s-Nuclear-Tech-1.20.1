package com.hbm.entity.effect;

import com.hbm.entity.projectile.EntityRubble;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModEntities;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Persistent singularity (legacy {@code EntityBlackHole}).
 * Pulls entities, shreds blocks into {@link EntityRubble}, kills at the event horizon.
 */
public class EntityBlackHole extends Entity {
    private static final EntityDataAccessor<Float> DATA_SIZE =
            SynchedEntityData.defineId(EntityBlackHole.class, EntityDataSerializers.FLOAT);
    private static final ResourceLocation FLAME_PONY_ID =
            new ResourceLocation(RefStrings.MODID, "flame_pony");

    public boolean breaksBlocks = true;

    public EntityBlackHole(EntityType<? extends EntityBlackHole> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;
        this.setInvulnerable(true);
    }

    public EntityBlackHole(Level level) {
        this(ModEntities.BLACK_HOLE.get(), level);
    }

    public EntityBlackHole(Level level, float size) {
        this(level);
        setHoleSize(size);
    }

    public EntityBlackHole noBreak() {
        this.breaksBlocks = false;
        return this;
    }

    public float getHoleSize() {
        return entityData.get(DATA_SIZE);
    }

    public void setHoleSize(float size) {
        entityData.set(DATA_SIZE, size);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_SIZE, 0.5F);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && tickCount == 1) {
            com.hbm.HbmNuclearTechMod.proxy.playBlackHole(this);
        }

        float size = getHoleSize();
        Level level = level();

        if (!level.isClientSide && breaksBlocks) {
            shredBlocks(size);
        }

        double range = size * 15.0D;
        AABB box = new AABB(
                getX() - range, getY() - range, getZ() - range,
                getX() + range, getY() + range, getZ() + range);
        List<Entity> entities = level.getEntities(this, box);

        for (Entity e : entities) {
            if (e instanceof Player player && player.getAbilities().instabuild) {
                continue;
            }

            if (e instanceof FallingBlockEntity falling && !level.isClientSide && e.tickCount > 1) {
                convertFallingBlock(falling);
                continue;
            }

            Vec3 vec = new Vec3(getX() - e.getX(), getY() - e.getY(), getZ() - e.getZ());
            double dist = vec.length();
            if (dist > range) {
                continue;
            }
            if (dist > 1.0E-6D) {
                vec = vec.normalize();
            }
            if (!(e instanceof ItemEntity)) {
                vec = vec.yRot((float) Math.toRadians(15.0D));
            }

            double speed = 0.1D;
            e.setDeltaMovement(e.getDeltaMovement().add(vec.x * speed, vec.y * speed * 2.0D, vec.z * speed));
            e.hurtMarked = true;

            if (e instanceof EntityBlackHole) {
                continue;
            }

            if (dist < size * 1.5D) {
                e.hurt(ModDamageSource.blackhole(level), 1000.0F);
                if (!(e instanceof LivingEntity)) {
                    e.discard();
                }
                if (!level.isClientSide && e instanceof ItemEntity itemEntity) {
                    ItemStack stack = itemEntity.getItem();
                    if (isSingularityCollapseItem(stack.getItem())) {
                        discard();
                        level.explode(null, getX(), getY(), getZ(), 5.0F, true, Level.ExplosionInteraction.TNT);
                        return;
                    }
                }
            }
        }

        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);
        setDeltaMovement(getDeltaMovement().scale(0.99D));
    }

    private void shredBlocks(float size) {
        Level level = level();
        int rays = Math.max(1, (int) (size * 2.0F));
        int length = (int) Math.ceil(size * 15.0D);
        for (int k = 0; k < rays; k++) {
            double phi = random.nextDouble() * (Math.PI * 2.0D);
            double costheta = random.nextDouble() * 2.0D - 1.0D;
            double theta = Math.acos(costheta);
            double vx = Math.sin(theta) * Math.cos(phi);
            double vy = Math.sin(theta) * Math.sin(phi);
            double vz = Math.cos(theta);

            for (int i = 0; i < length; i++) {
                // Legacy uses toward-zero (int) casts, not floor.
                int x0 = (int) (getX() + vx * i);
                int y0 = (int) (getY() + vy * i);
                int z0 = (int) (getZ() + vz * i);
                BlockPos pos = new BlockPos(x0, y0, z0);
                if (!level.isInWorldBounds(pos)) {
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (!state.getFluidState().isEmpty()) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    state = level.getBlockState(pos);
                }
                if (!state.isAir()) {
                    EntityRubble rubble = new EntityRubble(level, x0 + 0.5D, y0, z0 + 0.5D);
                    rubble.setBasedOnBlock(state.getBlock());
                    level.addFreshEntity(rubble);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    break;
                }
            }
        }
    }

    private void convertFallingBlock(FallingBlockEntity falling) {
        Level level = level();
        BlockState state = falling.getBlockState();
        EntityRubble rubble = new EntityRubble(level, falling.getX(), falling.getY(), falling.getZ());
        rubble.setBasedOnBlock(state.getBlock());
        rubble.setYRot(0.0F);
        rubble.setXRot(0.0F);
        rubble.setDeltaMovement(falling.getDeltaMovement());
        falling.discard();
        level.addFreshEntity(rubble);
    }

    private static boolean isSingularityCollapseItem(Item item) {
        if (item == null) {
            return false;
        }
        if (item == ModItems.PELLET_ANTIMATTER.get()) {
            return true;
        }
        Item pony = ForgeRegistries.ITEMS.getValue(FLAME_PONY_ID);
        return pony != null && item == pony;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setHoleSize(tag.getFloat("size"));
        this.breaksBlocks = !tag.contains("breaksBlocks") || tag.getBoolean("breaksBlocks");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("size", getHoleSize());
        tag.putBoolean("breaksBlocks", breaksBlocks);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 25000.0D * 25000.0D;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}
