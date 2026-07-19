package it.unibo.battleship.model.visibility;

import it.unibo.battleship.model.PlayerId;
import java.util.Objects;

/**
 * Input to decide if one intact ship may be rendered.
 * 
 * @param owner owns the ship in his fleet
 * @param viewer views the grid of the owner
 */
public record VisibilityContext(PlayerId owner, PlayerId viewer) {
    /**
     * Verifies that owner and viewer are not null.
     */
    public VisibilityContext {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(viewer, "viewer");
    }
}
