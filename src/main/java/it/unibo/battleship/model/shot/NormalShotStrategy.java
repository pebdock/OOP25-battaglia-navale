package it.unibo.battleship.model.shot;

/**
 * Strategy that fires at one coordinate.
 */
public final class NormalShotStrategy
        extends AbstractShotStrategy {

    /**
     * Creates a normal one-target strategy.
     */
    public NormalShotStrategy() {
        super(1);
    }
}
