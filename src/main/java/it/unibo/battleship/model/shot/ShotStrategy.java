package it.unibo.battleship.model.shot;

import it.unibo.battleship.model.Board;
import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.ShotResult;

import java.util.List;

/**
 * Strategy used to execute one kind of shot.
 */
@FunctionalInterface
public interface ShotStrategy {

    /**
     * Executes a shot against the supplied board.
     *
     * @param board board receiving the shot
     * @param targets coordinates selected by the player
     * @return immutable shot results in target order
     */
    List<ShotResult> execute(
        Board board,
        List<Coordinate> targets
    );
}
