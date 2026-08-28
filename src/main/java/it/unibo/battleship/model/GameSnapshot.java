package it.unibo.battleship.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable view of the game for a player.
 * 
 * @param phase current phase of the game
 * @param viewer the player viewing the game
 * @param currentPlayer the player of the current turn
 * @param winner the winner if the game has finshed
 * @param ownBoard the viewer's board
 * @param opponentBoard the opponent's visible board
 * @param doubleShot the status of the double shot of the viewer
 * @param sonarAvailable checks if the viewer can still use the sonar
 * @param sequentialShotAvailable checks if the viewer can still use the sequential shot
 */
public record GameSnapshot(
    GamePhase phase,
    PlayerId viewer,
    PlayerId currentPlayer,
    Optional<PlayerId> winner,
    BoardSnapshot ownBoard,
    BoardSnapshot opponentBoard,
    DoubleShotStatus doubleShot,
    boolean sonarAvailable,
    boolean sequentialShotAvailables
) {

    /**
     * Checks that all the components aren't null.
     */
    public GameSnapshot {
        Objects.requireNonNull(phase, "phase");
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(currentPlayer, "currentPlayer");
        Objects.requireNonNull(winner, "winner");
        Objects.requireNonNull(ownBoard, "ownBoard");
        Objects.requireNonNull(opponentBoard, "opponentBoard");
        Objects.requireNonNull(doubleShot, "doubleShot");
    }
}
