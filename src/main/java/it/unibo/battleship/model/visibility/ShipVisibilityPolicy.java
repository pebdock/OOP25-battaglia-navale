package it.unibo.battleship.model.visibility;

import it.unibo.battleship.model.Ship;

/**
 * Rules to make a ship visible.
 */
@FunctionalInterface
public interface ShipVisibilityPolicy {

    /**
     * Tells if a non sunken ship is visible.
     * 
     * @param ship the selected ship
     * @param context viewer and owner
     * @return true if the cell can be shown as SHIP, false if the cell should be UNKNOWN
     */
    boolean isVisible(Ship ship, VisibilityContext context);

    /**
     * Tells if a ship can be detected with the sonar.
     * 
     * @param ship the selected ship
     * @return true by default (normally all the ships are revealed by the sonar except invisible ones)
     */
    default boolean isDetectableBySonar(final Ship ship) {
        return true;
    }
}
