package it.unibo.battleship.view;

import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.PlayerId;
import it.unibo.battleship.model.RandomEventResult;
import it.unibo.battleship.model.RuleViolation;
import it.unibo.battleship.model.ShotKind;
import it.unibo.battleship.model.SonarResult;
import it.unibo.battleship.model.TurnResult;

import java.util.Map;
import java.util.Optional;

/**
 * Graphical surface updated by the controller.
 */
public interface BattleshipView {

    /**
     * Connects the observer that receives user actions.
     *
     * @param observer observer receiving view events
     */
    void setObserver(BattleshipViewObserver observer);

    /**
     * Shows the harbor-name setup screen.
     */
    void showSetup();

    /**
     * Shows the fleet-placement screen for the current player.
     *
     * @param state immutable placement presentation state
     */
    void showPlacement(PlacementViewState state);

    /**
     * Shows the privacy card used when passing the device.
     *
     * @param message text displayed on the card
     */
    void showPrivacy(String message);

    /**
     * Shows the battle screen for the current player.
     *
     * @param state immutable game presentation state
     */
    void showGame(GameViewState state);

    /**
     * Clears the match log.
     */
    void clearLog();

    /**
     * Announces the winner of the match.
     *
     * @param winnerName harbor name of the winner
     */
    void showChampion(String winnerName);

    /**
     * Plays a short hit sound.
     */
    void beep();

    /**
     * Starts the periodic random-event timer.
     */
    void startRandomEventTimer();

    /**
     * Stops the periodic random-event timer.
     */
    void stopRandomEventTimer();

    /**
     * Appends the initial match message to the game log.
     *
     * @param firstHarbor name of the first harbor
     */
    void appendGameStarted(String firstHarbor);

    /**
     * Reports the first selected target of a double shot.
     *
     * @param target selected target
     */
    void appendDoubleTargetSelected(Coordinate target);

    /**
     * Presents the result of an accepted shot action.
     *
     * @param harborName name of the acting harbor
     * @param kind selected shot kind
     * @param result immutable turn result
     */
    void appendTurnResult(
        String harborName,
        ShotKind kind,
        TurnResult result
    );

    /**
     * Presents the result of an accepted sonar action.
     *
     * @param harborName name of the acting harbor
     * @param result immutable sonar result
     */
    void appendSonarResult(
        String harborName,
        SonarResult result
    );

    /**
     * Presents the result of a periodic random event.
     *
     * @param result optional random-event result
     * @param harborNames immutable harbor names indexed by player
     */
    void appendRandomEvent(
        Optional<RandomEventResult> result,
        Map<PlayerId, String> harborNames
    );

    /**
     * Presents a rejected operation to the user.
     *
     * @param violation rule that rejected the operation
     */
    void showRuleViolation(RuleViolation violation);

}
