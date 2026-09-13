package com.hbm.items.tool;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 1.7 {@code ItemToolAbility} / {@code IToolAreaAbility} / {@code IToolHarvestAbility} / {@code IWeaponAbility}
 * without the GUI. Sneak + right-click cycles the default presets.
 */
public final class NtmToolAbilities {
    public static final int[] RECURSION_RADIUS = {3, 4, 5, 6, 7, 9, 10};
    public static final int[] HAMMER_RANGE = {1, 2, 3, 4};
    public static final int[] LUCK_FORTUNE = {1, 2, 3, 4, 5, 9};
    public static final int[] STUN_SECONDS = {2, 3, 5, 10, 15};
    public static final int[] FIRE_SECONDS = {5, 10};
    public static final float[] VAMPIRE_HEAL = {2F, 3F, 5F, 10F, 50F};
    public static final int RECURSION_DEPTH = 1000;
    private static final UUID MOVEMENT_UUID = UUID.fromString("2b512d17-8b3c-4e7a-9f21-6c0a7b8d4e90");

    private NtmToolAbilities() {
    }

    public enum Area {
        RECURSION("tool.ability.recursion", 1),
        HAMMER("tool.ability.hammer", 2),
        HAMMER_FLAT("tool.ability.hammer_flat", 3);

        final String lang;
        final int sort;

        Area(String lang, int sort) {
            this.lang = lang;
            this.sort = sort;
        }

        String extension(int level) {
            int[] values = this == RECURSION ? RECURSION_RADIUS : HAMMER_RANGE;
            return " (" + values[clamp(level, values.length)] + ")";
        }
    }

    public enum Harvest {
        SILK("tool.ability.silktouch", 1),
        LUCK("tool.ability.luck", 2),
        SMELTER("tool.ability.smelter", 3);

        final String lang;
        final int sort;

        Harvest(String lang, int sort) {
            this.lang = lang;
            this.sort = sort;
        }

        String extension(int level) {
            if (this != LUCK) {
                return "";
            }
            return " (" + LUCK_FORTUNE[clamp(level, LUCK_FORTUNE.length)] + ")";
        }
    }

    public enum Weapon {
        STUN("weapon.ability.stun", 3),
        VAMPIRE("weapon.ability.vampire", 2),
        FIRE("weapon.ability.fire", 6),
        BEHEADER("weapon.ability.beheader", 8);

        final String lang;
        final int sort;

        Weapon(String lang, int sort) {
            this.lang = lang;
            this.sort = sort;
        }

        String extension(int level) {
            return switch (this) {
                case STUN -> " (" + STUN_SECONDS[clamp(level, STUN_SECONDS.length)] + ")";
                case FIRE -> " (" + FIRE_SECONDS[clamp(level, FIRE_SECONDS.length)] + ")";
                case VAMPIRE -> " (" + VAMPIRE_HEAL[clamp(level, VAMPIRE_HEAL.length)] + ")";
                case BEHEADER -> "";
            };
        }
    }

    public static final class Profile {
        final List<AreaBind> areas;
        final List<HarvestBind> harvests;
        final List<WeaponBind> weapons;
        final double movement;

        Profile(List<AreaBind> areas, List<HarvestBind> harvests, List<WeaponBind> weapons, double movement) {
            this.areas = List.copyOf(areas);
            this.harvests = List.copyOf(harvests);
            this.weapons = List.copyOf(weapons);
            this.movement = movement;
        }

        public static Builder builder() {
            return new Builder();
        }

        public boolean canCycle() {
            return !areas.isEmpty() || !harvests.isEmpty();
        }

        public boolean silkActive(ItemStack stack) {
            return active(stack, this).harvest == Harvest.SILK;
        }
    }

    public static final class Builder {
        private final List<AreaBind> areas = new ArrayList<>();
        private final List<HarvestBind> harvests = new ArrayList<>();
        private final List<WeaponBind> weapons = new ArrayList<>();
        private double movement;

        public Builder area(Area area, int level) {
            areas.add(new AreaBind(area, level));
            return this;
        }

        public Builder harvest(Harvest harvest, int level) {
            harvests.add(new HarvestBind(harvest, level));
            return this;
        }

