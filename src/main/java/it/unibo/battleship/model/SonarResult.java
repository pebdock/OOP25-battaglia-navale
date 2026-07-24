package it.unibo.battleship.model;

import java.util.Objects;

/**
 * Result of the accepted sonar action.
 * 
 * @param player the player using the sonar
 * @param center the center of the sonar scan
 * @param detectedCells number of occupied cells in 3x3 area scanned
 * @param nextPlayer the player of the next turn
 */
public record SonarResult(PlayerId player, Coordinate center, int detectedCells, PlayerId nextPlayer) {

    private static final int SONAR_SIDE_LENGTH = 3;
    private static final int MAX_DETECTED_CELLS = SONAR_SIDE_LENGTH * SONAR_SIDE_LENGTH;

    /**
     * Verifies if the input values are correct.
     */
    public SonarResult {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(center, "center");
        Objects.requireNonNull(nextPlayer, "nextPlayer");
        if (detectedCells < 0 || detectedCells > MAX_DETECTED_CELLS) {
            throw new IllegalArgumentException("Detected cells must be between 0 and 9");
        }
        if (player == nextPlayer) {
            throw new IllegalArgumentException("A sonar action passes the turn to the opponent");
        }
    }
}
