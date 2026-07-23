package it.unibo.battleship.model.visibility;

import it.unibo.battleship.model.Ship;

/**
 * Intact ships visible only to the owner.
 */
public final class OwnerOnlyVisibilityPolicy implements ShipVisibilityPolicy {

    @Override
    public boolean isVisible(final Ship ship, final VisibilityContext context) {
        return context.owner() == context.viewer(); // verifies that owner is the same as the viewer
    }
}
