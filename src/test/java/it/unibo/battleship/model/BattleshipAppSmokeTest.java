package it.unibo.battleship.model;

import it.unibo.battleship.controller.GameController;
import it.unibo.battleship.controller.GameControllerImpl;
import it.unibo.battleship.view.BattleshipFrame;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;

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
            final GameController controller = new GameControllerImpl();

            controller.attachView(view);
            view.setVisible(true);

            assertTrue(view.isDisplayable());

            view.dispose();
        });
    }
}
