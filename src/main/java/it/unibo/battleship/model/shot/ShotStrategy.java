package it.unibo.battleship.model.shot;

import it.unibo.battleship.model.Board;
import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.GameRuleException;
import it.unibo.battleship.model.RuleViolation;
import it.unibo.battleship.model.ShotResult;

import java.util.List;
import java.util.Objects;

/**
 * Strategy for a family of shots.
 */
@FunctionalInterface
public interface ShotStrategy {

    /**
     * Chooses the number coordinates to shoot at.
     * 
     * @return  the number of coordinates to target
     */
    int requiredTargets();

    /**
     * Applies the action to the opponent's board using the given coordinates.
     * 
     * @param targetBoard the board to apply the action.
     * @param targets the chosen coordinates
     * @return the result of the action
     */
    default List<ShotResult> execute(final Board targetBoard, final List<Coordinate> targets) {
        Objects.requireNonNull(targets, "targets");
        Objects.requireNonNull(targetBoard, "targetBoard");
        if (targets.size() != this.requiredTargets()) {
            throw new GameRuleException(
                RuleViolation.WRONG_TARGET_COUNT,
                "Do not exceed " + this.requiredTargets() + " number of targets"
            );
        }
        return targetBoard.fireAt(targets);
    }
}
