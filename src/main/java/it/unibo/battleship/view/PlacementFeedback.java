package it.unibo.battleship.view;

import it.unibo.battleship.model.RuleViolation;

import java.util.Objects;

/**
 * Semantic feedback produced during fleet placement.
 */
public enum PlacementFeedback {
    INITIAL,
    PLACED,
    RESET,
    INCOMPLETE,
    SHIP_OVERLAP,
    SHIP_OUTSIDE_BOARD,
    FLEET_LIMIT_REACHED,
    SHIP_ALREADY_PLACED;

    /**
     * Converts a placement rule violation into semantic feedback.
     *
     * @param violation placement rule violation
     * @return corresponding placement feedback
     */
    public static PlacementFeedback fromViolation(
        final RuleViolation violation
    ) {
        return switch (
            Objects.requireNonNull(violation, "violation")
        ) {
            case SHIP_OVERLAP -> SHIP_OVERLAP;
            case SHIP_OUTSIDE_BOARD -> SHIP_OUTSIDE_BOARD;
            case FLEET_LIMIT_REACHED -> FLEET_LIMIT_REACHED;
            case SHIP_ALREADY_PLACED -> SHIP_ALREADY_PLACED;
            default -> throw new IllegalArgumentException(
                "Not a placement violation: " + violation
            );
        };
    }
}
