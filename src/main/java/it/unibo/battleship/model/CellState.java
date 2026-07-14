package it.unibo.battleship.model;

/**
 * Tells the different states a cell can have.
 */
public enum CellState {
    MISS,
    HIT,
    SUNK,
    SEA,
    SHIP,
    UNKNOWN // it's the state at the beginning, before the ships placement
}
