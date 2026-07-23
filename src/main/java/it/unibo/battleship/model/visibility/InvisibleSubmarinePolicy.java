package it.unibo.battleship.model.visibility;

import it.unibo.battleship.model.Ship;
import it.unibo.battleship.model.ShipType;
import java.util.Objects;

/**
 * Excludess the submarine from the sonar.
 */
public final class InvisibleSubmarinePolicy implements ShipVisibilityPolicy {

    private final ShipVisibilityPolicy base;

    /**
     * Creates a policy that decorates the visibility policy by excluding invisible submarines from sonar detection.
     * 
     * @param base the visibility to decorate
     */
    public InvisibleSubmarinePolicy(final ShipVisibilityPolicy base) {
        this.base = Objects.requireNonNull(base, "base");
    }

    @Override
    public boolean isVisible(final Ship ship, final VisibilityContext context) {
        return this.base.isVisible(ship, context);
    }

    @Override
    public boolean isDetectableBySonar(final Ship ship) {
        return ship.type() != ShipType.INVISIBLE_SUBMARINE && this.base.isDetectableBySonar(ship);
    }
}
