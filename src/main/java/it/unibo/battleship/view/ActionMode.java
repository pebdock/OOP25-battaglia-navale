package it.unibo.battleship.view;

/**
 * Player action selected in the battle screen.
 */
public enum ActionMode {
    NORMAL("Normal shot"),
    DOUBLE("Double shot"),
    SEQUENTIAL("Sequential shot"),
    SONAR("Sonar");

    private final String label;

    ActionMode(final String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
