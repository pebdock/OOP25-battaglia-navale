package it.unibo.battleship.model.shot;

import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.GameRuleException;
import it.unibo.battleship.model.RuleViolation;

import java.util.Comparator;
import java.util.List;

/**
 * Strategy that fires at three consecutive horizontal or vertical cells.
 */
public final class SequentialShotStrategy
        extends AbstractShotStrategy {

    private static final int TARGET_COUNT = 3;

    /**
     * Creates a sequential three-target strategy.
     */
    public SequentialShotStrategy() {
        super(TARGET_COUNT);
    }

    @Override
    protected void validateSpecificTargets(
        final List<Coordinate> targets
    ) {
        if (!areConsecutive(targets)) {
            throw new GameRuleException(
                RuleViolation.SEQUENTIAL_TARGETS_NOT_IN_LINE,
                "Choose three consecutive cells in a straight line"
            );
        }
    }

    private static boolean areConsecutive(
        final List<Coordinate> targets
    ) {
        final List<Coordinate> sorted = targets.stream()
            .sorted(
                Comparator.comparingInt(Coordinate::row)
                    .thenComparingInt(Coordinate::column)
            )
            .toList();

        final boolean horizontal =
            sorted.stream()
                .map(Coordinate::row)
                .distinct()
                .count() == 1
                && sorted.get(1).column()
                    == sorted.get(0).column() + 1
                && sorted.get(2).column()
                    == sorted.get(1).column() + 1;

        final boolean vertical =
            sorted.stream()
                .map(Coordinate::column)
                .distinct()
                .count() == 1
                && sorted.get(1).row()
                    == sorted.get(0).row() + 1
                && sorted.get(2).row()
                    == sorted.get(1).row() + 1;

        return horizontal || vertical;
    }
}
