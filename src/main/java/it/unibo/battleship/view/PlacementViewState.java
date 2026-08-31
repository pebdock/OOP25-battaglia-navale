package it.unibo.battleship.view;

import java.util.Map;
import java.util.Objects;

import it.unibo.battleship.model.BoardSnapshot;
import it.unibo.battleship.model.FleetRules;
import it.unibo.battleship.model.ShipType;

/**
 * Immutable data required to render fleet placement.
 *
 * @param harborName current harbor name
 * @param board visible placement board
 * @param rules active fleet rules
 * @param placedShips number of placed ships by type
 * @param feedback last semantic feedback
 * @param complete whether the fleet can be confirmed
 */
public record PlacementViewState(
    String harborName,
    BoardSnapshot board,
    FleetRules rules,
    Map<ShipType, Integer> placedShips,
    PlacementFeedback feedback,
    boolean complete
) {
    /**
     * Validates the state and stores defensive copies.
     */
    public PlacementViewState {
        Objects.requireNonNull(harborName, "harborName");
        Objects.requireNonNull(board, "board");
        Objects.requireNonNull(rules, "rules");
        placedShips = Map.copyOf(
            Objects.requireNonNull(placedShips, "placedShips")
        );
        Objects.requireNonNull(feedback, "feedback");
    }
}
