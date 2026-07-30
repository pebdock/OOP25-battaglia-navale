package it.unibo.battleship.model;

/**
 * Behaviour of the double shot ability.
 */
public final class DoubleShotAbility {
    public static final int REQUIRED_SINGLE_HITS = 3;

    private int progress;

    /**
     * Tells if the double shot is ready to be used.
     * 
     * @return true if the valid hits equals the required valid hits.
     */
    public boolean isReady() {
        return this.progress == REQUIRED_SINGLE_HITS;
    }

    /**
     * Tells the current status of the double shot ability.
     * 
     * @return the current number of valid hits done and if the ability is ready
     */
    public DoubleShotStatus status() {
        return new DoubleShotStatus(progress, REQUIRED_SINGLE_HITS, isReady());
    }

    /**
     * If the ability is ready, don't increase the progress value.
     */
    void registerSingleHit() {
        if (!this.isReady()) {
            this.progress++;
        }
    }

    /**
     * Uses the ability if ready.
     */
    void consume() {
        if (!this.isReady()) {
            throw new GameRuleException(
                RuleViolation.DOUBLE_SHOT_NOT_READY,
                "Three successful normal shots are required"
            );
        }
        this.progress = 0;
    }
}
