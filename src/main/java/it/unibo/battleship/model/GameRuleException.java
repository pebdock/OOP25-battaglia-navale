package it.unibo.battleship.model;

import java.util.Objects;

/**
 * Exceptions thrown when an operation violates a game rule.
 */
public final class GameRuleException extends RuntimeException {

    /**
     * Serialization version of this exception class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Semantic reason why the operation was rejected.
     */
    private final RuleViolation violation; // private field that tells the reason of the violation

    /**
     * Creates a game rule exception.
     * 
     * @param violation the rule violation that caused the exception
     * @param message a description of the problem
     */
    public GameRuleException(final RuleViolation violation, final String message) {
        super(message); // Calls the constructor of RuntimeException and stores the text that can be obtained with getMessage() 
        this.violation = Objects.requireNonNull(violation, "violation"); // Stores the violation in the final field 
    }

    /**
     * Returns the reason of the exception.
     * 
     * @return the rule violation associated with this exception
     */
    public RuleViolation violation() {
        return this.violation;
    }
}
