package it.unibo.battleship.model.shot;

/**
 * One coordinate shot.
 */
public final class NormalShotStrategy implements ShotStrategy {

    @Override
    public int requiredTargets() {
        return 1;
    }

}
