package it.unibo.battleship.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertEquals;

// import it.unibo.battleship.model.visibility.InvisibleSubmarinePolicy;
// import it.unibo.battleship.model.visibility.OwnerOnlyVisibilityPolicy;

import java.util.List;
// import java.util.Set;

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
}
