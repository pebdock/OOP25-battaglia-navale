package it.unibo.battleship.model;

import java.util.Objects;

/**
 * A record that ties the chosen target with the resulted outcome.
 * 
 * @param target chosen coordinates for the shooting
 * @param outcome what happens if you choose that target
 */
public record ShotResult(Coordinate target, ShotOutcome outcome) {

    /**
     * Verifies that both target and outcome are not null.
     */
    public ShotResult {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(outcome, "outcome");
    }

    /**
     * Verifies if the player hits something.
     * 
     * @return true if the outcome of the istance is different than MISS (it hits or sunk a boat)
     */
    public boolean isHit() {
        return this.outcome != ShotOutcome.MISS;
    }
}
