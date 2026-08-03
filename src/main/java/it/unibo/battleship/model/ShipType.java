package it.unibo.battleship.model;

/**
 * All the different kinds and sizes of the ships.
 */
public enum ShipType {
    FLAGSHIP(5, 1, ShipShape.LINEAR), // ammiraglia
    BATTLESHIP(4, 1, ShipShape.LINEAR), // corazzata
    CRUISER(3, 2, ShipShape.LINEAR), // incrociatore
    INVISIBLE_SUBMARINE(3, 1, ShipShape.LINEAR), // sottomarino invisibile
    DESTROYER(2, 2, ShipShape.LINEAR), // cacciatorpediniere
    RECON(3, 1, ShipShape.L_SHAPED);

    private final int length;
    private final int requiredQuantity;
    private final ShipShape shape;

    /**
     * Constructor for the different possible kinds of ships.
     * 
     * @param length the size of that type of ships
     * @param requiredQuantity the number of that type of ships
     * @param shape the shape of that type of ships
     */
    ShipType(final int length, final int requiredQuantity, final ShipShape shape) {
        this.length = length;
        this.requiredQuantity = requiredQuantity;
        this.shape = shape;
    }

    /**
     * Getter for the length of that type of ships.
     * 
     * @return length of the chosen type of ships
     */
    public int length() {
        return this.length;
    }

    /**
     * Getter for the number of that type of ships.
     * 
     * @return number of the chosen type of ships
     */
    public int requiredQuantity() {
        return this.requiredQuantity;
    }

    /**
     * Getter for the shape of the ships.
     * 
     * @return shape of the chosen type of ships
     */
    public ShipShape shape() {
        return this.shape;
    }
}
