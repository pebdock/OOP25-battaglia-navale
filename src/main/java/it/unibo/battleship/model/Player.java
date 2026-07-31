package it.unibo.battleship.model;

import java.util.Objects;

/**
 * Defines the player state.
 */
public final class Player {
    private final PlayerId id;
    private final Board board;
    private final DoubleShotAbility doubleShot = new DoubleShotAbility();
    private boolean sonarAvailable = true;

    /**
     * Pairs a player and his board together.
     * 
     * @param id the id of the player
     * @param board the board to associate to the player
     */
    public Player(final PlayerId id, final Board board) {
        this.id = Objects.requireNonNull(id, "id");
        this.board = Objects.requireNonNull(board, "board");
    }

    /**
     * Tells the id of the player.
     * 
     * @return the id of the player
     */
    public PlayerId id() {
        return this.id;
    }

    /**
     * Tells the board of the player.
     * 
     * @return the board paired to the player
     */
    Board board() {
        return this.board;
    }

    /**
     * Tells the status of the double shot.
     * 
     * @return the status of the double shot
     */
    public DoubleShotStatus doubleShotStatus() {
        return this.doubleShot.status();
    }

    /**
     * Tells if the sonar is still available.
     * 
     * @return true if available
     */
    public boolean sonarAvailable() {
        return this.sonarAvailable;
    }

    /**
     * Tells if the player can use the specified shot kind.
     * 
     * @param kind single or double shot
     * @return true if the player can use it
     */
    boolean canUse(final ShotKind kind) {
        return kind == ShotKind.NORMAL || this.doubleShot.isReady();
    }

    /**
     * Increases the double shot status after a single valid hit.
     */
    void registerSingleHit() {
        this.doubleShot.registerSingleHit();
    }

    /**
     * Restores to the initial state teh double shot status.
     */
    void consumeDoubleShot() {
        this.doubleShot.consume();
    }

    /**
     * Checks if the player can use the sonar and makes it unavailable after one use.
     */
    void consumeSonar() {
        if (!this.sonarAvailable) {
            throw new GameRuleException(
                RuleViolation.SONAR_NOT_AVAILABLE,
                "Sonar already used"
            );
        }
        this.sonarAvailable = false;
    }
}