        public Builder weapon(Weapon weapon, int level) {
            weapons.add(new WeaponBind(weapon, level));
            return this;
        }

        public Builder movement(double movement) {
            this.movement = movement;
            return this;
        }

        public Profile build() {
            return new Profile(areas, harvests, weapons, movement);
        }
    }

    record AreaBind(Area area, int level) {
    }

    record HarvestBind(Harvest harvest, int level) {
    }

    record WeaponBind(Weapon weapon, int level) {
    }

    record Preset(@Nullable Area area, int areaLevel, @Nullable Harvest harvest, int harvestLevel) {
        boolean none() {
            return area == null && harvest == null;
        }
    }

    public static float attackModifier(NtmTiers tier, float oneSevenDamage) {
        return oneSevenDamage - tier.getAttackDamageBonus();
    }

    public static int swordModifier(NtmTiers tier, float oneSevenDamage) {
        return Math.round(attackModifier(tier, oneSevenDamage));
    }

    public static InteractionResultHolder<ItemStack> cycleUse(Level level, Player player, InteractionHand hand,
                                                              ItemStack stack, Profile profile) {
        if (!player.isShiftKeyDown() || !profile.canCycle()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            cycle(stack, profile, player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player, Profile profile) {
        if (player.level().isClientSide || !(player instanceof ServerPlayer sp) || !(player.level() instanceof ServerLevel server)) {
            return false;
        }
        if (!profile.canCycle()) {
            return false;
        }
        Preset preset = active(stack, profile);
        if (preset.none()) {
            return false;
        }
        BlockState state = server.getBlockState(pos);
        if (state.isAir() || !canHarvest(stack, state, profile)) {
            return false;
        }
        Map<Enchantment, Integer> saved = EnchantmentHelper.getEnchantments(stack);
        applyHarvestEnchant(stack, preset);
        try {
            if (preset.area != null) {
                applyArea(preset, server, pos, sp, stack, profile);
            }
            harvestBlock(server, pos, sp, stack, preset, profile, pos, true);
        } finally {
            EnchantmentHelper.setEnchantments(saved, stack);
        }
        return true;
    }

    public static void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker, Profile profile) {
        if (attacker.level().isClientSide || !(attacker instanceof Player player)) {
            return;
        }
        for (WeaponBind bind : profile.weapons) {
            applyWeapon(bind, player, target);
        }
    }

    public static void tooltip(ItemStack stack, List<Component> tooltip, Profile profile) {
        if (profile.canCycle()) {
            tooltip.add(Component.translatable("tool.ability.header").withStyle(ChatFormatting.GRAY));
            for (AreaBind bind : profile.areas) {
                tooltip.add(Component.literal("  ").append(Component.translatable(bind.area.lang)
                        .append(bind.area.extension(bind.level))).withStyle(ChatFormatting.GOLD));
            }
            for (HarvestBind bind : profile.harvests) {
                tooltip.add(Component.literal("  ").append(Component.translatable(bind.harvest.lang)
                        .append(bind.harvest.extension(bind.level))).withStyle(ChatFormatting.GOLD));
            }
            tooltip.add(Component.translatable("tool.ability.cycle").withStyle(ChatFormatting.YELLOW));
            Preset preset = active(stack, profile);
            if (preset.none()) {
                tooltip.add(Component.translatable("tool.ability.off").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                tooltip.add(Component.literal("  ").append(presetLabel(preset)).withStyle(ChatFormatting.AQUA));
            }
        }
        if (!profile.weapons.isEmpty()) {
            tooltip.add(Component.translatable("weapon.ability.header").withStyle(ChatFormatting.GRAY));
            for (WeaponBind bind : profile.weapons) {
                tooltip.add(Component.literal("  ").append(Component.translatable(bind.weapon.lang)
                        .append(bind.weapon.extension(bind.level))).withStyle(ChatFormatting.RED));
            }
        }
    }

    public static boolean foil(ItemStack stack, Profile profile) {
        return profile.canCycle() && !active(stack, profile).none();
    }

    public static Multimap<Attribute, AttributeModifier> withMovement(
            Multimap<Attribute, AttributeModifier> base, Profile profile) {
        if (profile.movement == 0) {
            return base;
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(base);
        builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(MOVEMENT_UUID, "Tool modifier",
                profile.movement, AttributeModifier.Operation.MULTIPLY_TOTAL));
        return builder.build();
    }

    public static boolean minerCorrect(ItemStack stack, BlockState state, Profile profile) {
        if (profile.silkActive(stack)) {
            return true;
        }
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    private static void cycle(ItemStack stack, Profile profile, Player player) {
        List<Preset> presets = presets(profile);
        CompoundTag tag = stack.getOrCreateTag();
        int next = (tag.getInt("ability") + 1) % presets.size();
        tag.putInt("ability", next);
        Preset preset = presets.get(next);
        if (preset.none()) {
            player.displayClientMessage(Component.translatable("chat.hbm.tool.ability.off")
                    .withStyle(ChatFormatting.GOLD), true);
        } else {
            player.displayClientMessage(Component.translatable("chat.hbm.tool.ability.on", presetLabel(preset))
                    .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    private static Component presetLabel(Preset preset) {
        net.minecraft.network.chat.MutableComponent label = Component.empty();
        if (preset.area != null) {
            label.append(Component.translatable(preset.area.lang)).append(preset.area.extension(preset.areaLevel));
        }
        if (preset.area != null && preset.harvest != null) {
            label.append(" + ");
        }
        if (preset.harvest != null) {
            label.append(Component.translatable(preset.harvest.lang)).append(preset.harvest.extension(preset.harvestLevel));
        }
        return label;
    }

    private static Preset active(ItemStack stack, Profile profile) {
        List<Preset> presets = presets(profile);
        int index = 0;
        if (stack.hasTag()) {
            index = stack.getTag().getInt("ability");
        }
        if (index < 0 || index >= presets.size()) {
            index = 0;
        }
        return presets.get(index);
    }

    private static List<Preset> presets(Profile profile) {
        List<Preset> presets = new ArrayList<>();
        presets.add(new Preset(null, 0, null, 0));
        for (AreaBind bind : profile.areas) {
            presets.add(new Preset(bind.area, bind.level, null, 0));
        }
        for (HarvestBind bind : profile.harvests) {
            presets.add(new Preset(null, 0, bind.harvest, bind.level));
        }
        presets.subList(1, presets.size()).sort((a, b) -> {
            int harvestA = a.harvest == null ? 0 : a.harvest.sort;
            int harvestB = b.harvest == null ? 0 : b.harvest.sort;
            if (harvestA != harvestB) {
                return Integer.compare(harvestA, harvestB);
            }
            if (a.harvestLevel != b.harvestLevel) {
                return Integer.compare(a.harvestLevel, b.harvestLevel);
            }
            int areaA = a.area == null ? 0 : a.area.sort;
            int areaB = b.area == null ? 0 : b.area.sort;
            if (areaA != areaB) {
                return Integer.compare(areaA, areaB);
            }
            return Integer.compare(a.areaLevel, b.areaLevel);
        });
        return presets;
    }

    private static void applyHarvestEnchant(ItemStack stack, Preset preset) {
        if (preset.harvest == Harvest.SILK) {
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
            map.put(Enchantments.SILK_TOUCH, 1);
            EnchantmentHelper.setEnchantments(map, stack);
        } else if (preset.harvest == Harvest.LUCK) {
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
            map.put(Enchantments.BLOCK_FORTUNE, LUCK_FORTUNE[clamp(preset.harvestLevel, LUCK_FORTUNE.length)]);
            EnchantmentHelper.setEnchantments(map, stack);
        }
    }

    private static void applyArea(Preset preset, ServerLevel level, BlockPos origin, ServerPlayer player,
                                  ItemStack stack, Profile profile) {
        int range = HAMMER_RANGE[clamp(preset.areaLevel, HAMMER_RANGE.length)];
        if (preset.area == Area.RECURSION) {
            int radius = RECURSION_RADIUS[clamp(preset.areaLevel, RECURSION_RADIUS.length)];
            Block ref = level.getBlockState(origin).getBlock();
            if (ref == Blocks.STONE || ref == Blocks.NETHERRACK) {
                return;
            }
            java.util.HashSet<BlockPos> seen = new java.util.HashSet<>();
            recurse(level, origin, origin, player, stack, profile, preset, 0, radius, ref, seen);
            return;
        }
        if (preset.area == Area.HAMMER) {
            for (int x = origin.getX() - range; x <= origin.getX() + range; x++) {
                for (int y = origin.getY() - range; y <= origin.getY() + range; y++) {
                    for (int z = origin.getZ() - range; z <= origin.getZ() + range; z++) {
                        BlockPos extra = new BlockPos(x, y, z);
                        if (!extra.equals(origin)) {
                            harvestBlock(level, extra, player, stack, preset, profile, origin, false);
                        }
                    }
                }
            }
            return;
        }
        HitResult hit = player.pick(player.getBlockReach(), 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        Direction face = blockHit.getDirection();
        int xRange = range;
        int yRange = range;
        int zRange = 0;
        if (face.getAxis() == Direction.Axis.Y) {
            yRange = 0;
            zRange = range;
        } else if (face.getAxis() == Direction.Axis.Z) {
            zRange = 0;
        } else {
            xRange = 0;
            zRange = range;
        }
        for (int x = origin.getX() - xRange; x <= origin.getX() + xRange; x++) {
            for (int y = origin.getY() - yRange; y <= origin.getY() + yRange; y++) {
                for (int z = origin.getZ() - zRange; z <= origin.getZ() + zRange; z++) {
                    BlockPos extra = new BlockPos(x, y, z);
                    if (!extra.equals(origin)) {
                        harvestBlock(level, extra, player, stack, preset, profile, origin, false);
                    }
                }
            }
        }
    }

    private static void recurse(ServerLevel level, BlockPos pos, BlockPos origin, ServerPlayer player, ItemStack stack,
                                Profile profile, Preset preset, int depth, int radius, Block ref,
                                java.util.HashSet<BlockPos> seen) {
        List<BlockPos> offsets = new ArrayList<>(26);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        offsets.add(new BlockPos(dx, dy, dz));
                    }
                }
            }
        }
        Collections.shuffle(offsets, new java.util.Random(level.random.nextLong()));
        for (BlockPos offset : offsets) {
            BlockPos next = pos.offset(offset);
            if (!seen.add(next) || next.equals(origin)) {
                continue;
            }
            if (depth + 1 > RECURSION_DEPTH) {
                continue;
            }
            if (Vec3.atCenterOf(next).distanceTo(Vec3.atCenterOf(origin)) > radius) {
                continue;
            }
            if (level.getBlockState(next).getBlock() != ref) {
                continue;
            }
            harvestBlock(level, next, player, stack, preset, profile, origin, false);
            recurse(level, next, origin, player, stack, profile, preset, depth + 1, radius, ref, seen);
        }
    }

    private static void harvestBlock(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack stack,
                                     Preset preset, Profile profile, BlockPos origin, boolean originBreak) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0) {
            return;
        }
        if (!canHarvest(stack, state, profile)) {
            return;
        }
        BlockState refState = level.getBlockState(origin);
        float refSpeed = Math.max(stack.getDestroySpeed(refState), 0.0001F);
        float speed = stack.getDestroySpeed(state);
        if (speed <= 0 || refSpeed / speed > 10.0F) {
            return;
        }
        if (!originBreak) {
            int exp = ForgeHooks.onBlockBreakEvent(level, player.gameMode.getGameModeForPlayer(), player, pos);
            if (exp == -1) {
                return;
            }
        }
        if (preset.harvest == Harvest.SMELTER && smelt(level, pos, player, stack, origin)) {
            return;
        }
        if (!player.getAbilities().instabuild) {
            BlockEntity be = level.getBlockEntity(pos);
            Block.dropResources(state, level, pos, be, player, stack);
            int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack);
            int silk = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0 ? 1 : 0;
            int exp = state.getExpDrop(level, level.random, pos, fortune, silk);
            if (exp > 0) {
                state.getBlock().popExperience(level, pos, exp);
            }
        }
        level.levelEvent(player, 2001, pos, Block.getId(state));
        level.removeBlock(pos, false);
        if (!player.getAbilities().instabuild && stack.isDamageableItem()) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
    }

