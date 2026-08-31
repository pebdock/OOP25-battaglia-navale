package it.unibo.battleship.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable quantities required for a complete fleet. 
 *
 * @param quantities number of ships.
 */
public record FleetRules(Map<ShipType, Integer> quantities) {

    /**
     * Constructor of fleet rules.
     */
    public FleetRules {
        Objects.requireNonNull(quantities, "quantities");
        for (final Map.Entry<ShipType, Integer> entry : quantities.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "ship type");
            Objects.requireNonNull(entry.getValue(), "quantity for" + entry.getKey());
        }

        final Map<ShipType, Integer> copy = new EnumMap<>(ShipType.class);

        for (final ShipType type : ShipType.values()) {
            final int quantity = quantities.getOrDefault(type, 0);

            if (quantity < 0) {
                throw new IllegalArgumentException("Negative quantity for " + type);
            }

            copy.put(type, quantity);
        }

        quantities = Collections.unmodifiableMap(copy);
    }

    /**
     * Returns the classic fleet without optional armored ship.
     * 
     * @return the immutable classic fleet rules
     */
    public static FleetRules classic() {
        final Map<ShipType, Integer> quantities = new EnumMap<>(ShipType.class);
        quantities.put(ShipType.FLAGSHIP, 1);
        quantities.put(ShipType.BATTLESHIP, 1);
        quantities.put(ShipType.CRUISER, 2);
        quantities.put(ShipType.INVISIBLE_SUBMARINE, 1);
        quantities.put(ShipType.DESTROYER, 2);
        quantities.put(ShipType.RECON, 1);
        return new FleetRules(quantities);
    }

    /**
     * Returns the classic fleet + armored ship.
     * 
     * @return immutable armored fleet rules
     */
    public static FleetRules withArmoredShip() {
        final Map<ShipType, Integer> quantities = new EnumMap<>(ShipType.class);
        quantities.putAll(classic().quantities());
        quantities.put(ShipType.ARMORED_SHIP, 1);
        return new FleetRules(quantities);
    }

    /**
     * Returns required quantity of ship type.
     * 
     * @param type the ship type
     * @return the configured quantity
     */
    public int quantity(final ShipType type) {
        return this.quantities.get(
            Objects.requireNonNull(type, "type")
        );
    }

    /**
     * Returns the total number of ships required by these rules.
     *
     * @return the total required number of ships
     */
    public int totalShips() {
        return this.quantities.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
    }
}
