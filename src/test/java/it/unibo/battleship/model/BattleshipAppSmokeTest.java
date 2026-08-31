package it.unibo.battleship.model;

import it.unibo.battleship.controller.BattleshipController;
import it.unibo.battleship.model.factory.StandardGameFactory;
import it.unibo.battleship.view.BattleshipFrame;
import it.unibo.battleship.view.BattleshipViewObserver;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the window.
 */
class BattleshipAppSmokeTest {

    /**
     * Checks if the window can be opened and closed.
     *
     * @throws InterruptedException if the thread waiting for the EDT is interrupted
     * @throws InvocationTargetException if the code executed on the EDT fails
     */
    @Test
    void appOpenClose() throws InterruptedException, InvocationTargetException {
        Assumptions.assumeFalse(
            GraphicsEnvironment.isHeadless(),
            "GUI smoke test requires a graphical environment"
        );

        SwingUtilities.invokeAndWait(() -> {
            final BattleshipFrame view = new BattleshipFrame();
            final BattleshipViewObserver controller = new BattleshipController(
                view,
                new StandardGameFactory(),
                new Random(42)
            );

            view.setObserver(controller);
            view.setVisible(true);

            assertTrue(view.isDisplayable());

            view.dispose();
        });
    }
}
