package it.unibo.battleship.model;

/** 
 * Immutable position on a game board.
 * 
 * @param row the zero-based row index
 * @param column the zero-based column index
 */
public record Coordinate(int row, int column) {

    /**
     * Compact constructor that builds a Coordinate.
     * 
     * @throws IllegalArgumentException if row or column are negative
     */
    public Coordinate {
        if (row < 0 || column < 0) {
            throw new IllegalArgumentException("Row and column must be non-negative");
        }
    }

    /**
     * Checks if the coordinate is inside a game board of given size.
     * 
     * @param boardSize the size of the game board
     * @return true if the coordinate is inside the board, false otherwise
     */
    public boolean isInside(final int boardSize) {
        return row < boardSize && column < boardSize;
    }
}
