package it.unibo.battleship.model;

/**
 * Progress to charge a double shot.
 * 
 * @param progress current number of valid hits
 * @param requiredHits number of hits required
 * @param ready true if the double shot is ready
 */
public record DoubleShotStatus(int progress, int requiredHits, boolean ready) {

    /**
     * Validates the double-shot chatging status.
     */
    public DoubleShotStatus {
        if (requiredHits <= 0 || progress < 0 || progress > requiredHits) {
            throw new IllegalArgumentException("Invalid double-shot status");
        }
        if (ready != (progress == requiredHits)) {
            throw new IllegalArgumentException("Ready state not matching progress");
        }
    }
}
