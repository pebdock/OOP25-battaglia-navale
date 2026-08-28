package it.unibo.battleship.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.battleship.model.shot.DoubleShotStrategy;
import it.unibo.battleship.model.shot.NormalShotStrategy;
import it.unibo.battleship.model.shot.SequentialShotStrategy;
import it.unibo.battleship.model.shot.ShotStrategy;
import it.unibo.battleship.model.visibility.InvisibleSubmarinePolicy;
import it.unibo.battleship.model.visibility.OwnerOnlyVisibilityPolicy;
import it.unibo.battleship.model.testutils.FleetFactory;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * End-to-end game-engine smoke test with two generated full fleets.
 */
class CompleteGameSmokeTest {

    private static final int MAXIMUM_TURNS = 250;

    /**
     * Two players can finish a complete game even with armour and random movement enabled.
     */
    @Test
    void twoPlayersCanFinishCompleteGame() {
        final Random random = new Random(91);
        final Map<ShotKind, ShotStrategy> strategies = Map.of(
            ShotKind.NORMAL, new NormalShotStrategy(),
            ShotKind.DOUBLE, new DoubleShotStrategy(),
            ShotKind.SEQUENTIAL, new SequentialShotStrategy()
        );
        final Game game = new GameImpl(
            new Player(PlayerId.PLAYER1, FleetFactory.createRandomBoard(random, "p1")),
            new Player(PlayerId.PLAYER2, FleetFactory.createRandomBoard(random, "p2")),
            strategies,
            new InvisibleSubmarinePolicy(new OwnerOnlyVisibilityPolicy()),
            random
        );
        game.start();
        assertTrue(game.triggerRandomEvent().isPresent());

        final Map<PlayerId, Set<Coordinate>> attemptedByBoard = new EnumMap<>(PlayerId.class);
        attemptedByBoard.put(PlayerId.PLAYER1, new HashSet<>());
        attemptedByBoard.put(PlayerId.PLAYER2, new HashSet<>());
        int turns = 0;
        while (game.phase() == GamePhase.IN_PROGRESS && turns < MAXIMUM_TURNS) {
            turns++;
            final PlayerId targetOwner = game.currentPlayer().other();
            final Set<Coordinate> attempted = attemptedByBoard.get(targetOwner);
            final Coordinate target = nextTarget(attempted);
            attempted.add(target);
            final ShotResult shot = game.playTurn(ShotKind.NORMAL, List.of(target)).shots().getFirst();
            if (shot.outcome() == ShotOutcome.ARMOR_ABSORBED) {
                attempted.remove(target);
            }
        }

        assertEquals(GamePhase.FINISHED, game.phase());
        assertTrue(game.winner().isPresent());
    }

    private static Coordinate nextTarget(final Set<Coordinate> attempted) {
        for (int row = 0; row < BoardImpl.REQUIRED_SIZE; row++) {
            for (int column = 0; column < BoardImpl.REQUIRED_SIZE; column++) {
                final Coordinate candidate = new Coordinate(row, column);
                if (!attempted.contains(candidate)) {
                    return candidate;
                }
            }
        }
        throw new IllegalStateException("No target left");
    }
}