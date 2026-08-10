package com.hbm.blockentity.bomb;

import com.hbm.blocks.bomb.LandmineBlock;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Proximity sensor for {@link LandmineBlock} (legacy TileEntityLandmine).
 */
public class LandmineBlockEntity extends BlockEntity {
    private boolean primed;
    private boolean waitingForPlayer;

    public LandmineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LANDMINE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LandmineBlockEntity be) {
        if (!(state.getBlock() instanceof LandmineBlock landmine)) {
            return;
        }

        // Trigger volume needs clear air above the mine (legacy behavior).
        if (!level.getBlockState(pos.above()).isAir()) {
            return;
        }

        double range = landmine.getRange();
        double height = landmine.getHeight();
        if (be.waitingForPlayer) {
            range = 25.0D;
            height = 25.0D;
        } else if (!be.primed) {
            range *= 2.0D;
            height *= 2.0D;
        }

        AABB box = new AABB(
                pos.getX() - range, pos.getY() - height, pos.getZ() - range,
                pos.getX() + range + 1.0D, pos.getY() + height, pos.getZ() + range + 1.0D);

        for (Entity entity : level.getEntities((Entity) null, box, e -> true)) {
            if (entity instanceof WaterAnimal || entity instanceof AmbientCreature) {
                continue;
            }

            if (be.waitingForPlayer) {
                if (entity instanceof Player) {
                    be.waitingForPlayer = false;
                    be.setChanged();
                    return;
                }
                continue;
            }

            if (entity instanceof LivingEntity) {
                if (be.primed) {
                    landmine.explode(level, pos);
                }
                return;
            }
        }

        // Prime only after all living entities leave the double-radius keep-out zone.
        if (!be.primed && !be.waitingForPlayer) {
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 3.0F, 0.5F);
            be.primed = true;
            be.setChanged();
        }
    }

    public void setWaitingForPlayer(boolean waiting) {
        this.waitingForPlayer = waiting;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("primed", primed);
        tag.putBoolean("waiting", waitingForPlayer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        primed = tag.getBoolean("primed");
        waitingForPlayer = tag.getBoolean("waiting");
    }
}
