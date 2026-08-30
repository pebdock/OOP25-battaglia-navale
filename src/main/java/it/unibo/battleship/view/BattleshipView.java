package it.unibo.battleship.view;

import it.unibo.battleship.controller.GameController;
import it.unibo.battleship.model.BoardSnapshot;
import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.GameSnapshot;

import java.util.List;

/**
 * Graphical surface updated by the controller.
 */
public interface BattleshipView {

    /**
     * Connects the view to the controller that handles user actions.
     *
     * @param controller the controller
     */
    void setController(GameController controller);

    /**
     * Shows the harbor-name setup screen.
     */
    void showSetup();

    /**
     * Shows the fleet-placement screen for the current player.
     *
     * @param title header text
     * @param status status or hint text
     * @param board visible own board
     * @param confirmEnabled whether the fleet can be confirmed
     */
    void showPlacement(String title, String status, BoardSnapshot board, boolean confirmEnabled);

    /**
     * Shows the privacy card used when passing the device.
     *
     * @param message text displayed on the card
     */
    void showPrivacy(String message);

    /**
     * Shows the battle screen for the current player.
     *
     * @param snapshot visible game state
     * @param turnText current-turn label
     * @param abilitiesText available abilities
     * @param ownTitle own-board title
     * @param opponentTitle opponent-board title
     * @param pendingTargets cells selected for a double shot
     */
    void showGame(
        GameSnapshot snapshot,
        String turnText,
        String abilitiesText,
        String ownTitle,
        String opponentTitle,
        List<Coordinate> pendingTargets
    );

    /**
     * Clears the match log.
     */
    void clearLog();

    /**
     * Appends a line to the match log.
     *
     * @param text log line
     */
    void appendLog(String text);

    /**
     * Shows an informational dialog.
     *
     * @param title dialog title
     * @param message dialog body
     */
    void showInfo(String title, String message);

    /**
     * Shows a rule-warning dialog.
     *
     * @param message warning text
     */
    void showWarning(String message);

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
}
