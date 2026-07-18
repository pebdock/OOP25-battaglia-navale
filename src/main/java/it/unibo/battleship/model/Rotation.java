package it.unibo.battleship.model;

/**
 * The different orientations a ship can have.
 */
public enum Rotation {
    DEGREES_0(0),
    DEGREES_90(1),
    DEGREES_180(2),
    DEGREES_270(3);

    private final int quarterTurns; // it indicates how many 90 degrees turns are made

    /**
     * Constructor implicitly private because it's an enum.
     * 
     * @param quarterTurns how many 90 degrees turns are made
     */
    Rotation(final int quarterTurns) {
        this.quarterTurns = quarterTurns;
    }

    /**
     * Getter that returns the quarter turns.
     * 
     * @return the number of 90 degrees turns
     */
    int quarterTurns() {
        return this.quarterTurns;
    }
}
