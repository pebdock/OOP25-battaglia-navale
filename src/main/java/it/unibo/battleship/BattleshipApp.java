package it.unibo.battleship;

import it.unibo.battleship.controller.GameController;
import it.unibo.battleship.controller.GameControllerImpl;
import it.unibo.battleship.view.BattleshipFrame;

import javax.swing.SwingUtilities;

/**
 * Application entry point. Wires the model, controller and graphical view.
 */
public final class BattleshipApp {

    private BattleshipApp() { }

    /**
     * Starts the Swing application on the Event Dispatch Thread.
     *
     * @param args ignored arguments
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            final BattleshipFrame view = new BattleshipFrame();
            final GameController controller = new GameControllerImpl();
            controller.attachView(view);
            view.setVisible(true);
        });
    }
}
