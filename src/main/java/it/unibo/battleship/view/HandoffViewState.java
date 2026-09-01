package it.unibo.battleship.view;

import java.util.Objects;

/**
 * Immutable information displayed while the device changes hands.
 *
 * @param recipientHarbor harbor allowed to reveal the next screen
 * @param reason reason for the device handoff
 */
public record HandoffViewState(
    String recipientHarbor,
    HandoffReason reason
) {

    /**
     * Validates and normalizes the handoff information.
     *
     */
    public HandoffViewState {
        recipientHarbor = Objects.requireNonNull(
            recipientHarbor,
            "recipientHarbor"
        ).trim();

        Objects.requireNonNull(reason, "reason");

        if (recipientHarbor.isBlank()) {
            throw new IllegalArgumentException(
                "Recipient harbor cannot be blank"
            );
        }
    }
}
