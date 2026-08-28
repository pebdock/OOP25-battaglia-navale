package it.unibo.battleship.model;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable and filtered projection of a game board.
 * 
 * @param size number of rows and colums of the board
 * @param cells the visible state for each coordinate
 * @param visibleShipTypes types of intact ships visible to the board owner
 */
public record BoardSnapshot(
    int size,
    Map<Coordinate, CellState> cells,
    Map<Coordinate, ShipType> visibleShipTypes
) {

    /**
     * Creates an immutable board snapshot after validating its content.
     */
    public BoardSnapshot {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be positive");
        }
        cells = Map.copyOf(Objects.requireNonNull(cells, "cells"));
        visibleShipTypes = Map.copyOf(Objects.requireNonNull(visibleShipTypes, "visibleShipTypes"));
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
            throw new IllegalArgumentException("Coordinate not belonging to this snapshot");
        }
        return state;
    }

    /**
     * Returns the type of an intact ship only when it is visible to this snapshot viewer.
     *
     * @param coordinate the coordinate to check
     * @return the ship type when it can be safely shown, otherwise an empty optional
     */
    public Optional<ShipType> shipTypeAt(final Coordinate coordinate) {
        Objects.requireNonNull(coordinate, "coordinate");
        if (!this.cells.containsKey(coordinate)) {
            throw new IllegalArgumentException("Coordinate not belonging to this snapshot");
        }
        return Optional.ofNullable(this.visibleShipTypes.get(coordinate));
    }
}
