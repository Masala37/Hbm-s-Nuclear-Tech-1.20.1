package api.hbm.block;

import com.hbm.items.tool.DefuserItem;
import com.hbm.items.tool.HandDrillItem;
import com.hbm.items.tool.ScrewdriverItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code api.hbm.block.IToolable}: screwdriver / hand drill / defuser interactions.
 */
public interface IToolable {
    boolean onScrew(Level level, Player player, BlockPos pos, Direction side, float hitX, float hitY, float hitZ,
                    ToolType tool);

    enum ToolType {
        SCREWDRIVER,
        HAND_DRILL,
        DEFUSER,
        WRENCH,
        TORCH,
        BOLT;

        @Nullable
        public static ToolType from(Item item) {
            if (item instanceof ScrewdriverItem) {
                return SCREWDRIVER;
            }
            if (item instanceof HandDrillItem) {
                return HAND_DRILL;
            }
            if (item instanceof DefuserItem) {
                return DEFUSER;
            }
            return null;
        }
    }

    static void hurtTool(ItemStack stack, Player player, InteractionHand hand) {
        if (stack.isDamageableItem()) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }
    }

    static InteractionResult tryUse(Level level, Player player, InteractionHand hand, BlockHitResult hit,
                                    IToolable toolable) {
        ItemStack held = player.getItemInHand(hand);
        ToolType type = ToolType.from(held.getItem());
        if (type == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Vec3 loc = hit.getLocation();
        BlockPos pos = hit.getBlockPos();
        if (toolable.onScrew(level, player, pos, hit.getDirection(),
                (float) (loc.x - pos.getX()), (float) (loc.y - pos.getY()), (float) (loc.z - pos.getZ()), type)) {
            hurtTool(held, player, hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    static InteractionResult tryUseOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (!(level.getBlockState(ctx.getClickedPos()).getBlock() instanceof IToolable toolable)) {
            return InteractionResult.PASS;
        }
        Player player = ctx.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        ItemStack held = ctx.getItemInHand();
        ToolType type = ToolType.from(held.getItem());
        if (type == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Vec3 loc = ctx.getClickLocation();
        BlockPos pos = ctx.getClickedPos();
        if (toolable.onScrew(level, player, pos, ctx.getClickedFace(),
                (float) (loc.x - pos.getX()), (float) (loc.y - pos.getY()), (float) (loc.z - pos.getZ()), type)) {
            hurtTool(held, player, ctx.getHand());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
