package it.unibo.battleship.model.shot;

import it.unibo.battleship.model.Board;
import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.GameRuleException;
import it.unibo.battleship.model.RuleViolation;
import it.unibo.battleship.model.ShotResult;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Validates common shot constraints.
 */
public abstract class AbstractShotStrategy
        implements ShotStrategy {

    private final int requiredTargets;

    /**
     * Creates a strategy requiring an exact number of targets.
     *
     * @param requiredTargets required number of coordinates
     */
    protected AbstractShotStrategy(final int requiredTargets) {
        if (requiredTargets <= 0) {
            throw new IllegalArgumentException(
                "Required targets must be positive"
            );
        }
        this.requiredTargets = requiredTargets;
    }

    @Override
    public final List<ShotResult> execute(
        final Board board,
        final List<Coordinate> targets
    ) {
        Objects.requireNonNull(board, "board");

        final List<Coordinate> copy = List.copyOf(
            Objects.requireNonNull(targets, "targets")
        );

        this.validateCount(copy);
        validateDuplicates(copy);
        this.validateSpecificTargets(copy);

        return board.fireAt(copy);
    }

    /**
     * Validates constraints owned by a concrete strategy.
     *
     * @param targets validated immutable targets
     */
    protected void validateSpecificTargets(
        final List<Coordinate> targets
    ) { }

    /**
     * Checks if the correct number of targets has been selected.
     * 
     * @param targets chosen targets
     */
    private void validateCount(
        final List<Coordinate> targets
    ) {
        if (targets.size() != this.requiredTargets) {
            throw new GameRuleException(
                RuleViolation.WRONG_TARGET_COUNT,
                "Expected " + this.requiredTargets
                    + " targets, but received " + targets.size()
            );
        }
    }

    /**
     * Checks if target coordinates are different.
     * 
     * @param targets chosen coordinates
     */
    private static void validateDuplicates(
        final List<Coordinate> targets
    ) {
        if (new HashSet<>(targets).size() != targets.size()) {
            throw new GameRuleException(
                RuleViolation.DUPLICATE_TARGET,
                "Target coordinates must be distinct"
            );
        }
    }
}
