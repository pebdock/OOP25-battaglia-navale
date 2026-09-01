package it.unibo.battleship.view;

/**
 * Reasons for hiding private boards while the device changes hands.
 */
public enum HandoffReason {

    /**
     * The second player is about to place their fleet.
     */
    PLACEMENT,

    /**
     * The first player is about to begin the match.
     */
    GAME_START,

    /**
     * The device is being passed after a turn change.
     */
    TURN_CHANGE
}
