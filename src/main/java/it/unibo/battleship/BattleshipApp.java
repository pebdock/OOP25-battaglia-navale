package it.unibo.battleship;

import it.unibo.battleship.controller.BattleshipController;
import it.unibo.battleship.model.factory.StandardGameFactory;
import it.unibo.battleship.view.BattleshipFrame;
import it.unibo.battleship.view.BattleshipViewObserver;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.random.RandomGenerator;

/**
 * Application entry point that assembles the MVC components.
 */
public final class BattleshipApp {

    private BattleshipApp() { }

    /**
     * Configures Swing and starts the application.
     *
     * @param args ignored command-line arguments
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            applySystemLookAndFeel();

            final BattleshipFrame view =
                new BattleshipFrame();
            final BattleshipViewObserver controller =
                new BattleshipController(
                    view,
                    new StandardGameFactory(),
                    RandomGenerator.getDefault()
                );

            view.setObserver(controller);
            view.setVisible(true);
        });
    }

    /**
     * Applies the operating system look and feel when available.
     *
     */
    private static void applySystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
        } catch (
            final ReflectiveOperationException
                | UnsupportedLookAndFeelException ignored
        ) {
            // Swing keeps its portable default look and feel.
        }
    }
}
