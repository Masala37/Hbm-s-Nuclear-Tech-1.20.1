package com.hbm.client;

import com.hbm.HbmNuclearTechMod;
import com.hbm.blocks.bomb.AssembledNukeBlock;
import com.hbm.lib.RefStrings;
import com.hbm.network.ModMessages;
import com.hbm.network.OpenBlockMenuPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Look-assist only: when a right-click misses the 1x1 nuke hitbox but aims at a nearby nuke.
 * Direct hits use {@code Block#use} (same path as shift-eject).
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Dist.CLIENT)
public final class ClientMenuClickHandler {
    private static final double RANGE = 10.0D;
    private static long lastSendMs;

    private ClientMenuClickHandler() {
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        BlockPos pos = findLookTarget(event.getEntity());
        if (pos != null) {
            sendOpen(pos);
        }
    }

    private static void sendOpen(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>) {
            return;
        }
        if (mc.player != null && mc.player.containerMenu != mc.player.inventoryMenu) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastSendMs < 500L) {
            return;
        }
        lastSendMs = now;
        HbmNuclearTechMod.LOGGER.info("Client look-assist GUI open at {}", pos);
        ModMessages.CHANNEL.sendToServer(new OpenBlockMenuPacket(pos));
    }

    private static BlockPos findLookTarget(Player player) {
        Level level = player.level();
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(RANGE));
        AABB sweep = new AABB(start, end).inflate(3.0D);

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = (int) Math.floor(sweep.minX); x <= (int) Math.floor(sweep.maxX); x++) {
            for (int y = (int) Math.floor(sweep.minY); y <= (int) Math.floor(sweep.maxY); y++) {
                for (int z = (int) Math.floor(sweep.minZ); z <= (int) Math.floor(sweep.maxZ); z++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    Block block = state.getBlock();
                    if (!(block instanceof AssembledNukeBlock)) {
                        continue;
                    }
                    Vec3 center = Vec3.atCenterOf(cursor);
                    Vec3 to = center.subtract(start);
                    double len = to.length();
                    if (len < 1.0E-4D || look.dot(to.normalize()) < 0.55D) {
                        continue;
                    }
                    double d = start.distanceToSqr(center);
                    if (d < bestDist) {
                        bestDist = d;
                        best = cursor.immutable();
                    }
                }
            }
        }
        return best;
    }
}
