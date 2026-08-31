package it.unibo.battleship.model;

import it.unibo.battleship.model.visibility.ShipVisibilityPolicy;
import it.unibo.battleship.model.visibility.VisibilityContext;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Collection based board implementation.
 */
public final class BoardImpl implements Board {

    public static final int REQUIRED_SIZE = 10;

    private final int size;
    private final FleetRules fleetRules;
    private final Map<Coordinate, Ship> occupiedBy = new HashMap<>();
    private final List<Ship> fleet = new ArrayList<>();
    private final Set<Coordinate> firedAt = new HashSet<>();

    /**
     * Builds a board that uses specified fleet rules.
     *
     * @param size number of rows and columns
     * @param fleetRules immutable fleet rules used by this board
     */
    public BoardImpl(final int size, final FleetRules fleetRules) {
        if (size != REQUIRED_SIZE) {
            throw new IllegalArgumentException("The board must be 10x10");
        }

        this.size = size;
        this.fleetRules = Objects.requireNonNull(fleetRules, "fleetRules");
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public FleetRules fleetRules() {
        return this.fleetRules;
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
        if (sameType >= this.fleetRules.quantity(ship.type())) {
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

        if (!target.isInside(this.size)) {
            throw violation(RuleViolation.OUTSIDE_BOARD, "Target outside board");
        }

        if (this.firedAt.contains(target)) {
            final Ship ship = this.occupiedBy.get(target);

            if (ship == null || ship.isHitAt(target)) {
                throw violation(RuleViolation.ALREADY_TARGETED, "Cell already targeted");
            }
        }
    }

    /**
     * Checks if something was hit by the shoot and tells what happened.
     * 
     * @param target the chosen coordinate to shoot
     * @return if a boat was hit, sunk or not
     */
    private ShotResult resolveValidatedShot(final Coordinate target) {
        final Ship ship = this.occupiedBy.get(target);
        if (ship == null) {
            this.firedAt.add(target);
            return new ShotResult(target, ShotOutcome.MISS);
        }
        final HitEffect effect = ship.registerHit(target);
        if (effect == HitEffect.ABSORBED) {
            return new ShotResult(target, ShotOutcome.ARMOR_ABSORBED);
        }
        this.firedAt.add(target);
        final ShotOutcome outcome = ship.isSunk() ? ShotOutcome.SUNK : ShotOutcome.HIT;
        return new ShotResult(target, outcome);
    }

    @Override
    public boolean hasShips() {
        return !this.fleet.isEmpty();
    }

    @Override
    public boolean hasCompleteFleet() {
        return this.fleetRules.quantities().entrySet().stream()
        .allMatch(entry ->
            this.fleet.stream()
                .filter(ship -> ship.type() == entry.getKey())
                .count() == entry.getValue()
        );
    }

    @Override
    public boolean allShipsSunk() {
        return this.hasShips() && this.fleet.stream().allMatch(Ship::isSunk);
    }

    @Override
    public int scan3x3(
        final Coordinate center,
        final ShipVisibilityPolicy visibilityPolicy
    ) {
        Objects.requireNonNull(center, "center");
        Objects.requireNonNull(visibilityPolicy, "visibilityPolicy");
        if (!center.isInside(this.size)) {
            throw violation(RuleViolation.OUTSIDE_BOARD, "Sonar center outside board");
        }
        int detectedCells = 0;
        for (int row = Math.max(0, center.row() - 1); row <= Math.min(this.size - 1, center.row() + 1); row++) {
            for (int column = Math.max(0, center.column() - 1);
                 column <= Math.min(this.size - 1, center.column() + 1); column++) {
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
    public BoardSnapshot snapshot(final PlayerId owner,
        final PlayerId viewer,
        final ShipVisibilityPolicy visibilityPolicy
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(visibilityPolicy, "visibilityPolicy");
        final VisibilityContext context = new VisibilityContext(owner, viewer);
        final Map<Coordinate, CellState> cells = new HashMap<>();
        final Map<Coordinate, ShipType> visibleShipTypes = new HashMap<>();
        for (int row = 0; row < this.size; row++) {
            for (int column = 0; column < this.size; column++) {
                final Coordinate coordinate = new Coordinate(row, column);
                final Ship ship = this.occupiedBy.get(coordinate);
                final CellState state = this.project(coordinate, ship, context, visibilityPolicy);
                cells.put(coordinate, state);
                if (state == CellState.SHIP && ship != null) {
                    visibleShipTypes.put(coordinate, ship.type());
                }
            }
        }
        return new BoardSnapshot(this.size, cells, visibleShipTypes);
    }

    @Override
    public Optional<ShipMove> moveRandomUnsunkShip(final RandomGenerator random) {
        Objects.requireNonNull(random, "random");
        final List<Ship> movable = this.fleet.stream()
            .filter(ship -> !ship.isSunk())
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        while (!movable.isEmpty()) {
            final Ship ship = movable.remove(random.nextInt(movable.size()));
            final List<Placement> placements = this.validPlacements(ship);
            if (!placements.isEmpty()) {
                final Placement placement = placements.get(random.nextInt(placements.size()));
                return Optional.of(this.applyMove(ship, placement));
            }
        }
        return Optional.empty();
    }

    private List<Placement> validPlacements(final Ship ship) {
        final List<Placement> placements = new ArrayList<>();
        for (final Rotation rotation : Rotation.values()) {
            for (int row = 0; row < this.size; row++) {
                for (int column = 0; column < this.size; column++) {
                    final Coordinate origin = new Coordinate(row, column);
                    final Ship candidate = Ship.place(ship.id(), ship.type(), origin, rotation);
                    if (!Collections.disjoint(candidate.cells(), ship.cells())
                        || candidate.cells().stream()
                            .anyMatch(cell -> !cell.isInside(this.size)
                                || this.firedAt.contains(cell)
                                || this.isOccupiedByAnotherShip(cell, ship))) {
                        continue;
                    }
                    placements.add(new Placement(origin, rotation));
                }
            }
        }
        return placements;
    }

    private boolean isOccupiedByAnotherShip(final Coordinate coordinate, final Ship movingShip) {
        final Ship occupant = this.occupiedBy.get(coordinate);
        return occupant != null && !occupant.id().equals(movingShip.id());
    }

    private ShipMove applyMove(final Ship ship, final Placement placement) {
        final Set<Coordinate> previousCells = ship.cells();
        final Ship moved = ship.relocate(placement.origin(), placement.rotation());

        previousCells.forEach(this.occupiedBy::remove);
        moved.cells().forEach(coordinate -> this.occupiedBy.put(coordinate, moved));
        this.firedAt.addAll(moved.hitCells());
        this.fleet.set(this.fleet.indexOf(ship), moved);

        return new ShipMove(ship.id(), ship.type(), previousCells, moved.cells(), moved.damageCount());
    }

    /**
     * Determines the visible state of one coordinate for the given viewer. 
     * 
     * @param coordinate to project
     * @param ship the ship that occupies the coordinate or null
     * @param context the owner and viewer of the board
     * @param visibilityPolicy the policy to determine ship visibility
     * @return the observable state of the coordinate
     */
    private CellState project(
        final Coordinate coordinate,
        final Ship ship,
        final VisibilityContext context,
        final ShipVisibilityPolicy visibilityPolicy
    ) {
        if (this.firedAt.contains(coordinate)) {
            if (ship == null || !ship.isHitAt(coordinate)) {
                return CellState.MISS;
            }
            return ship.isSunk() ? CellState.SUNK : CellState.HIT;
        }

        if (ship != null) {
            return visibilityPolicy.isVisible(ship, context)
                ? CellState.SHIP
                : CellState.UNKNOWN;
        }

        return context.owner() == context.viewer()
            ? CellState.SEA
            : CellState.UNKNOWN;
    }

    /**
     * Creates an exception describin a game rule violation.
     * 
     * @param reason the rule that was violated
     * @param message diagnostuc message
     * @return the exception describing the violation
     */
    private static GameRuleException violation(final RuleViolation reason, final String message) {
        return new GameRuleException(reason, message);
    }

    private record Placement(Coordinate origin, Rotation rotation) { }
}
