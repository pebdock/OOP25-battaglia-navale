package it.unibo.battleship.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.battleship.model.shot.SequentialShotStrategy;
import it.unibo.battleship.model.shot.DoubleShotStrategy;
import it.unibo.battleship.model.shot.NormalShotStrategy;
import it.unibo.battleship.model.shot.ShotStrategy;
import it.unibo.battleship.model.visibility.InvisibleSubmarinePolicy;
import it.unibo.battleship.model.visibility.OwnerOnlyVisibilityPolicy;
import it.unibo.battleship.model.testutils.FleetFactory;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
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

    /**
     * A random event moves an unsunk ship and returns its movement details.
     */
    @Test
    void randomEventMovesAnUnsunkShip() {
        final Random random = new Random(20);

        final Map<ShotKind, ShotStrategy> strategies = Map.of(
            ShotKind.NORMAL, new NormalShotStrategy(),
            ShotKind.DOUBLE, new DoubleShotStrategy(),
            ShotKind.SEQUENTIAL, new SequentialShotStrategy()
        );

        final Game game = new GameImpl(
            new Player(
                PlayerId.PLAYER1,
                FleetFactory.createRandomBoard(random, "p1")
            ),
            new Player(
                PlayerId.PLAYER2,
                FleetFactory.createRandomBoard(random, "p2")
            ),
            strategies,
            new InvisibleSubmarinePolicy(new OwnerOnlyVisibilityPolicy()),
            random
        );

        game.start();

        final RandomEventResult result = game.triggerRandomEvent().orElseThrow();

        assertTrue(
            result.boardOwner() == PlayerId.PLAYER1
                || result.boardOwner() == PlayerId.PLAYER2
        );

        assertNotEquals(result.move().from(), result.move().to());
        assertTrue(result.move().damageCount() >= 0);
    }

    /**
     * A random event cannot be triggered when the game is not running.
     */
    @Test
    void randomEventIsRejectedWhenGameIsNotRunning() {
        final Random random = new Random(30);

        final Map<ShotKind, ShotStrategy> strategies = Map.of(
            ShotKind.NORMAL, new NormalShotStrategy(),
            ShotKind.DOUBLE, new DoubleShotStrategy(),
            ShotKind.SEQUENTIAL, new SequentialShotStrategy()
        );

        final Game game = new GameImpl(
            new Player(
                PlayerId.PLAYER1,
                FleetFactory.createRandomBoard(random, "p1")
            ),
            new Player(
                PlayerId.PLAYER2,
                FleetFactory.createRandomBoard(random, "p2")
            ),
            strategies,
            new InvisibleSubmarinePolicy(new OwnerOnlyVisibilityPolicy()),
            random
        );

        final GameRuleException exception = assertThrows(
            GameRuleException.class,
            game::triggerRandomEvent
        );

        assertEquals(
            RuleViolation.GAME_NOT_RUNNING,
            exception.violation()
        );
    }

    /**
     * A board snapshot exposes the type of a visible ship.
     */
    @Test
    void snapshotReturnsVisibleShipType() {
        final Board board = new BoardImpl(BoardImpl.REQUIRED_SIZE);

        board.placeShip(Ship.place(
            new ShipId("destroyer"),
            ShipType.DESTROYER,
            new Coordinate(2, 2),
            Rotation.DEGREES_0
        ));

        final BoardSnapshot snapshot = board.snapshot(
            PlayerId.PLAYER1,
            PlayerId.PLAYER1,
            new OwnerOnlyVisibilityPolicy()
        );

        assertEquals(
            ShipType.DESTROYER,
            snapshot.shipTypeAt(new Coordinate(2, 2)).orElseThrow()
        );

        assertTrue(
            snapshot.shipTypeAt(new Coordinate(0, 0)).isEmpty()
        );
    }

    /**
     * A board snapshot rejects coordinates that do not belong to it.
     */
    @Test
    void snapshotRejectsCoordinateOutsideSnapshot() {
        final Board board = new BoardImpl(BoardImpl.REQUIRED_SIZE);

        final BoardSnapshot snapshot = board.snapshot(
            PlayerId.PLAYER1,
            PlayerId.PLAYER1,
            new OwnerOnlyVisibilityPolicy()
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> snapshot.shipTypeAt(
                new Coordinate(BoardImpl.REQUIRED_SIZE + 1, 0)
            )
        );
    }
}