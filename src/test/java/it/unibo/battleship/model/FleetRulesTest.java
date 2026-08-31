package it.unibo.battleship.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests validation immutability and standard fleet configurations.
 */
class FleetRulesTest {

    /**
     * Verifies quantities of the classic fleet.
     */
    @Test
    void fleetExpectedQuantities() {
        final FleetRules rules = FleetRules.classic();

        assertEquals(1, rules.quantity(ShipType.FLAGSHIP));
        assertEquals(1, rules.quantity(ShipType.BATTLESHIP));
        assertEquals(2, rules.quantity(ShipType.CRUISER));
        assertEquals(
            1,
            rules.quantity(ShipType.INVISIBLE_SUBMARINE)
        );
        assertEquals(2, rules.quantity(ShipType.DESTROYER));
        assertEquals(1, rules.quantity(ShipType.RECON));
        assertEquals(0, rules.quantity(ShipType.ARMORED_SHIP));
        assertEquals(8, rules.totalShips());
    }

    /**
     * Verifies that the armored configuration adds only one armored ship.
     */
    @Test
    void armoredFleetAddsArmoredOnly() {
        final FleetRules classic = FleetRules.classic();
        final FleetRules armored = FleetRules.withArmoredShip();

        for (final ShipType type : ShipType.values()) {
            if (type == ShipType.ARMORED_SHIP) {
                assertEquals(1, armored.quantity(type));
            } else {
                assertEquals(
                    classic.quantity(type),
                    armored.quantity(type)
                );
            }
        }

        final int numExpected = 9;
        assertEquals(numExpected, armored.totalShips());
    }

    /**
     * Verifies that the quantities exposed by the record are immutable.
     */
    @Test
    void noEditSize() {
        final FleetRules rules = FleetRules.classic();

        assertThrows(
            UnsupportedOperationException.class,
            () -> rules.quantities().put(ShipType.FLAGSHIP, 10)
        );
    }

    /**
     * Verifies that the constructor makes a defensive copy.
     */
    @Test
    void constructorMakesCopy() {
        final Map<ShipType, Integer> source = new EnumMap<>(ShipType.class);
        source.put(ShipType.FLAGSHIP, 1);

        final FleetRules rules = new FleetRules(source);
        final int num = 5;
        source.put(ShipType.FLAGSHIP, num);

        assertEquals(1, rules.quantity(ShipType.FLAGSHIP));
    }

    /**
     * Verifies that negative quantities are rejected.
     */
    @Test
    void negativeQuantityIsRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new FleetRules(
                Map.of(ShipType.FLAGSHIP, -1)
            )
        );
    }

    /**
     * Verifies that a null map is rejected.
     */
    @Test
    void nullMapIsRejected() {
        assertThrows(
            NullPointerException.class,
            () -> new FleetRules(null)
        );
    }
}
