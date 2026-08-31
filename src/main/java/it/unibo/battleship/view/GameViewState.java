package it.unibo.battleship.view;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.unibo.battleship.model.GameSnapshot;
import it.unibo.battleship.model.PlayerId;
import it.unibo.battleship.model.Coordinate;

/**
 * Immutable data required to render one player's battle screen.
 *
 * @param snapshot game snapshot filtered for the viewer
 * @param harborNames immutable player names
 * @param pendingTargets selected but not yet fired targets
 */
public record GameViewState(
    GameSnapshot snapshot,
    Map<PlayerId, String> harborNames,
    List<Coordinate> pendingTargets
) {
    /**
     * Validates the state and stores defensive copies.
     */
    public GameViewState {
        Objects.requireNonNull(snapshot, "snapshot");
        harborNames = Map.copyOf(
            Objects.requireNonNull(harborNames, "harborNames")
        );
        pendingTargets = List.copyOf(
            Objects.requireNonNull(pendingTargets, "pendingTargets")
        );
    }
}
