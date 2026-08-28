package it.unibo.battleship.model;

import java.util.List;
import java.util.Optional;

/**
 * Defines operations to control and view a game.
 */
public interface Game {

    /**
     * Starts the game.
     */
    void start();

    /**
     * Plays a shot action for the current player.
     * 
     * @param kind the type of shot
     * @param targets coordinates of the target
     * @return the result of the completed turn
     */
    TurnResult playTurn(ShotKind kind, List<Coordinate> targets);

    /**
     * Uses the sonar.
     * 
     * @param center the center of the scanned area
     * @return the result of the sonar scan
     */
    SonarResult useSonar(Coordinate center);

    /**
     * Creates a view of the game to the chosen player.
     * 
     * @param viewer the player viewing the game
     * @return the game snaphot visible to the player
     */
    GameSnapshot snapshotFor(PlayerId viewer);

    /**
     * Tells the current phase of the game.
     * 
     * @return the current game phase
     */
    GamePhase phase();

    /**
     * Tells the player of the current turn.
     * 
     * @return the current player
     */
    PlayerId currentPlayer();

    /**
     * Returns the winner if the game has finished.
     * 
     * @return an optional containing the winner oe empty
     */
    Optional<PlayerId> winner();

    /**
     * Triggers one random event that moves a non-sunken ship.
     *
     * @return event details, or empty when no valid movement is available
     */
    Optional<RandomEventResult> triggerRandomEvent();
}
