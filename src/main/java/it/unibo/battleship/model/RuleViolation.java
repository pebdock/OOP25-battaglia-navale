package it.unibo.battleship.model;

/**
 * Reasons why an operation was rejected.
 */
public enum RuleViolation {
    OUTSIDE_BOARD,
    ALREADY_TARGETED,
    DUPLICATE_TARGET,
    WRONG_TARGET_COUNT,
    SHIP_OVERLAP,
    SHIP_OUTSIDE_BOARD,
    SHIP_ALREADY_PLACED,
    FLEET_LIMIT_REACHED,
    FLEET_INCOMPLETE,
    DOUBLE_SHOT_NOT_READY,
    SONAR_NOT_AVAILABLE,
    GAME_NOT_RUNNING,
    GAME_ALREADY_STARTED
}
