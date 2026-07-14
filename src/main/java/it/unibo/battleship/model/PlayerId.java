package it.unibo.battleship.model;

/**
 * Immutable identifier for a player.
 */
public enum PlayerId {
    PLAYER1,
    PLAYER2;

    /**
     * Returns the other player ID.
     * 
     * @return the other player ID
     */
    public PlayerId other() {
        return this == PLAYER1 ? PLAYER2 : PLAYER1;
    }
}
