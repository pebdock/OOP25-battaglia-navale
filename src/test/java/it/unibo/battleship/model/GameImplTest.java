package it.unibo.battleship.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.battleship.model.shot.DoubleShotStrategy;
import it.unibo.battleship.model.shot.NormalShotStrategy;
import it.unibo.battleship.model.shot.ShotStrategy;
import it.unibo.battleship.model.visibility.InvisibleSubmarinePolicy;
import it.unibo.battleship.model.visibility.OwnerOnlyVisibilityPolicy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests the main game rules in GameImpl.
 */
class GameImplTest {

    private static final int LAST_INDEX = 9;
    private static final int D1_INDEX = 5;
    private static final int D2_INDEX = 6;
    private static final int R_INDEX = 7;

    /**
     * Tests that a hit keeps the turn, a miss passes the turn and an
     * invalid action doesn't do anything.
     */
    @Test
    void checkTurnActions() {
        final Complete complete = newGame();
        final Game game = complete.game();
        game.start();

        game.playTurn(ShotKind.NORMAL, List.of(new Coordinate(0, 0)));
        assertEquals(PlayerId.PLAYER1, game.currentPlayer());

        game.playTurn(ShotKind.NORMAL, List.of(new Coordinate(LAST_INDEX, LAST_INDEX)));
        assertEquals(PlayerId.PLAYER2, game.currentPlayer());

        assertThrows(
            GameRuleException.class,
            () -> game.playTurn(ShotKind.NORMAL, List.of(new Coordinate(10, 10)))
        );
        assertEquals(PlayerId.PLAYER2, game.currentPlayer());
    }

    /**
     * Checks that an absorbed impact passes the turn and does not charge the
     * double shot, while the following damaging impact counts as a hit.
     */
    @Test
    void absorbedArmorImpactDoesNotCountAsHit() {
        final Board board1 = new BoardImpl(BoardImpl.REQUIRED_SIZE);
        final Board board2 = new BoardImpl(BoardImpl.REQUIRED_SIZE);
        placeCompleteFleet(board1, "p1");
        placeCompleteFleet(board2, "p2");
        final List<Coordinate> ignored = new ArrayList<>();
        place(
            board2,
            ignored,
            "p2-armoured",
            ShipType.ARMORED_SHIP,
            LAST_INDEX,
            0
        );
        final Game game = new GameImpl(
            new Player(PlayerId.PLAYER1, board1),
            new Player(PlayerId.PLAYER2, board2),
            Map.of(
                ShotKind.NORMAL, new NormalShotStrategy(),
                ShotKind.DOUBLE, new DoubleShotStrategy()
            ),
            new InvisibleSubmarinePolicy(new OwnerOnlyVisibilityPolicy())
        );
        game.start();

        final Coordinate armoredSection = new Coordinate(LAST_INDEX, 0);
        final TurnResult absorbed = game.playTurn(
            ShotKind.NORMAL,
            List.of(armoredSection)
        );
        assertEquals(ShotOutcome.ARMOR_ABSORBED, absorbed.shots().getFirst().outcome());
        assertEquals(PlayerId.PLAYER2, game.currentPlayer());
        assertEquals(0, game.snapshotFor(PlayerId.PLAYER1).doubleShot().progress());

        game.playTurn(
            ShotKind.NORMAL,
            List.of(new Coordinate(LAST_INDEX, LAST_INDEX))
        );
        final TurnResult damaged = game.playTurn(
            ShotKind.NORMAL,
            List.of(armoredSection)
        );
        assertEquals(ShotOutcome.HIT, damaged.shots().getFirst().outcome());
        assertEquals(PlayerId.PLAYER1, game.currentPlayer());
        assertEquals(1, game.snapshotFor(PlayerId.PLAYER1).doubleShot().progress());
    }

    /**
     * Tests that a three valid shots unclock double shot and
     * double shot hits don't charge it.
     */
    @Test
    void checkDoubleShots() {
        final Complete complete = newGame();
        final Game game = complete.game();
        game.start();

        final GameRuleException tooEarly = assertThrows(
            GameRuleException.class,
            () -> game.playTurn(ShotKind.DOUBLE, List.of(
            new Coordinate(0, 0), new Coordinate(0, 1)))
        );
        assertEquals(RuleViolation.DOUBLE_SHOT_NOT_READY, tooEarly.violation());

        game.playTurn(ShotKind.NORMAL, List.of(new Coordinate(0, 0)));
        game.playTurn(ShotKind.NORMAL, List.of(new Coordinate(0, 1)));
        game.playTurn(ShotKind.NORMAL, List.of(new Coordinate(0, 2)));
        assertTrue(game.snapshotFor(PlayerId.PLAYER1).doubleShot().ready());

        final TurnResult result = game.playTurn(ShotKind.DOUBLE, List.of(
            new Coordinate(0, 3), new Coordinate(0, 4))
        );

        assertEquals(2, result.shots().size());
        assertEquals(PlayerId.PLAYER1, game.currentPlayer());
        assertEquals(0, game.snapshotFor(PlayerId.PLAYER1).doubleShot().progress());
    }

