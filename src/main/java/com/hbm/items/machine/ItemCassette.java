package com.hbm.items.machine;

import com.hbm.registry.ModItems;
import com.hbm.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Siren track cassette. Track ordinals match 1.7.10 {@code ItemCassette} metadata.
 */
public class ItemCassette extends Item {
    public static final String TAG_TYPE = "type";

    public enum SoundType {
        LOOP,
        PASS,
        SOUND
    }

    public enum TrackType {
        NULL(" ", null, SoundType.SOUND, 0, 0),
        HATCH("Hatch Siren", "alarm.hatch", SoundType.LOOP, 3358839, 250),
        ATUOPILOT("Autopilot Disconnected", "alarm.autopilot", SoundType.LOOP, 11908533, 50),
        AMS_SIREN("AMS Siren", "alarm.ams_siren", SoundType.LOOP, 15055698, 50),
        BLAST_DOOR("Blast Door Alarm", "alarm.blast_door_alarm", SoundType.LOOP, 11665408, 50),
        APC_LOOP("APC Siren", "alarm.apc_loop", SoundType.LOOP, 3565216, 50),
        KLAXON("Klaxon", "alarm.klaxon", SoundType.LOOP, 8421504, 50),
        KLAXON_A("Vault Door Alarm", "alarm.fo_klaxon_a", SoundType.LOOP, 0x8c810b, 50),
        KLAXON_B("Security Alert", "alarm.fo_klaxon_b", SoundType.LOOP, 0x76818e, 50),
        SIREN("Standard Siren", "alarm.regular_siren", SoundType.LOOP, 6684672, 100),
        CLASSIC("Classic Siren", "alarm.classic", SoundType.LOOP, 0xc0cfe8, 100),
        BANK_ALARM("Bank Alarm", "alarm.bank_alarm", SoundType.LOOP, 3572962, 100),
        BEEP_SIREN("Beep Siren", "alarm.beep_siren", SoundType.LOOP, 13882323, 100),
        CONTAINER_ALARM("Container Alarm", "alarm.container_alarm", SoundType.LOOP, 14727839, 100),
        SWEEP_SIREN("Sweep Siren", "alarm.sweep_siren", SoundType.LOOP, 15592026, 500),
        STRIDER_SIREN("Missile Silo Siren", "alarm.strider_siren", SoundType.LOOP, 11250586, 500),
        AIR_RAID("Air Raid Siren", "alarm.air_raid", SoundType.LOOP, 0xDF3795, 500),
        NOSTROMO_SIREN("Nostromo Self Destruct", "alarm.nostromo_siren", SoundType.LOOP, 0x5dd800, 100),
        EAS_ALARM("EAS Alarm Screech", "alarm.eas_alarm", SoundType.LOOP, 0xb3a8c1, 50),
        APC_PASS("APC Pass", "alarm.apc_pass", SoundType.PASS, 3422163, 50),
        RAZORTRAIN("Razortrain Horn", "alarm.razortrain_horn", SoundType.SOUND, 7819501, 250);

        private final String title;
        private final String soundPath;
        private final SoundType type;
        private final int color;
        private final int volume;

        TrackType(String title, String soundPath, SoundType type, int color, int volume) {
            this.title = title;
            this.soundPath = soundPath;
            this.type = type;
            this.color = color;
            this.volume = volume;
        }

        public String getTrackTitle() {
            return title;
        }

        public String getSoundPath() {
            return soundPath;
        }

        public SoundEvent getSoundEvent() {
            if (soundPath == null) {
                return null;
            }
            return ModSounds.require(soundPath);
        }

        public SoundType getType() {
            return type;
        }

        public int getColor() {
            return color;
        }

        public int getVolume() {
            return volume;
        }

        public static TrackType getEnum(int id) {
            TrackType[] values = values();
            if (id >= 0 && id < values.length) {
                return values[id];
            }
            return NULL;
        }
    }

    public ItemCassette() {
        super(new Item.Properties().stacksTo(1));
    }

    public static ItemStack stack(TrackType type) {
        ItemStack stack = new ItemStack(ModItems.SIREN_TRACK.get());
        setType(stack, type);
        return stack;
    }

    public static void setType(ItemStack stack, TrackType type) {
        stack.getOrCreateTag().putInt(TAG_TYPE, type.ordinal());
    }

    public static TrackType getType(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemCassette)) {
            return TrackType.NULL;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return TrackType.NULL;
        }
        if (tag.contains(TAG_TYPE)) {
            return TrackType.getEnum(tag.getInt(TAG_TYPE));
        }
        if (tag.contains("Damage")) {
            return TrackType.getEnum(tag.getInt("Damage"));
        }
        return TrackType.NULL;
    }

    public static int overlayColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) {
            return 0xFFFFFFFF;
        }
        int color = getType(stack).getColor();
        if (color < 0) {
            return 0xFFFFFFFF;
        }
        return 0xFF000000 | color;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        TrackType type = getType(stack);
        tooltip.add(Component.literal("Siren sound cassette:"));
        tooltip.add(Component.literal("   Name: " + type.getTrackTitle()));
        tooltip.add(Component.literal("   Type: " + type.getType().name()));
        tooltip.add(Component.literal("   Volume: " + type.getVolume()));
    }
}
