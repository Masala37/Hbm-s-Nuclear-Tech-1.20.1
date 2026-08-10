package com.hbm.hazard;

import com.hbm.hazard.type.HazardTypeBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Hazard table for an item/block (legacy {@code HazardData}).
 */
public class HazardData {

    boolean doesOverride = false;
    final List<HazardEntry> entries = new ArrayList<>();

    public HazardData addEntry(HazardTypeBase hazard) {
        return addEntry(hazard, 1.0F, false);
    }

    public HazardData addEntry(HazardTypeBase hazard, float level) {
        return addEntry(hazard, level, false);
    }

    public HazardData addEntry(HazardTypeBase hazard, float level, boolean override) {
        entries.add(new HazardEntry(hazard, level));
        this.doesOverride = override;
        return this;
    }

    public HazardData addEntry(HazardEntry entry) {
        entries.add(entry);
        return this;
    }

    public boolean doesOverride() {
        return doesOverride;
    }

    public List<HazardEntry> getEntries() {
        return entries;
    }
}
