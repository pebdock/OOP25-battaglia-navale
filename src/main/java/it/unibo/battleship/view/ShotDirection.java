package it.unibo.battleship.view;

/**
 * Cardinal direction used to build a sequential shot.
 */
public enum ShotDirection {
    RIGHT("Right", 0, 1),
    DOWN("Down", 1, 0),
    LEFT("Left", 0, -1),
    UP("Up", -1, 0);

    private final String label;
    private final int rowDelta;
    private final int columnDelta;

    ShotDirection(final String label, final int rowDelta, final int columnDelta) {
        this.label = label;
        this.rowDelta = rowDelta;
        this.columnDelta = columnDelta;
    }

    /**
     * Row offset applied for each consecutive cell.
     *
     * @return row delta
     */
    public int rowDelta() {
        return this.rowDelta;
    }

    /**
     * Column offset applied for each consecutive cell.
     *
     * @return column delta
     */
    public int columnDelta() {
        return this.columnDelta;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
