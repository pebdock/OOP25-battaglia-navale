package it.unibo.battleship.model.shot;

/**
 * Two coordinates shot.
 */
public final class DoubleShotStrategy implements ShotStrategy {

    @Override
    public int requiredTargets() {
        return 1;
    }

}
