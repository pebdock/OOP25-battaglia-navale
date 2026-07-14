package it.unibo.battleship.model;

import java.util.Objects;

/**
 * Immutable identifier for a ship.
 * 
 * @param value the value of the ship ID
 */
public record ShipId(String value) {

    /**
     * Compact constructor that builds a ShipId.
     * 
     * @param value the value of the ship ID
     */
    public ShipId {
        Objects.requireNonNull(value, "ShipId value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ShipId value cannot be blank");
        }
    } 
}
