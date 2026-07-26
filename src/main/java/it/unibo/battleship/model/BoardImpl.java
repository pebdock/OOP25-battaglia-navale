package it.unibo.battleship.model;

import it.unibo.battleship.model.visibility.ShipVisibilityPolicy;
import it.unibo.battleship.model.visibility.VisibilityContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Collection based board implementation
 */
public class BoardImpl implements Board {

    public static final int REQUIRED_SIZE = 10;

    private final int size;
    private final Map<Coordinate, Ship> occupiedBy = new HashMap<>();
    private final List<Ship> fleet = new ArrayList<>();
    private final Set<Coordinate> firedAt = new HashSet();

    /**
     * Build a game board with defined size.
     * 
     * @param size number of rows or cols
     */
    public BoardImpl(final int size) {
        if (size != REQUIRED_SIZE) {
            throw new IllegalArgumentException("The board must be 10x10");
        }
        this.size = size;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void placeShip(final Ship ship) {
        Objects.requireNonNull(ship, "ship");
        if (this.fleet.stream().anyMatch(existing -> existing.id().equals(ship.id()))) {
            throw violation(RuleViolation.SHIP_ALREADY_PLACED, "Ship already placed");
        }
        final long sameType = this.fleet.stream()
            .filter(existing -> existing.type() == ship.type())
            .count();
        if (sameType >= ship.type().requiredQuantity()) {
            throw violation(RuleViolation.FLEET_LIMIT_REACHED, "Too many ships of thys type");
        }
        for (final Coordinate coordinate : ship.cells()) {
            if (!coordinate.isInside(this.size)) {
                throw violation(RuleViolation.SHIP_OUTSIDE_BOARD, "Ship outside board");
            }
            if (this.occupiedBy.containsKey(coordinate)) {
                throw violation(RuleViolation.SHIP_OVERLAP, "Ships can't overlap");
            }
        }
        ship.cells().forEach(coordinate -> this.occupiedBy.put(coordinate, ship));
        this.fleet.add(ship);
    }

    @Override
    public List<ShotResult> fireAt(final List<Coordinate> targets) {
        Objects.requireNonNull(targets, "targets");
        final List<Coordinate> copy = List.copyOf(targets);
        if (copy.isEmpty()) {
            throw violation(RuleViolation.WRONG_TARGET_COUNT, "Choose at least one target");
        }
        if (new HashSet<>(copy).size() != copy.size()) {
            throw violation(RuleViolation.DUPLICATE_TARGET, "There is a duplicated target");
        }

        copy.forEach(this::validateTarget);

        final List<ShotResult> results = new ArrayList<>();
        copy.forEach(target -> results.add(this.resolveValidatedShot(target)));
        return List.copyOf(results);
    }

    /**
     * Checks if the target can be shot.
     * 
     * @param target the chosen coordinate to shoot
     */
    private void validateTarget(final Coordinate target) {
        Objects.requireNonNull(target, "target");
        if(!target.isInside(this.size)) {
            throw violation(RuleViolation.OUTSIDE_BOARD, "Target outside board");
        }
        if(this.firedAt.contains(target)) {
            throw violation(RuleViolation.ALREADY_TARGETED, "Cell already targeted");
        }
    }

    /**
     * Checks if something was hit by the shoot and tells what happened
     * 
     * @param target the chosen coordinate to shoot
     * @return if a boat was hit, sunk or not
     */
    private ShotResult resolveValidatedShot(final Coordinate target) {
        this.firedAt.add(target);
        final Ship ship = this.occupiedBy.get(target);
        if (ship == null) {
            return new ShotResult(target, ShotOutcome.MISS);
        }
        ship.registerHit(target);
        final ShotOutcome outcome = ship.isSunk() ? ShotOutcome.SUNK : ShotOutcome.MISS;
        return new ShotResult(target, outcome);
    }

    @Override
    public boolean hasShips() {
        return !this.fleet.isEmpty();
    }

    @Override
    public boolean hasCompleteFleet() {
        return Arrays.stream(ShipType.values()).allMatch(type ->
            this.fleet.stream().filter(ship -> ship.type() == type).count() == type.requiredQuantity()
        );
    }

    @Override
    public boolean allShipsSunk() {
        return this.hasShips() && this.fleet.stream().allMatch(Ship::isSunk);
    }

    @Override
    public int scan3x3(final Coordinate center, final ShipVisibilityPolicy visibilityPolicy) {
        Objects.requireNonNull(center, "center");
        Objects.requireNonNull(visibilityPolicy, "visibilityPolicy");
        if (!center.isInside(this.size)) {
            throw violation(RuleViolation.OUTSIDE_BOARD, "Sonar center outside board");
        }
        int detectedCells = 0;
        for (int row = Math.max(0, center.row() - 1);row <= Math.min(this.size - 1, center.row() + 1); row++) {
            for (int column = Math.max(0, center.column() - 1);
                 row <= Math.min(this.size - 1, center.column() + 1); column++) {
                final Coordinate coordinate = new Coordinate(row, column);
                final Ship ship = this.occupiedBy.get(coordinate);
                if (ship != null && !ship.isSunk() && !ship.isHitAt(coordinate) && visibilityPolicy.isDetectableBySonar(ship)) {
                    detectedCells++;
                }
            }
        }
        return detectedCells;
    }

    @Override
    public BoardSnapshot snapshot(final PlayerId owner, final PlayerId viewer, final ShipVisibilityPolicy visibilityPolicy) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(visibilityPolicy, "visibilityPolicy");
        final VisibilityContext context = new VisibilityContext(owner, viewer);
        final Map<Coordinate, CellState> cells = new HashMap<>();
        for (int row = 0; row < this.size; row++) {
            for (int column = 0; column < this.size; column++) {
                final Coordinate coordinate = new Coordinate(row, column);
                final Ship ship = this.occupiedBy.get(coordinate);
                cells.put(coordinate, this.project(coordinate, ship, context, visibilityPolicy));
            }
        }
        return new BoardSnapshot(this.size, cells);
    }

    private CellState project(){...}; //finire
    private static GameRuleException violation(){...}; //finire

}
