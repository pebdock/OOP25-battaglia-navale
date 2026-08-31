package it.unibo.battleship.model.shot;

/**
 * Strategy that fires at two distinct coordinates.
 */
public final class DoubleShotStrategy
        extends AbstractShotStrategy {

    /**
     * Creates a double two-target strategy.
     */
    public DoubleShotStrategy() {
        super(2);
    }
}
