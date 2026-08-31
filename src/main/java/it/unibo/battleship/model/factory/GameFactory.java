package it.unibo.battleship.model.factory;

import it.unibo.battleship.model.Board;
import it.unibo.battleship.model.Game;

import java.util.random.RandomGenerator;

/**
 * Creates fully configured game instances for the application.
 */
@FunctionalInterface
public interface GameFactory {

    /**
     * Creates a ready game from two complete boards.
     *
     * @param firstBoard board owned by player one
     * @param secondBoard board owned by player two
     * @param random source used by random events
     * @return a new game in the ready phase
     */
    Game create(
        Board firstBoard,
        Board secondBoard,
        RandomGenerator random
    );
}
