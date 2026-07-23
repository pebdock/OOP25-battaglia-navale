package it.unibo.battleship.model;

import java.util.Map;
import java.util.Objects;

/**
 * An immutable and filtered projection of a game board.
 * 
 * @param size number of rows and colums of the board
 * @param cells the visible state for each coordinate
 */
public record BoardSnapshot(int size, Map<Coordinate, CellState> cells) {

    /**
     * Creates an immutabke board snapshot after validating its content.
     */
    public BoardSnapshot {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be positive");
        }
        cells = Map.copyOf(Objects.requireNonNull(cells));
    }

    /**
     * Returns the visible state of a coordinate.
     * 
     * @param coordinate the coordinate to check
     * @return the visible state of the coordinate
     */
    public CellState stateAt(final Coordinate coordinate) {
        final CellState state = this.cells.get(Objects.requireNonNull(coordinate, "coordinate"));
        if (state == null) {
            throw new IllegalArgumentException("Coordinare not belonging to this snapshot");
        }
        return state;
    }
}
