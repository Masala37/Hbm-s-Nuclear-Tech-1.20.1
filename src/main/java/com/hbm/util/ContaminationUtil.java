package com.hbm.util;

import com.hbm.capability.HbmLivingProps;
import com.hbm.config.RadiationConfig;
import com.hbm.handler.HazmatRegistry;
import com.hbm.handler.radiation.ChunkRadiationManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Contamination facade (legacy ContaminationUtil radiation + digamma paths).
 */
public final class ContaminationUtil {
    private ContaminationUtil() {
    }

    public enum HazardType {
        RADIATION,
        DIGAMMA
    }

    public enum ContaminationType {
        HAZMAT,
        HAZMAT2,
        DIGAMMA,
        DIGAMMA2,
        CREATIVE,
        RAD_BYPASS,
        NONE
    }

    public static float calculateRadiationMod(LivingEntity entity) {
        if (entity instanceof Player player) {
            return (float) Math.pow(10.0F, -HazmatRegistry.getResistance(player));
        }
        return 1.0F;
    }

    public static float getRads(Entity e) {
        if (!(e instanceof LivingEntity living) || isRadImmune(e)) {
            return 0.0F;
        }
        return HbmLivingProps.getRadiation(living);
    }

    public static boolean isRadImmune(Entity e) {
        return e instanceof MushroomCow
                || e instanceof Zombie
                || e instanceof Skeleton
                || e instanceof Ocelot;
    }

    public static void applyDigammaData(Entity e, float amount) {
        if (!(e instanceof LivingEntity living)) {
            return;
        }
        if (RadiationConfig.enableDigamma != null && !RadiationConfig.enableDigamma.get()) {
            return;
        }
        if (living instanceof Ocelot) {
            return;
        }
        if (living instanceof Player player) {
            if (player.getAbilities().instabuild || player.tickCount < 200) {
                return;
            }
            if (ArmorUtil.checkForDigamma(player) || ArmorUtil.checkForDigamma2(player)) {
                return;
            }
        }
        HbmLivingProps.incrementDigamma(living, amount);
    }

    public static void applyDigammaDirect(Entity e, float amount) {
        if (!(e instanceof LivingEntity living)) {
            return;
        }
        if (RadiationConfig.enableDigamma != null && !RadiationConfig.enableDigamma.get()) {
            return;
        }
        if (living instanceof Player player && player.getAbilities().instabuild) {
            return;
        }
        HbmLivingProps.incrementDigamma(living, amount);
    }

    public static float getDigamma(Entity e) {
        if (!(e instanceof LivingEntity living)) {
            return 0.0F;
        }
        return HbmLivingProps.getDigamma(living);
    }

    public static boolean contaminate(LivingEntity entity, HazardType hazard, ContaminationType cont, float amount) {
        if (hazard == HazardType.RADIATION) {
            HbmLivingProps.setRadEnv(entity, HbmLivingProps.getRadEnv(entity) + amount);
        }

        if (entity instanceof Player player) {
            switch (cont) {
                case HAZMAT -> {
                    if (ArmorUtil.checkForHazmat(player)) {
                        return false;
                    }
                }
                case HAZMAT2 -> {
                    if (ArmorUtil.checkForHaz2(player)) {
                        return false;
                    }
                }
                case DIGAMMA -> {
                    if (ArmorUtil.checkForDigamma(player) || ArmorUtil.checkForDigamma2(player)) {
                        return false;
                    }
                }
                case DIGAMMA2 -> {
                    if (ArmorUtil.checkForDigamma2(player)) {
                        return false;
                    }
                }
                default -> {
                }
            }

            if (player.getAbilities().instabuild && cont != ContaminationType.NONE && cont != ContaminationType.DIGAMMA2) {
                return false;
            }
            if (player.tickCount < 200) {
                return false;
            }
        }

        switch (hazard) {
            case RADIATION -> {
                if (isRadImmune(entity)) {
                    return false;
                }
                float applied = amount * (cont == ContaminationType.RAD_BYPASS ? 1.0F : calculateRadiationMod(entity));
                HbmLivingProps.incrementRadiation(entity, applied);
                return true;
            }
            case DIGAMMA -> {
                HbmLivingProps.incrementDigamma(entity, amount);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public static void printGeigerData(Player player) {
        Level level = player.level();
        double eRad = Math.floor(HbmLivingProps.getRadiation(player) * 10.0D) / 10.0D;
        double rads = Math.floor(ChunkRadiationManager.INSTANCE.getRadiation(
                level,
                (int) Math.floor(player.getX()),
                (int) Math.floor(player.getY()),
                (int) Math.floor(player.getZ())) * 10.0D) / 10.0D;
        double env = Math.floor(HbmLivingProps.getRadBuf(player) * 10.0D) / 10.0D;
        double res = Math.floor((10000.0D - calculateRadiationMod(player) * 10000.0D)) / 100.0D;
        double resKoeff = Math.floor(HazmatRegistry.getResistance(player) * 100.0D) / 100.0D;

        player.sendSystemMessage(Component.literal("===== ☢ Geiger ☢ =====").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("Chunk radiation: " + prefixFromRad(rads) + rads + " RAD/s")
                .withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal("Environment: " + prefixFromRad(env) + env + " RAD/s")
                .withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal("Player dose: " + playerRadPrefix(eRad) + eRad + " RAD")
                .withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal("Resistance: " + (resKoeff > 0 ? ChatFormatting.GREEN : ChatFormatting.WHITE)
                + res + "% (" + resKoeff + ")").withStyle(ChatFormatting.YELLOW));
    }

    public static void printDiagnosticData(Player player) {
        double digamma = Math.floor(HbmLivingProps.getDigamma(player) * 100.0D) / 100.0D;
        double halflife = Math.floor((1.0D - Math.pow(0.5D, digamma)) * 10000.0D) / 100.0D;

        player.sendSystemMessage(Component.literal("===== Ϝ Digamma Ϝ =====").withStyle(ChatFormatting.DARK_PURPLE));
        player.sendSystemMessage(Component.literal("Player digamma: " + ChatFormatting.RED + digamma + " DRX")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal("Health lost: " + ChatFormatting.RED + halflife + "%")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static String prefixFromRad(double rads) {
        if (rads == 0.0D) {
            return ChatFormatting.GREEN.toString();
        }
        if (rads < 1.0D) {
            return ChatFormatting.YELLOW.toString();
        }
        if (rads < 10.0D) {
            return ChatFormatting.GOLD.toString();
        }
        if (rads < 100.0D) {
            return ChatFormatting.RED.toString();
        }
        if (rads < 1000.0D) {
            return ChatFormatting.DARK_RED.toString();
        }
        return ChatFormatting.DARK_GRAY.toString();
    }

    private static String playerRadPrefix(double eRad) {
        if (eRad < 200.0D) {
            return ChatFormatting.GREEN.toString();
        }
        if (eRad < 400.0D) {
            return ChatFormatting.YELLOW.toString();
        }
        if (eRad < 600.0D) {
            return ChatFormatting.GOLD.toString();
        }
        if (eRad < 800.0D) {
            return ChatFormatting.RED.toString();
        }
        if (eRad < 1000.0D) {
            return ChatFormatting.DARK_RED.toString();
        }
        return ChatFormatting.DARK_GRAY.toString();
    }
}
