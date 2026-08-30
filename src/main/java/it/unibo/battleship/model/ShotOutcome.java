package it.unibo.battleship.model;

/**
 * The different outcomes after choosing a coordinate to shoot.
 * 
 * ARMOR_ABSORBED is treated as a miss for game rules:
 * the shot does not damage the ship and the same cell can be targeted again.
 */
public enum ShotOutcome {
    HIT,
    MISS,
    SUNK,
    ARMOR_ABSORBED
}
