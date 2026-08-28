package it.unibo.battleship.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.battleship.model.shot.SequentialShotStrategy;
import it.unibo.battleship.model.visibility.OwnerOnlyVisibilityPolicy;
import it.unibo.battleship.model.testutils.FleetFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

/**
 * Deterministic tests for the added special mechanics.
 */
class SpecialMechanicsTest {

    private static final int MOVEMENT_SEED = 7;
    private static final int GENERATED_FLEET_CELL_COUNT = 28;

    /**
     * The armour absorbs exactly one impact and the same cell remains targetable.
     */
    @Test
    void armoredShipAbsorbsOnlyTheFirstHit() {
        final Board board = new BoardImpl(BoardImpl.REQUIRED_SIZE);
        final Ship ship = Ship.place(
            new ShipId("armoured"),
            ShipType.ARMORED_SHIP,
            new Coordinate(1, 1),
            Rotation.DEGREES_0
        );
        board.placeShip(ship);

        assertEquals(
            ShotOutcome.ARMOR_ABSORBED,
            board.fireAt(List.of(new Coordinate(1, 1))).getFirst().outcome()
        );
        assertFalse(ship.armorAvailable());
        assertEquals(
            ShotOutcome.HIT,
            board.fireAt(List.of(new Coordinate(1, 1))).getFirst().outcome()
        );
        board.fireAt(List.of(new Coordinate(1, 2)));
        assertEquals(
            ShotOutcome.SUNK,
            board.fireAt(List.of(new Coordinate(1, 3))).getFirst().outcome()
        );
    }

    /**
     * The sequential shot accepts only three consecutive cells in one line.
     */
    @Test
    void sequentialShotValidatesGeometry() {
        final Board board = new BoardImpl(BoardImpl.REQUIRED_SIZE);
        board.placeShip(Ship.place(
            new ShipId("cruiser"),
            ShipType.CRUISER,
            new Coordinate(3, 3),
            Rotation.DEGREES_0
        ));
        final SequentialShotStrategy strategy = new SequentialShotStrategy();

        final List<ShotResult> results = strategy.execute(board, List.of(
            new Coordinate(3, 3),
            new Coordinate(3, 4),
            new Coordinate(3, 5)
        ));
        assertEquals(3, results.size());
        assertEquals(ShotOutcome.SUNK, results.getLast().outcome());

        final GameRuleException exception = assertThrows(
            GameRuleException.class,
            () -> strategy.execute(new BoardImpl(BoardImpl.REQUIRED_SIZE), List.of(
                new Coordinate(1, 1),
                new Coordinate(1, 2),
                new Coordinate(2, 2)
            ))
        );
        assertEquals(RuleViolation.SEQUENTIAL_TARGETS_NOT_IN_LINE, exception.violation());
    }

    /**
     * A moved ship keeps its damage and never remains in the same cells.
     */
    @Test
    void randomMovementPreservesDamage() {
        final Board board = new BoardImpl(BoardImpl.REQUIRED_SIZE);
        board.placeShip(Ship.place(
            new ShipId("moving"),
            ShipType.CRUISER,
            new Coordinate(2, 2),
            Rotation.DEGREES_0
        ));
        board.fireAt(List.of(new Coordinate(2, 2)));

        final ShipMove move = board.moveRandomUnsunkShip(new Random(MOVEMENT_SEED)).orElseThrow();
        assertEquals(1, move.damageCount());
        assertNotEquals(move.from(), move.to());
        assertTrue(move.from().stream().noneMatch(move.to()::contains));

        final BoardSnapshot snapshot = board.snapshot(
            PlayerId.PLAYER1,
            PlayerId.PLAYER1,
            new OwnerOnlyVisibilityPolicy()
        );
        assertEquals(
            1,
            snapshot.cells().values().stream().filter(state -> state == CellState.HIT).count()
        );
        assertEquals(CellState.MISS, snapshot.stateAt(new Coordinate(2, 2)));
    }

    /**
     * Generated GUI fleets contain the optional special ship and satisfy the
     * engine's original complete-fleet contract.
     */
    @Test
    void generatedFleetIsComplete() {
        final Board board = FleetFactory.createRandomBoard(new Random(42), "test");
        assertTrue(board.hasCompleteFleet());
        final BoardSnapshot snapshot = board.snapshot(
            PlayerId.PLAYER1,
            PlayerId.PLAYER1,
            new OwnerOnlyVisibilityPolicy()
        );
        assertEquals(
            GENERATED_FLEET_CELL_COUNT,
            snapshot.cells().values().stream().filter(state -> state == CellState.SHIP).count()
        );
        assertFalse(board.allShipsSunk());
    }
}