    /**
     * Test that the sonar passes the turn and can't be used after using it one time.
     */
    @Test
    void checkSonar() {
        final Complete complete = newGame();
        final Game game = complete.game();
        game.start();

        final SonarResult result = game.useSonar(new Coordinate(2, 2));

        assertEquals(PlayerId.PLAYER2, result.nextPlayer());
        assertEquals(PlayerId.PLAYER2, game.currentPlayer());
        assertFalse(game.snapshotFor(PlayerId.PLAYER1).sonarAvailable());
    }

    /**
     * Checks that sinking the enemy last ship end the game.
     */
    @Test
    void lastSinkEndGame() {
        final Complete complete = newGame();
        final Game game = complete.game();
        game.start();

        TurnResult result = null;
        for (final Coordinate target : complete.playerTwoCells()) {
            result = game.playTurn(ShotKind.NORMAL, List.of(target));
        }

        assertNotNull(result);
        assertEquals(GamePhase.FINISHED, result.phase());
        assertEquals(PlayerId.PLAYER1, result.winner().orElseThrow());
        assertEquals(PlayerId.PLAYER1, game.currentPlayer());
    }

    /**
     * Creates a game with 2 complete fleets.
     * 
     * @return the union containing the game and the second player's ship cells
     */
    private static Complete newGame() {
        final Board board1 = new BoardImpl(10);
        final Board board2 = new BoardImpl(10);
        placeCompleteFleet(board1, "p1");
        final List<Coordinate> playerTwoCells = placeCompleteFleet(board2, "p2");
        final Player p1 = new Player(PlayerId.PLAYER1, board1);
        final Player p2 = new Player(PlayerId.PLAYER2, board2);
        final Map<ShotKind, ShotStrategy> strategies = Map.of(
            ShotKind.NORMAL, new NormalShotStrategy(),
            ShotKind.DOUBLE, new DoubleShotStrategy()
        );

        return new Complete(
            new GameImpl(
                p1,
                p2,
                strategies,
                new InvisibleSubmarinePolicy(new OwnerOnlyVisibilityPolicy())
            ), 
            playerTwoCells
        );
    }

    /**
     * Places a complete fleet on the board.
     * 
     * @param board the board to place the fleet
     * @param name the prefix for ship idesntifiers
     * @return coordinates occupied by the fleet
     */
    private static List<Coordinate> placeCompleteFleet(
        final Board board,
        final String name
    ) {
        final List<Coordinate> cells = new ArrayList<>();
        place(board, cells, name + "fs", ShipType.FLAGSHIP, 0, 0);
        place(board, cells, name + "bs", ShipType.BATTLESHIP, 1, 0);
        place(board, cells, name + "cr1", ShipType.CRUISER, 2, 0);
        place(board, cells, name + "cr2", ShipType.CRUISER, 3, 0);
        place(board, cells, name + "is", ShipType.INVISIBLE_SUBMARINE, 4, 0);
        place(board, cells, name + "de1", ShipType.DESTROYER, D1_INDEX, 0);
        place(board, cells, name + "de2", ShipType.DESTROYER, D2_INDEX, 0);
        place(board, cells, name + "r", ShipType.RECON, R_INDEX, 0);
        return List.copyOf(cells);
    }

    /**
     * Creates and places one ship.
     * 
     * @param board the destination board
     * @param cells the collection to record the occupied cells
     * @param id ship identifier
     * @param type ship type
     * @param row starting row
     * @param column starting column
     */
    private static void place(
        final Board board,
        final List<Coordinate> cells,
        final String id,
        final ShipType type,
        final int row,
        final int column
    ) {
        final Ship ship = Ship.place(
            new ShipId(id),
            type,
            new Coordinate(row, column),
            Rotation.DEGREES_0
        );
        board.placeShip(ship);
        cells.addAll(ship.cells());
    }

    /**
     * Unify in a single object the game and second player ship cells.
     * 
     * @param game configured game
     * @param playerTwoCells cells occupied by the second player's ships
     */
    private record Complete(Game game, List<Coordinate> playerTwoCells) { }
}
