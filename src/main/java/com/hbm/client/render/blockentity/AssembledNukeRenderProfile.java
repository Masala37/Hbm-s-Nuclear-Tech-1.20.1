package com.hbm.client.render.blockentity;

import com.hbm.blocks.bomb.AssembledNukeBlock;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

/**
 * Legacy TESR yaw / offset tables for assembled nukes.
 * Meta map (1.7): 2=N, 3=S, 4=W, 5=E.
 */
public enum AssembledNukeRenderProfile {
    /** Boy / Custom — facing yaw + local X −2. */
    BOY_LENGTH,
    /** Tsar / Fstbmb — Boy-style yaw, no offset. */
    BOY_YAW,
    /** Fat Man. */
    MAN,
    /** The Gadget. */
    GADGET,
    /** Ivy Mike. */
    MIKE,
    /** Fleija / Prototype. */
    FLEIJA,
    /** Solinium / N2 — base +90° then Boy-style facing yaw. */
    SOLINIUM;

    public static AssembledNukeRenderProfile of(Block block) {
        if (block == ModBlocks.NUKE_BOY.get() || block == ModBlocks.NUKE_CUSTOM.get()) {
            return BOY_LENGTH;
        }
        if (block == ModBlocks.NUKE_TSAR.get() || block == ModBlocks.NUKE_FSTBMB.get()) {
            return BOY_YAW;
        }
        if (block == ModBlocks.NUKE_MAN.get()) {
            return MAN;
        }
        if (block == ModBlocks.NUKE_GADGET.get()) {
            return GADGET;
        }
        if (block == ModBlocks.NUKE_MIKE.get()) {
            return MIKE;
        }
        if (block == ModBlocks.NUKE_FLEIJA.get() || block == ModBlocks.NUKE_PROTOTYPE.get()) {
            return FLEIJA;
        }
        if (block == ModBlocks.NUKE_SOLINIUM.get() || block == ModBlocks.NUKE_N2.get()) {
            return SOLINIUM;
        }
        return BOY_YAW;
    }

    public float baseYaw() {
        return this == SOLINIUM ? 90.0F : 0.0F;
    }

    public float facingYaw(Direction facing) {
        return switch (this) {
            case BOY_LENGTH, BOY_YAW, SOLINIUM -> switch (facing) {
                case NORTH -> 90.0F;
                case WEST -> 180.0F;
                case SOUTH -> 270.0F;
                default -> 0.0F; // EAST
            };
            case MAN -> switch (facing) {
                case SOUTH -> 90.0F;
                case EAST -> 180.0F;
                case NORTH -> 270.0F;
                default -> 0.0F; // WEST
            };
            case GADGET -> switch (facing) {
                case NORTH -> 0.0F;
                case WEST -> 90.0F;
                case SOUTH -> 180.0F;
                default -> 270.0F; // EAST
            };
            case MIKE -> switch (facing) {
                case SOUTH -> 0.0F;
                case EAST -> 90.0F;
                case NORTH -> 180.0F;
                default -> 270.0F; // WEST
            };
            case FLEIJA -> switch (facing) {
                case EAST -> 90.0F;
                case NORTH -> 180.0F;
                case WEST -> 270.0F;
                default -> 0.0F; // SOUTH
            };
        };
    }

    public double localOffsetX() {
        return this == BOY_LENGTH ? -2.0D : 0.0D;
    }
}
