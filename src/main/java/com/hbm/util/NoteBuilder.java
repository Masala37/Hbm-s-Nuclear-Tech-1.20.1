package com.hbm.util;

import java.util.ArrayList;
import java.util.List;

/** 1.7 {@code NoteBuilder} — compact RTTY note packets for the FM radio. */
public final class NoteBuilder {
    private String beat = "";

    private NoteBuilder() {
    }

    public static NoteBuilder start() {
        return new NoteBuilder();
    }

    public NoteBuilder add(Instrument instrument, Note note, Octave octave) {
        if (!beat.isEmpty()) {
            beat += "-";
        }
        beat += instrument.ordinal() + ":" + note.ordinal() + ":" + octave.ordinal();
        return this;
    }

    public String end() {
        return beat;
    }

    public static Hit[] translate(String beat) {
        if (beat == null || beat.isEmpty()) {
            return new Hit[0];
        }
        String[] hits = beat.split("-");
        List<Hit> notes = new ArrayList<>();
        try {
            for (String hit : hits) {
                String[] components = hit.split(":");
                Instrument instrument = Instrument.values()[Integer.parseInt(components[0])];
                Note note = Note.values()[Integer.parseInt(components[1])];
                Octave octave = Octave.values()[Integer.parseInt(components[2])];
                notes.add(new Hit(instrument, note, octave));
            }
            return notes.toArray(Hit[]::new);
        } catch (Exception ignored) {
            return new Hit[0];
        }
    }

    public record Hit(Instrument instrument, Note note, Octave octave) {
    }

    public enum Instrument {
        PIANO,
        BASSDRUM,
        SNARE,
        CLICKS,
        BASSGUITAR
    }

    public enum Note {
        F_SHARP,
        G,
        G_SHARP,
        A,
        A_SHARP,
        B,
        C,
        C_SHARP,
        D,
        D_SHARP,
        E,
        F
    }

    public enum Octave {
        LOW, MID, HIGH
    }
}
