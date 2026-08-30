package it.unibo.battleship.controller;

import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.Rotation;
import it.unibo.battleship.model.ShipType;
import it.unibo.battleship.view.BattleshipView;

/**
 * Mediates user actions from the view and the game model.
 */
public interface GameController {

    /**
     * Connects the graphical view that receives updates.
     *
     * @param view the view
     */
    void attachView(BattleshipView view);

    /**
     * Starts manual fleet placement for both harbors.
     *
     * @param firstHarbor name of the first harbor
     * @param secondHarbor name of the second harbor
     */
    void startPlacement(String firstHarbor, String secondHarbor);

    /**
     * Places the selected ship on the current placement board.
     *
     * @param origin first cell of the ship
     * @param type ship type
     * @param rotation orientation
     */
    void placeShip(Coordinate origin, ShipType type, Rotation rotation);

    /**
     * Clears the fleet of the player currently placing ships.
     */
    void resetCurrentFleet();

    /**
     * Confirms the current fleet and continues placement or starts the match.
     */
    void confirmCurrentFleet();

    /**
     * Handles a click on the opponent board during a match.
     *
     * @param target clicked cell
     * @param mode selected action
     * @param direction direction for a sequential shot
     */
    void handleTarget(Coordinate target, ActionMode mode, ShotDirection direction);

    /**
     * Clears a half-selected double shot when the action mode changes.
     */
    void actionModeChanged();

    /**
     * Returns to the setup screen and discards the current match.
     */
    void returnToSetup();

    /**
     * Applies one random storm event if a match is in progress.
     */
    void onRandomEventTick();
}
