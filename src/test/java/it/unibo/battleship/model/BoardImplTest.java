package it.unibo.battleship.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertEquals;

// import it.unibo.battleship.model.visibility.InvisibleSubmarinePolicy;
// import it.unibo.battleship.model.visibility.OwnerOnlyVisibilityPolicy;

import java.util.List;
import java.util.Set;

/**
 * Tests teh board implementation.
 */
class BoardImplTest {

    /**
     * Checks if firing, positioning and outcomes work correctly.
     */
    @Test
    void checkMissHitSunk() {
        final Board board = new BoardImpl(10);
        board.placeShip(Ship.place(
            new ShipId("distr"),
            ShipType.DESTROYER,
            new Coordinate(1, 1),
            Rotation.DEGREES_0
        ));

        assertEquals(ShotOutcome.MISS, board.fireAt(List.of(new Coordinate(0, 0))).getFirst().outcome());
        assertEquals(ShotOutcome.HIT, board.fireAt(List.of(new Coordinate(1, 1))).getFirst().outcome());
        assertEquals(ShotOutcome.SUNK, board.fireAt(List.of(new Coordinate(1, 2))).getFirst().outcome());
    }

    /**
     * Tests if the l shape remains if rotated.
     */
    @Test
    void reconLShapeRotation() {
        final Ship ship = Ship.place(
            new ShipId("recon"),
            ShipType.RECONNAISSANCE,
            new Coordinate(4, 4),
            Rotation.DEGREES_90
        );

        final int lValue = 5;

        assertEquals(Set.of(
            new Coordinate(4, lValue),
            new Coordinate(lValue, lValue),
            new Coordinate(lValue, 4)
        ), ship.cells());
    }
}
