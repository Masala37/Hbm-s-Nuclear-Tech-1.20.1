package com.hbm.interfaces;

/**
 * 1.7.10 {@code HalfLifeType} units used by {@link com.hbm.util.RTGUtil#getLifespan}.
 */
public enum HalfLifeType {
    /** Counted in days. */
    SHORT,
    /** Counted in years. */
    MEDIUM,
    /** Counted in hundreds of years. */
    LONG
}
