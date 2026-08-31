package it.unibo.battleship.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import it.unibo.battleship.model.visibility.InvisibleSubmarinePolicy;
import it.unibo.battleship.model.visibility.OwnerOnlyVisibilityPolicy;

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
        final Board board = new BoardImpl(10, FleetRules.classic());
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
            ShipType.RECON,
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

    /**
     * Check that when one of a group of shots is invalid, all the shots are invalid.
     */
    @Test
    void checkValidShots() {
        final Board board = new BoardImpl(10, FleetRules.classic());
        board.placeShip(Ship.place(
            new ShipId("sub"),
            ShipType.INVISIBLE_SUBMARINE,
            new Coordinate(3, 3),
            Rotation.DEGREES_0
        ));
        board.fireAt(List.of(new Coordinate(0, 0)));

        final GameRuleException exception = assertThrows(
            GameRuleException.class,
            () -> board.fireAt(List.of(new Coordinate(3, 3), new Coordinate(0, 0)))
        );
        assertEquals(RuleViolation.ALREADY_TARGETED, exception.violation());
        final BoardSnapshot snapshot = board.snapshot(
            PlayerId.PLAYER1,
            PlayerId.PLAYER1,
            new OwnerOnlyVisibilityPolicy()
        );
        assertEquals(CellState.SHIP, snapshot.stateAt(new Coordinate(3, 3)));
    }

    /**
     * Checks that a ship type is available only in the owner snapshot.
     */
    @Test
    void ownerSnapshotShowsShipTypeButOpponentSnapshotDoesNot() {
        final Board board = new BoardImpl(10, FleetRules.classic());
        final Coordinate coordinate = new Coordinate(2, 2);
        board.placeShip(Ship.place(
            new ShipId("flagship"),
            ShipType.FLAGSHIP,
            coordinate,
            Rotation.DEGREES_0
        ));

        final BoardSnapshot ownerSnapshot = board.snapshot(
            PlayerId.PLAYER1,
            PlayerId.PLAYER1,
            new OwnerOnlyVisibilityPolicy()
        );
        final BoardSnapshot opponentSnapshot = board.snapshot(
            PlayerId.PLAYER1,
            PlayerId.PLAYER2,
            new OwnerOnlyVisibilityPolicy()
        );

        assertEquals(ShipType.FLAGSHIP, ownerSnapshot.shipTypeAt(coordinate).orElseThrow());
        assertTrue(opponentSnapshot.shipTypeAt(coordinate).isEmpty());
        assertEquals(CellState.UNKNOWN, opponentSnapshot.stateAt(coordinate));
    }

    /**
     * Checks that the submarine is ignored by the sonar.
     */
    @Test
    void sonarIgnoreSub() {

        final Board board = new BoardImpl(10, FleetRules.classic());

        board.placeShip(Ship.place(
            new ShipId("Sub"),
            ShipType.INVISIBLE_SUBMARINE,
            new Coordinate(1, 1),
            Rotation.DEGREES_0
        ));

        board.placeShip(Ship.place(
            new ShipId("cru"),
            ShipType.CRUISER,
            new Coordinate(2, 1),
            Rotation.DEGREES_0
        ));

        final int detected = board.scan3x3(
            new Coordinate(2, 2),
            new InvisibleSubmarinePolicy(new OwnerOnlyVisibilityPolicy())
        );

        assertEquals(3, detected);
        assertFalse(board.hasCompleteFleet());
    }
}