    private static boolean smelt(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack tool, BlockPos origin) {
        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);
        List<ItemStack> drops = Block.getDrops(state, level, pos, be, player, tool);
        boolean did = false;
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack drop : drops) {
            Optional<SmeltingRecipe> recipe = level.getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, new SimpleContainer(drop), level);
            if (recipe.isPresent()) {
                ItemStack smelted = recipe.get().getResultItem(level.registryAccess()).copy();
                smelted.setCount(smelted.getCount() * drop.getCount());
                out.add(smelted);
                did = true;
            } else {
                out.add(drop);
            }
        }
        if (!did) {
            return false;
        }
        level.levelEvent(player, 2001, pos, Block.getId(state));
        level.removeBlock(pos, false);
        if (!player.getAbilities().instabuild) {
            for (ItemStack drop : out) {
                ItemEntity entity = new ItemEntity(level, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5,
                        drop);
                entity.setDefaultPickUpDelay();
                level.addFreshEntity(entity);
            }
            if (tool.isDamageableItem()) {
                tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            }
        }
        return true;
    }

    private static boolean canHarvest(ItemStack stack, BlockState state, Profile profile) {
        if (profile.silkActive(stack)) {
            return true;
        }
        if (stack.isCorrectToolForDrops(state)) {
            return true;
        }
        return !state.requiresCorrectToolForDrops() && stack.getDestroySpeed(state) > 1.0F;
    }

    private static void applyWeapon(WeaponBind bind, Player player, LivingEntity target) {
        switch (bind.weapon) {
            case STUN -> {
                int seconds = STUN_SECONDS[clamp(bind.level, STUN_SECONDS.length)];
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, seconds * 20, 4));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, seconds * 20, 4));
            }
            case FIRE -> target.setSecondsOnFire(FIRE_SECONDS[clamp(bind.level, FIRE_SECONDS.length)]);
            case VAMPIRE -> {
                if (target.getHealth() <= 0) {
                    return;
                }
                float amount = VAMPIRE_HEAL[clamp(bind.level, VAMPIRE_HEAL.length)];
                target.setHealth(Math.max(0.0F, target.getHealth() - amount));
                if (target.getHealth() <= 0.0F) {
                    target.die(player.damageSources().magic());
                }
                player.heal(amount);
            }
            case BEHEADER -> {
                if (target.getHealth() > 0.0F) {
                    return;
                }
                if (target instanceof WitherSkeleton) {
                    if (target.getRandom().nextInt(20) == 0) {
                        target.spawnAtLocation(new ItemStack(Items.WITHER_SKELETON_SKULL));
                    } else {
                        target.spawnAtLocation(new ItemStack(Items.COAL, 3));
                    }
                } else if (target instanceof Skeleton) {
                    target.spawnAtLocation(new ItemStack(Items.SKELETON_SKULL));
                } else if (target instanceof Zombie) {
                    target.spawnAtLocation(new ItemStack(Items.ZOMBIE_HEAD));
                } else if (target instanceof Creeper) {
                    target.spawnAtLocation(new ItemStack(Items.CREEPER_HEAD));
                } else if (target instanceof MagmaCube) {
                    target.spawnAtLocation(new ItemStack(Items.MAGMA_CREAM, 3));
                } else if (target instanceof Slime) {
                    target.spawnAtLocation(new ItemStack(Items.SLIME_BALL, 3));
                } else if (target instanceof Player victim) {
                    ItemStack head = new ItemStack(Items.PLAYER_HEAD);
                    CompoundTag tag = new CompoundTag();
                    tag.putString("SkullOwner", victim.getGameProfile().getName());
                    head.setTag(tag);
                    target.spawnAtLocation(head);
                } else {
                    target.spawnAtLocation(new ItemStack(Items.ROTTEN_FLESH, 3));
                    target.spawnAtLocation(new ItemStack(Items.BONE, 2));
                }
            }
        }
    }

    private static int clamp(int level, int length) {
        return Math.max(0, Math.min(level, length - 1));
    }
}
