package it.unibo.battleship.model.factory;

import it.unibo.battleship.model.Board;
import it.unibo.battleship.model.Game;
import it.unibo.battleship.model.GameImpl;
import it.unibo.battleship.model.Player;
import it.unibo.battleship.model.PlayerId;
import it.unibo.battleship.model.ShotKind;
import it.unibo.battleship.model.shot.DoubleShotStrategy;
import it.unibo.battleship.model.shot.NormalShotStrategy;
import it.unibo.battleship.model.shot.SequentialShotStrategy;
import it.unibo.battleship.model.shot.ShotStrategy;
import it.unibo.battleship.model.visibility.InvisibleSubmarinePolicy;
import it.unibo.battleship.model.visibility.OwnerOnlyVisibilityPolicy;

import java.util.Map;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Creates the standard Battleship game configuration.
 */
public final class StandardGameFactory
        implements GameFactory {

    @Override
    public Game create(
        final Board firstBoard,
        final Board secondBoard,
        final RandomGenerator random
    ) {
        Objects.requireNonNull(firstBoard, "firstBoard");
        Objects.requireNonNull(secondBoard, "secondBoard");
        Objects.requireNonNull(random, "random");

        final Map<ShotKind, ShotStrategy> strategies = Map.of(
            ShotKind.NORMAL, new NormalShotStrategy(),
            ShotKind.DOUBLE, new DoubleShotStrategy(),
            ShotKind.SEQUENTIAL, new SequentialShotStrategy()
        );

        return new GameImpl(
            new Player(PlayerId.PLAYER1, firstBoard),
            new Player(PlayerId.PLAYER2, secondBoard),
            strategies,
            new InvisibleSubmarinePolicy(
                new OwnerOnlyVisibilityPolicy()
            ),
            random
        );
    }
}
