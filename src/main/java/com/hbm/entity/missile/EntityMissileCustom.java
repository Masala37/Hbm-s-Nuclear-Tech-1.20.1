package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.handler.MissileStruct;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.items.weapon.ItemCustomMissilePart.WarheadType;
import com.hbm.registry.ModEntities;
import com.hbm.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityMissileCustom extends EntityMissileBaseNT {
    private static final EntityDataAccessor<String> DATA_WARHEAD =
            SynchedEntityData.defineId(EntityMissileCustom.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_FUSELAGE =
            SynchedEntityData.defineId(EntityMissileCustom.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_FINS =
            SynchedEntityData.defineId(EntityMissileCustom.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_THRUSTER =
            SynchedEntityData.defineId(EntityMissileCustom.class, EntityDataSerializers.STRING);

    public float fuel;
    public float consumption;

    public EntityMissileCustom(EntityType<? extends EntityMissileCustom> type, Level level) {
        super(type, level);
    }

    public EntityMissileCustom(Level level) {
        this(ModEntities.MISSILE_CUSTOM.get(), level);
    }

    public EntityMissileCustom(Level level, double x, double y, double z,
                               int targetX, int targetY, int targetZ, MissileStruct template) {
        super(ModEntities.MISSILE_CUSTOM.get(), level, x, y, z, targetX, targetY, targetZ);
        if (template != null) {
            setPart(DATA_WARHEAD, template.warhead);
            setPart(DATA_FUSELAGE, template.fuselage);
            setPart(DATA_FINS, template.fins);
            setPart(DATA_THRUSTER, template.thruster);
            if (template.fuselage != null && template.fuselage.attributes != null) {
                this.fuel = (Float) template.fuselage.attributes[1];
            }
            if (template.thruster != null && template.thruster.attributes != null) {
                this.consumption = (Float) template.thruster.attributes[1];
            }
        }
    }

    private void setPart(EntityDataAccessor<String> accessor, ItemCustomMissilePart part) {
        entityData.set(accessor, partId(part));
    }

    private static String partId(ItemCustomMissilePart part) {
        if (part == null) {
            return "";
        }
        var key = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(part);
        return key == null ? "" : key.toString();
    }

    public String warheadId() {
        return entityData.get(DATA_WARHEAD);
    }

    public String fuselageId() {
        return entityData.get(DATA_FUSELAGE);
    }

    public String finsId() {
        return entityData.get(DATA_FINS);
    }

    public String thrusterId() {
        return entityData.get(DATA_THRUSTER);
    }

    public ItemCustomMissilePart warhead() {
        return part(warheadId());
    }

    public ItemCustomMissilePart fuselage() {
        return part(fuselageId());
    }

    public ItemCustomMissilePart fins() {
        return part(finsId());
    }

    public ItemCustomMissilePart thruster() {
        return part(thrusterId());
    }

    private static ItemCustomMissilePart part(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return ItemCustomMissilePart.of(
                net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(id)));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_WARHEAD, "");
        entityData.define(DATA_FUSELAGE, "");
        entityData.define(DATA_FINS, "");
        entityData.define(DATA_THRUSTER, "");
    }

    @Override
    public void tick() {
        ItemCustomMissilePart warhead = warhead();
        if (warhead != null && warhead.attributes != null && warhead.attributes[0] instanceof WarheadType type
                && type.updateCustom != null) {
            type.updateCustom.accept(this);
        }
        if (!level().isClientSide && hasPropulsion()) {
            fuel -= consumption;
        }
        super.tick();
    }

    @Override
    public boolean hasPropulsion() {
        return fuel > 0.0F;
    }

    @Override
    protected void killMissile() {
        if (!isRemoved()) {
            discard();
            Vec3 motion = getDeltaMovement();
            com.hbm.explosion.ExplosionLarge.explode(level(), getX(), getY(), getZ(), 5, true, false, true, this);
            com.hbm.explosion.ExplosionLarge.spawnShrapnelShower(
                    level(), getX(), getY(), getZ(), motion.x, motion.y, motion.z, 15, 0.075);
        }
    }

    @Override
    protected void onImpact(HitResult hit) {
        CustomMissileImpacts.onImpact(this, hit);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        fuel = tag.getFloat("fuel");
        consumption = tag.getFloat("consumption");
        entityData.set(DATA_WARHEAD, tag.getString("warhead"));
        entityData.set(DATA_FUSELAGE, tag.getString("fuselage"));
        entityData.set(DATA_FINS, tag.getString("fins"));
        entityData.set(DATA_THRUSTER, tag.getString("thruster"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("fuel", fuel);
        tag.putFloat("consumption", consumption);
        tag.putString("warhead", warheadId());
        tag.putString("fuselage", fuselageId());
        tag.putString("fins", finsId());
        tag.putString("thruster", thrusterId());
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        super.writeSpawnData(buf);
        buf.writeUtf(warheadId());
        buf.writeUtf(fuselageId());
        buf.writeUtf(finsId());
        buf.writeUtf(thrusterId());
        buf.writeFloat(fuel);
        buf.writeFloat(consumption);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        super.readSpawnData(buf);
        entityData.set(DATA_WARHEAD, buf.readUtf());
        entityData.set(DATA_FUSELAGE, buf.readUtf());
        entityData.set(DATA_FINS, buf.readUtf());
        entityData.set(DATA_THRUSTER, buf.readUtf());
        fuel = buf.readFloat();
        consumption = buf.readFloat();
    }

    @Override
    public String getUnlocalizedName() {
        ItemCustomMissilePart fuselage = fuselage();
        if (fuselage == null) {
            return "radar.target.custom";
        }
        PartSize top = fuselage.top;
        PartSize bottom = fuselage.bottom;
        if (top == PartSize.SIZE_10 && bottom == PartSize.SIZE_10) {
            return "radar.target.custom10";
        }
        if (top == PartSize.SIZE_10 && bottom == PartSize.SIZE_15) {
            return "radar.target.custom1015";
        }
        if (top == PartSize.SIZE_15 && bottom == PartSize.SIZE_15) {
            return "radar.target.custom15";
        }
        if (top == PartSize.SIZE_15 && bottom == PartSize.SIZE_20) {
            return "radar.target.custom1520";
        }
        if (top == PartSize.SIZE_20 && bottom == PartSize.SIZE_20) {
            return "radar.target.custom20";
        }
        return "radar.target.custom";
    }

    @Override
    public int getBlipLevel() {
        ItemCustomMissilePart fuselage = fuselage();
        if (fuselage == null) {
            return IRadarDetectableNT.TIER1;
        }
        PartSize top = fuselage.top;
        PartSize bottom = fuselage.bottom;
        if (top == PartSize.SIZE_10 && bottom == PartSize.SIZE_10) {
            return IRadarDetectableNT.TIER10;
        }
        if (top == PartSize.SIZE_10 && bottom == PartSize.SIZE_15) {
            return IRadarDetectableNT.TIER10_15;
        }
        if (top == PartSize.SIZE_15 && bottom == PartSize.SIZE_15) {
            return IRadarDetectableNT.TIER15;
        }
        if (top == PartSize.SIZE_15 && bottom == PartSize.SIZE_20) {
            return IRadarDetectableNT.TIER15_20;
        }
        if (top == PartSize.SIZE_20 && bottom == PartSize.SIZE_20) {
            return IRadarDetectableNT.TIER20;
        }
        return IRadarDetectableNT.TIER1;
    }

    public ItemStack getMissileItemForInfo() {
        return new ItemStack(ModItems.MISSILE_CUSTOM.get());
    }

    public Vec3 motionVec() {
        return getDeltaMovement();
    }
}
