package com.hbm.energy;

/**
 * 1.7.10 {@code TileEntityPylonBase.ConnectionType}. Wires only join matching types.
 */
public enum PylonConnectionType {
    SINGLE,
    TRIPLE,
    QUAD;

    public String displayName() {
        return switch (this) {
            case SINGLE -> "Single";
            case TRIPLE -> "Triple";
            case QUAD -> "Quadruple";
        };
    }
}
