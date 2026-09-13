package api.hbm.tile;

/**
 * Heat source used by fireboxes, heaters, and anything a boiler can sit on.
 * Heat is not Forge Energy.
 */
public interface IHeatSource {
    int getHeatStored();

    /**
     * Removes heat. Implementations must not go negative.
     */
    void useUpHeat(int heat);
}
