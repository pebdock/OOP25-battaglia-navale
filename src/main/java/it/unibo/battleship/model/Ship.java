package it.unibo.battleship.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Typed ship with immutable occupied cells with private hit-tracking.
 */
public final class Ship {
    private final ShipId id;
    private final ShipType type;
    private final Set<Coordinate> cells; /* because the same coordinate cannot appear two times, cell
    order is not important and with contains() the search is simple */
    private final Set<Coordinate> hits = new HashSet<>(); /* it changes during the match and registers 
    the hits on the ship's coordinates */

    /**
     * Constructor that verifies that the occupied cells are the same number as the length of the ship.
     * 
     * @param id identifies the single ship
     * @param type one of the types in ShipType
     * @param cells the occupied cells coordinates
     */
    private Ship(final ShipId id, final ShipType type, final Collection<Coordinate> cells) { /* private
        to avoid irregular shapes */
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        Objects.requireNonNull(cells);
        final Set<Coordinate> copy = Set.copyOf(cells); /* without the copy, the caller could edit the
        original collection after the ship building */
        if (copy.size() != type.length() || copy.size() != cells.size()) { /* verifies that the number
            of coordinates is compatible with the ship length and duplicated coordinates */
            throw new IllegalArgumentException("Cells don't match the type of ship");
        }
        this.cells = copy;
    }

    /**
     * Static factory method that builds a ship with a valid shape from a top-left origin and a quarter-turn rotation.
     * 
     * @param id identifies thw single ship
     * @param type one of the types in ShipType
     * @param origin the origin coordinate of the ship
     * @param rotation number of 90° rotations
     * @return the ship with the correct position and shape
     */
    public static Ship place(final ShipId id, final ShipType type, final Coordinate origin, final Rotation rotation) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(rotation, "rotation");
        final List<Offset> rotated = baseOffsets(Objects.requireNonNull(type, "type")); /* gets the initial shape 
        of the ship and saves the offsets in a list */
        for (int turn = 0; turn < rotation.quarterTurns(); turn++) { // repeating the cycle based on the value in rotation
            for (int index = 0; index < rotated.size(); index++) { // theinternal cycle visits every offset of the ship
                final Offset old = rotated.get(index); // retrieves the current offset
                rotated.set(index, new Offset(old.column(), -old.row())); //applies a 90° rotation
            }
        }

        int minimumRow = 0;
        int minimumColumn = 0;
        for (final Offset offset : rotated) { // visits all the rotated offsets and finds the minimum rows and columns values
            minimumRow = Math.min(minimumRow, offset.row());
            minimumColumn = Math.min(minimumColumn, offset.column());
        }

        final Set<Coordinate> cells = new HashSet<>();
        for (final Offset offset : rotated) {
            cells.add(new Coordinate(origin.row() + offset.row() - minimumRow,
             origin.column() + offset.column() - minimumColumn));
            // calculates the final coordinates of the ship in the game grid
        }
        return new Ship(id, type, cells);
    }

    /**
     * Relative coordinates of that type of ship.
     * 
     * @param type tells the shape of the ship
     * @return relative coordinates that show the shape of that kind of ship
     */
    private static List<Offset> baseOffsets(final ShipType type) {
        final List<Offset> offsets = new ArrayList<>();
        if (type.shape() == ShipShape.LINEAR) {
            for (int column = 0; column < type.length(); column++) {
                offsets.add(new Offset(0, column));
            }
        } else {
            offsets.add(new Offset(0, 0));
            offsets.add(new Offset(0, 1));
            offsets.add(new Offset(1, 1));
        }
        return offsets;
    }

    /**
     * Getter of the ship identifier.
     * 
     * @return ship identifier
     */
    public ShipId id() {
        return this.id;
    }

    /**
     * Getter of the ship type.
     * 
     * @return ship type
     */
    public ShipType type() {
        return this.type;
    }

    /**
     * Getter of the ship occupied coordinates.
     * 
     * @return set of occupied coordinates
     */
    public Set<Coordinate> cells() {
        return this.cells;
    }

    /**
     * Tells if the ship has sunk.
     * 
     * @return true if the ship sunk
     */
    public boolean isSunk() {
        return this.hits.containsAll(this.cells); /* if all the occupied coordinates of this ship are inside 
        of hits then the ship sunk */
    }

    /**
     * Verifies if the coordinate is occupied by that ship.
     * 
     * @param coordinate the chosen coordinate
     * @return true if the coordinate is occupied by this ship
     */
    boolean occupies(final Coordinate coordinate) {
        return this.cells.contains(coordinate);
    }

    /**
     * Verifies if the coordinate of that ship has been hit.
     * 
     * @param coordinate the chosen coordinate
     * @return true if the coordinate has been hit
     */
    boolean isHitAt(final Coordinate coordinate) {
        return this.hits.contains(coordinate);
    }

    /**
     * Register a hit coordinate of the ship in the hit set.
     * 
     * @param coordinate the coordinate to be hit
     */
    void registerHit(final Coordinate coordinate) {
        if (!this.occupies(coordinate)) {
            throw new IllegalArgumentException("The ship doesn't occupy " + coordinate);
        }
        this.hits.add(coordinate);
    }

    /**
     * Offset of rows and columns compared to the selected coordinates od the ship.
     * 
     * @param row row offset
     * @param column column offset
     */
    private record Offset(int row, int column) { }
}
