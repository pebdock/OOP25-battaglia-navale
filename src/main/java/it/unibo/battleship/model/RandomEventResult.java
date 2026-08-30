package it.unibo.battleship.model;

import java.util.Objects;

/**
 * Result of a timed random event.
 *
 * @param boardOwner player whose ship moved
 * @param move movement details
 */
public record RandomEventResult(PlayerId boardOwner, ShipMove move) {
    /**
     * Validates event data.
     */
    public RandomEventResult {
        Objects.requireNonNull(boardOwner, "boardOwner");
        Objects.requireNonNull(move, "move");
    }
}
