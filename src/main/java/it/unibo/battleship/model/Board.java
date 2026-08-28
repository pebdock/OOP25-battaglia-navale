package it.unibo.battleship.model;

import it.unibo.battleship.model.visibility.ShipVisibilityPolicy;

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Contract for placements, shots, win detectionc and safe protections.
 */
public interface Board {

    /**
     * Returns the size of the board.
     * 
     * @return number of rows and columns
     */
    int size();

    /**
     * Places a ship on the board.
     * 
     * @param ship the ship to place on the board
     */
    void placeShip(Ship ship);

    /**
     * Shoot one or more specified coordinates.
     * 
     * @param targets the specified coordinates
     * @return the results in the same order as the targets
     */
    List<ShotResult> fireAt(List<Coordinate> targets);

    /**
     * Checks if the board contains at least one ship.
     * 
     * @return true if the board contains a ship
     */
    boolean hasShips();

    /**
     * Checks if the board contains the complete fleet.
     * 
     * @return true if the fleet is complete
     */
    boolean hasCompleteFleet();

    /**
     * Checks if every ship on the board is sunk.
     * 
     * @return true if all ships are sunk
     */
    boolean allShipsSunk();

    /**
     * Scans the 3x3 square around a coordinate.
     * 
     * @param center the center of the scanned area
     * @param visibilityPolicy the policy used for sonar detection
     * @return the number of detectable ship cells
     */
    int scan3x3(Coordinate center, ShipVisibilityPolicy visibilityPolicy);

    /**
     * Creates a safe projection of the board for a viewer.
     * 
     * @param owner the owner of the board
     * @param viewer the player viewing the board
     * @param visibilityPolicy the policy used to filter the ships
     * @return an immutable and filtered board snapshot
     */
    BoardSnapshot snapshot(PlayerId owner, PlayerId viewer, ShipVisibilityPolicy visibilityPolicy);

    /**
     * Moves one randomly selected non-sunken ship to a valid free position.
     * Existing damage travels with the ship.
     *
     * @param random source of randomness
     * @return details of the move, or empty when no ship can be moved
     */
    Optional<ShipMove> moveRandomUnsunkShip(RandomGenerator random);
}
