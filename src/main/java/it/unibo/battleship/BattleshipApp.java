package it.unibo.battleship;

import it.unibo.battleship.controller.GameController;
import it.unibo.battleship.controller.GameControllerImpl;
import it.unibo.battleship.view.BattleshipFrame;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.logging.Logger;

/**
 * Application entry point. Wires the model, controller and graphical view.
 */
public final class BattleshipApp {

    private static final Logger LOGGER = Logger.getLogger(BattleshipApp.class.getName());
    private static final int SMOKE_CLOSE_DELAY_MS = 750;

    private BattleshipApp() { }

    /**
     * Starts the Swing application on the Event Dispatch Thread.
     *
     * @param args pass {@code --smoke-test} to open and close a deterministic game
     */
    public static void main(final String[] args) {
        final boolean smokeTest = args.length > 0 && "--smoke-test".equals(args[0]);
        SwingUtilities.invokeLater(() -> {
            final BattleshipFrame view = new BattleshipFrame();
            final GameController controller = new GameControllerImpl();
            controller.attachView(view);
            view.setVisible(true);
            if (smokeTest) {
                controller.startSmokeGame();
                final Timer closeTimer = new Timer(SMOKE_CLOSE_DELAY_MS, event -> {
                    view.dispose();
                    LOGGER.info("GUI_SMOKE_OK");
                    System.exit(0);
                });
                closeTimer.setRepeats(false);
                closeTimer.start();
            }
        });
    }
}
