package com.hbm.tileentity.network;

import com.hbm.lib.RefStrings;
import com.hbm.util.NoteBuilder;
import com.hbm.util.NoteBuilder.Instrument;
import com.hbm.util.NoteBuilder.Note;
import com.hbm.util.NoteBuilder.Octave;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;

/**
 * 1.7 {@code RTTYSystem}: one-tick-delayed radio channels, plus the built-in Song of Storms test sender.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class RTTYSystem {
    private static final Map<Key, RTTYChannel> BROADCAST = new HashMap<>();
    private static final Map<Key, Object> NEW_MESSAGES = new HashMap<>();

    private RTTYSystem() {
    }

    public static void broadcast(Level level, String channelName, Object signal) {
        Key id = new Key(level, channelName);
        if (isNumber(signal) && NEW_MESSAGES.containsKey(id)) {
            Object existing = NEW_MESSAGES.get(id);
            if (isNumber(existing)) {
                try {
                    long first = Long.parseLong(String.valueOf(signal));
                    long second = Long.parseLong(String.valueOf(existing));
                    NEW_MESSAGES.put(id, Long.toString(first + second));
                    return;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        NEW_MESSAGES.put(id, signal);
    }

    public static RTTYChannel listen(Level level, String channelName) {
        return BROADCAST.get(new Key(level, channelName));
    }

    public static void updateBroadcastQueue() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        for (Map.Entry<Key, Object> entry : NEW_MESSAGES.entrySet()) {
            Key id = entry.getKey();
            RTTYChannel channel = new RTTYChannel();
            channel.timeStamp = id.level().getGameTime();
            channel.signal = entry.getValue();
            BROADCAST.put(id, channel);
        }

        for (ServerLevel level : server.getAllLevels()) {
            RTTYChannel chan = new RTTYChannel();
            chan.timeStamp = level.getGameTime();
            chan.signal = getTestSender(chan.timeStamp);
            BROADCAST.put(new Key(level, "2012-08-06"), chan);
        }

        NEW_MESSAGES.clear();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            updateBroadcastQueue();
        }
    }

    public static final class RTTYChannel {
        public long timeStamp = -1L;
        public Object signal;
    }

    private record Key(Level level, String channel) {
    }

    private static boolean isNumber(Object signal) {
        if (signal == null) {
            return false;
        }
        try {
            Long.parseLong(String.valueOf(signal));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Song of Storms at 300 BPM (1.7 {@code RTTYSystem.getTestSender}). */
    public static Object getTestSender(long timeStamp) {
        int tempo = 4;
        int time = (int) (timeStamp % (tempo * 160L));

        Instrument flute = Instrument.PIANO;
        Instrument accordion = Instrument.BASSGUITAR;

        if (time == tempo * 0) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
        if (time == tempo * 2) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();
        if (time == tempo * 4) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

        if (time == tempo * 6) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
        if (time == tempo * 8) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).end();

        if (time == tempo * 12) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).end();
        if (time == tempo * 14) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).end();
        if (time == tempo * 16) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).end();

        if (time == tempo * 18) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
        if (time == tempo * 20) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).end();

        if (time == tempo * 24) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
        if (time == tempo * 26) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();
        if (time == tempo * 28) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

        if (time == tempo * 30) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
        if (time == tempo * 32) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).end();

        if (time == tempo * 36) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).end();
        if (time == tempo * 38) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();
        if (time == tempo * 40) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

        if (time == tempo * 42) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
        if (time == tempo * 44) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).end();

        if (time == tempo * 48) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 50) return NoteBuilder.start().add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 52) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(flute, Note.D, Octave.MID).end();
        if (time == tempo * 54) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

        if (time == tempo * 56) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 58) return NoteBuilder.start().add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 60) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).add(flute, Note.D, Octave.MID).end();

        if (time == tempo * 64) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(flute, Note.E, Octave.MID).end();
        if (time == tempo * 66) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).end();
        if (time == tempo * 67) return NoteBuilder.start().add(flute, Note.F, Octave.MID).end();
        if (time == tempo * 68) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).add(flute, Note.E, Octave.MID).end();
        if (time == tempo * 69) return NoteBuilder.start().add(flute, Note.F, Octave.MID).end();

        if (time == tempo * 70) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.E, Octave.MID).end();
        if (time == tempo * 71) return NoteBuilder.start().add(flute, Note.B, Octave.MID).end();
        if (time == tempo * 72) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).add(flute, Note.A, Octave.MID).end();

        if (time == tempo * 76) return NoteBuilder.start().add(accordion, Note.G, Octave.LOW).add(flute, Note.A, Octave.MID).end();
        if (time == tempo * 78) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 80) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 81) return NoteBuilder.start().add(flute, Note.G, Octave.MID).end();

        if (time == tempo * 82) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(flute, Note.A, Octave.MID).end();
        if (time == tempo * 84) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

        if (time == tempo * 88) return NoteBuilder.start().add(accordion, Note.G, Octave.LOW).add(flute, Note.A, Octave.MID).end();
        if (time == tempo * 90) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 92) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 93) return NoteBuilder.start().add(accordion, Note.B, Octave.MID).add(flute, Note.G, Octave.MID).end();

        if (time == tempo * 94) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(flute, Note.E, Octave.LOW).end();
        if (time == tempo * 96) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

        if (time == tempo * 100) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 101) return NoteBuilder.start().add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 102) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.D, Octave.MID).end();
        if (time == tempo * 104) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).end();

        if (time == tempo * 106) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 107) return NoteBuilder.start().add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 108) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.D, Octave.MID).end();

        if (time == tempo * 112) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(flute, Note.E, Octave.MID).end();
        if (time == tempo * 114) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).end();
        if (time == tempo * 115) return NoteBuilder.start().add(flute, Note.F, Octave.MID).end();
        if (time == tempo * 116) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).add(flute, Note.E, Octave.MID).end();
        if (time == tempo * 117) return NoteBuilder.start().add(flute, Note.F, Octave.MID).end();

        if (time == tempo * 118) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.E, Octave.MID).end();
        if (time == tempo * 119) return NoteBuilder.start().add(flute, Note.C, Octave.MID).end();
        if (time == tempo * 120) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.A, Octave.MID).end();

        if (time == tempo * 124) return NoteBuilder.start().add(accordion, Note.G, Octave.LOW).add(flute, Note.A, Octave.MID).end();
        if (time == tempo * 126) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.MID).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 128) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.MID).add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 129) return NoteBuilder.start().add(flute, Note.G, Octave.MID).end();

        if (time == tempo * 130) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(flute, Note.A, Octave.MID).end();
        if (time == tempo * 132) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(accordion, Note.E, Octave.LOW).add(accordion, Note.A, Octave.MID).add(accordion, Note.G, Octave.LOW).end();
        if (time == tempo * 134) return NoteBuilder.start().add(flute, Note.A, Octave.MID).end();

        if (time == tempo * 136) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 138) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.MID).end();
        if (time == tempo * 140) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.MID).end();

        return "";
    }
}
