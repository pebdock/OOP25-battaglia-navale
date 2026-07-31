package it.unibo.battleship.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the result of an accepted action.
 * 
 * @param player the player that performed the action
 * @param shots the results of the shots
 * @param phase the game phase after the action
 * @param nextPlayer the next player if the game continues
 * @param winner the winner if the game finished
 */
public record TurnResult(
    PlayerId player,
    List<ShotResult> shots,
    GamePhase phase,
    Optional<PlayerId> nextPlayer,
    Optional<PlayerId> winner
) {
    /**
     * Validates and checks that the result of one turn is correct.
     */
    public TurnResult {
        Objects.requireNonNull(player, "player");
        shots = List.copyOf(Objects.requireNonNull(shots, "shots"));
        Objects.requireNonNull(phase, "phase");
        Objects.requireNonNull(nextPlayer, "nextPlayer");
        Objects.requireNonNull(winner, "winner");
        if (phase == GamePhase.FINISHED) {
            if (winner.isEmpty() || nextPlayer.isPresent()) {
                throw new IllegalArgumentException(
                    "The current turn needs a winner and no next player"
                );
            }
        } else if (phase == GamePhase.IN_PROGRESS) {
            if (winner.isPresent() || nextPlayer.isEmpty()) {
                throw new IllegalArgumentException(
                    "The current turn needs a next player and no winner"
                );
            }
        } else {
            throw new IllegalArgumentException(
                "The game can't be ready after a turn"
            );
        }
    }

}
