package it.unibo.battleship.model;

import java.util.Objects;
import java.util.Set;

/**
 * Immutable description of a random ship movement.
 *
 * @param shipId moved ship identifier
 * @param shipType moved ship type
 * @param from previous occupied cells
 * @param to new occupied cells
 * @param damageCount damage preserved during movement
 */
public record ShipMove(
    ShipId shipId,
    ShipType shipType,
    Set<Coordinate> from,
    Set<Coordinate> to,
    int damageCount
) {
    /**
     * Validates and copies movement data.
     */
    public ShipMove {
        Objects.requireNonNull(shipId, "shipId");
        Objects.requireNonNull(shipType, "shipType");
        from = Set.copyOf(Objects.requireNonNull(from, "from"));
        to = Set.copyOf(Objects.requireNonNull(to, "to"));
        if (from.isEmpty() || to.isEmpty() || damageCount < 0) {
            throw new IllegalArgumentException("Invalid ship movement");
        }
    }
}
