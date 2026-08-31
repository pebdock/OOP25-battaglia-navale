package it.unibo.battleship.view;

import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.Rotation;
import it.unibo.battleship.model.ShipType;

/**
 * Receives semantic user actions emitted by a Battleship view.
 */
public interface BattleshipViewObserver {

    /**
     * Starts placement for two fleets.
     *
     * @param firstHarbor first fleet name
     * @param secondHarbor second fleet name
     * @param useArmoredShip whether both fleets include the armored ship
     */
    void onSetupSubmitted(
        String firstHarbor,
        String secondHarbor,
        boolean useArmoredShip
    );

    /**
     * Requests placement of one ship.
     *
     * @param origin selected origin
     * @param type selected ship type
     * @param rotation selected rotation
     */
    void onShipPlacementRequested(
        Coordinate origin,
        ShipType type,
        Rotation rotation
    );

    /**
     * Requests the reset of the current fleet.
     */
    void onFleetResetRequested();

    /**
     * Confirms the current fleet.
     */
    void onFleetConfirmed();

    /**
     * Requests a normal shot.
     *
     * @param target selected target
     */
    void onNormalShotRequested(Coordinate target);

    /**
     * Selects one target for a double shot.
     *
     * @param target selected target
     */
    void onDoubleShotTargetSelected(Coordinate target);

    /**
     * Requests a sequential shot.
     *
     * @param start first target
     * @param direction selected direction
     */
    void onSequentialShotRequested(
        Coordinate start,
        ShotDirection direction
    );

    /**
     * Requests a sonar scan.
     *
     * @param center center of the scan
     */
    void onSonarRequested(Coordinate center);

    /**
     * Reports that the selected action changed.
     */
    void onActionSelectionChanged();

    /**
     * Requests a return to the setup screen.
     */
    void onNewGameRequested();

    /**
     * Reports that the random-event timer elapsed.
     */
    void onRandomEventElapsed();
}
