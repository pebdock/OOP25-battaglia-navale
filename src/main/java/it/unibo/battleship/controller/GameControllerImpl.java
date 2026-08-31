package it.unibo.battleship.controller;

import it.unibo.battleship.model.Board;
import it.unibo.battleship.model.BoardImpl;
import it.unibo.battleship.model.BoardSnapshot;
import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.DoubleShotStatus;
import it.unibo.battleship.model.Game;
import it.unibo.battleship.model.GameImpl;
import it.unibo.battleship.model.GamePhase;
import it.unibo.battleship.model.GameRuleException;
import it.unibo.battleship.model.GameSnapshot;
import it.unibo.battleship.model.Player;
import it.unibo.battleship.model.PlayerId;
import it.unibo.battleship.model.RandomEventResult;
import it.unibo.battleship.model.Rotation;
import it.unibo.battleship.model.RuleViolation;
import it.unibo.battleship.model.Ship;
import it.unibo.battleship.model.ShipId;
import it.unibo.battleship.model.ShipType;
import it.unibo.battleship.model.ShotKind;
import it.unibo.battleship.model.ShotOutcome;
import it.unibo.battleship.model.ShotResult;
import it.unibo.battleship.model.SonarResult;
import it.unibo.battleship.model.TurnResult;
import it.unibo.battleship.model.shot.DoubleShotStrategy;
import it.unibo.battleship.model.shot.NormalShotStrategy;
import it.unibo.battleship.model.shot.SequentialShotStrategy;
import it.unibo.battleship.model.shot.ShotStrategy;
import it.unibo.battleship.model.visibility.InvisibleSubmarinePolicy;
import it.unibo.battleship.model.visibility.OwnerOnlyVisibilityPolicy;
import it.unibo.battleship.view.BattleshipView;
import it.unibo.battleship.model.FleetRules;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Owns match and placement state and updates the view after each model change.
 */
public final class GameControllerImpl implements GameController {

    private static final int SEQUENTIAL_LENGTH = 3;
    private static final FleetRules PLACEMENT_FLEET_RULES = FleetRules.withArmoredShip();

    private final List<Coordinate> pendingTargets = new ArrayList<>();
    private final Map<PlayerId, String> harborNames = new EnumMap<>(PlayerId.class);
    private final Map<PlayerId, Board> placementBoards = new EnumMap<>(PlayerId.class);
    private final Map<PlayerId, Map<ShipType, Integer>> placementCounts = new EnumMap<>(PlayerId.class);

    private BattleshipView view;
    private Game game;
    private PlayerId placingPlayer = PlayerId.PLAYER1;
    private RandomGenerator setupRandom;

    @Override
    public void attachView(final BattleshipView attachedView) {
        this.view = Objects.requireNonNull(attachedView, "view");
        this.view.setController(this);
    }

    @Override
    public void startPlacement(final String firstHarbor, final String secondHarbor) {
        this.requireView();
        this.resetSession();
        this.harborNames.put(PlayerId.PLAYER1, firstHarbor);
        this.harborNames.put(PlayerId.PLAYER2, secondHarbor);
        this.placementBoards.put(PlayerId.PLAYER1, new BoardImpl(BoardImpl.REQUIRED_SIZE, PLACEMENT_FLEET_RULES));
        this.placementBoards.put(PlayerId.PLAYER2, new BoardImpl(BoardImpl.REQUIRED_SIZE, PLACEMENT_FLEET_RULES));
        this.placementCounts.put(PlayerId.PLAYER1, new EnumMap<>(ShipType.class));
        this.placementCounts.put(PlayerId.PLAYER2, new EnumMap<>(ShipType.class));
        this.setupRandom = new Random();
        this.beginPlacement(PlayerId.PLAYER1, "Select a ship, choose its rotation, then click its first cell.");
    }

    @Override
    public void placeShip(final Coordinate origin, final ShipType type, final Rotation rotation) {
        this.requireView();
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(rotation, "rotation");
        final Map<ShipType, Integer> counts = this.placementCounts.get(this.placingPlayer);
        final int number = counts.getOrDefault(type, 0) + 1;
        if (number > PLACEMENT_FLEET_RULES.quantity(type)) {
            this.refreshPlacement("All " + typeLabel(type) + " ships have already been placed.");
            return;
        }
        try {
            final Ship ship = Ship.place(
                new ShipId(
                    this.placingPlayer.name().toLowerCase(Locale.ROOT)
                        + "-" + type.name().toLowerCase(Locale.ROOT) + "-" + number
                ),
                type,
                origin,
                rotation
            );
            this.placementBoards.get(this.placingPlayer).placeShip(ship);
            counts.put(type, number);
            this.refreshPlacement(typeLabel(type) + " placed at " + label(origin) + ".");
        } catch (final GameRuleException exception) {
            this.refreshPlacement(placementViolationMessage(exception.violation()));
        }
    }

    @Override
    public void resetCurrentFleet() {
        this.requireView();
        this.placementBoards.put(this.placingPlayer, new BoardImpl(BoardImpl.REQUIRED_SIZE, PLACEMENT_FLEET_RULES));
        this.placementCounts.put(this.placingPlayer, new EnumMap<>(ShipType.class));
        this.refreshPlacement("The fleet has been reset. Place the ships again.");
    }

    @Override
    public void confirmCurrentFleet() {
        this.requireView();
        if (!this.placementBoards.get(this.placingPlayer).hasCompleteFleet()) {
            this.refreshPlacement("Place all ships before confirming the fleet.");
            return;
        }
        if (this.placingPlayer == PlayerId.PLAYER1) {
            final String next = this.harborNames.get(PlayerId.PLAYER2);
            this.view.showPrivacy("Pass the device to " + next + " for fleet placement.");
            this.view.showInfo("Fleet placement", "Pass the device to " + next + ".");
            this.beginPlacement(PlayerId.PLAYER2, "Select a ship, choose its rotation, then click its first cell.");
            return;
        }
        this.startGame(
            this.harborNames.get(PlayerId.PLAYER1),
            this.harborNames.get(PlayerId.PLAYER2),
            this.placementBoards.get(PlayerId.PLAYER1),
            this.placementBoards.get(PlayerId.PLAYER2),
            this.setupRandom
        );
    }

    @Override
    public void handleTarget(final Coordinate target, final ActionMode mode, final ShotDirection direction) {
        this.requireView();
        if (this.game == null || this.game.phase() != GamePhase.IN_PROGRESS) {
            return;
        }
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(mode, "mode");
        final GameSnapshot snapshot = this.game.snapshotFor(this.game.currentPlayer());
        switch (mode) {
            case NORMAL -> this.performShot(ShotKind.NORMAL, List.of(target));
            case DOUBLE -> this.selectDoubleTarget(target, snapshot);
            case SEQUENTIAL -> this.performSequentialShot(target, direction, snapshot);
            case SONAR -> this.performSonar(target, snapshot);
        }
    }

    @Override
    public void actionModeChanged() {
        this.pendingTargets.clear();
        if (this.game != null) {
            this.refreshGame();
        }
    }

    @Override
    public void returnToSetup() {
        this.requireView();
        this.resetSession();
        this.view.showSetup();
    }

    @Override
    public void onRandomEventTick() {
        if (this.view == null || this.game == null || this.game.phase() != GamePhase.IN_PROGRESS) {
            return;
        }
        final var result = this.game.triggerRandomEvent();
        if (result.isPresent()) {
            final RandomEventResult randomEvent = result.orElseThrow();
            this.view.appendLog("The storm moved the harbor’s unsunk ship «"
                + this.harborNames.get(randomEvent.boardOwner()) + "»; Damage saved.");
        } else {
            this.view.appendLog("The storm passed without moving any ships.");
        }
        this.refreshGame();
    }

    private void beginPlacement(final PlayerId player, final String status) {
        this.placingPlayer = player;
        this.refreshPlacement(status);
    }

    private void refreshPlacement(final String status) {
        final Board board = this.placementBoards.get(this.placingPlayer);
        final BoardSnapshot snapshot = board.snapshot(
            this.placingPlayer,
            this.placingPlayer,
            new OwnerOnlyVisibilityPolicy()
        );
        this.view.showPlacement(
            "Fleet placement — " + this.harborNames.get(this.placingPlayer),
            status,
            snapshot,
            board.hasCompleteFleet()
        );
    }

    private void startGame(
        final String first,
        final String second,
        final Board firstBoard,
        final Board secondBoard,
        final RandomGenerator random
    ) {
        Objects.requireNonNull(random, "random");
        this.view.stopRandomEventTimer();
        this.game = createGame(firstBoard, secondBoard, random);
        this.harborNames.clear();
        this.harborNames.put(PlayerId.PLAYER1, first);
        this.harborNames.put(PlayerId.PLAYER2, second);
        this.pendingTargets.clear();
        this.view.clearLog();
        this.game.start();
        this.view.appendLog("Fleets deployed. First move: " + first + ".");
        this.view.startRandomEventTimer();
        this.refreshGame();
    }

    private void refreshGame() {
        final PlayerId viewer = this.game.currentPlayer();
        final GameSnapshot snapshot = this.game.snapshotFor(viewer);
        this.view.showGame(
            snapshot,
            "Turn: " + this.harborNames.get(viewer),
            formatAbilities(snapshot),
            "Your board — " + this.harborNames.get(viewer),
            "Opponent — " + this.harborNames.get(viewer.other()),
            List.copyOf(this.pendingTargets)
        );
    }

    private void selectDoubleTarget(final Coordinate coordinate, final GameSnapshot snapshot) {
        if (!snapshot.doubleShot().ready()) {
            this.showStatus("Double Shot becomes available after three hits.");
            return;
        }
        if (this.pendingTargets.contains(coordinate)) {
            this.showStatus("This cell has already been selected.");
            return;
        }
        this.pendingTargets.add(coordinate);
        if (this.pendingTargets.size() < 2) {
            this.view.appendLog("The first cell for the Double Shot has been selected: " + label(coordinate) + ".");
            this.refreshGame();
            return;
        }
        final List<Coordinate> targets = List.copyOf(this.pendingTargets);
        this.pendingTargets.clear();
        this.performShot(ShotKind.DOUBLE, targets);
    }

    private void performSequentialShot(
        final Coordinate start,
        final ShotDirection direction,
        final GameSnapshot snapshot
    ) {
        if (!snapshot.sequentialShotAvailable()) {
            this.showStatus("Sequential Shot has already been used.");
            return;
        }
        Objects.requireNonNull(direction, "direction");
        final int size = BoardImpl.REQUIRED_SIZE;
        final List<Coordinate> targets = new ArrayList<>();
        for (int offset = 0; offset < SEQUENTIAL_LENGTH; offset++) {
            final int row = start.row() + direction.rowDelta() * offset;
            final int column = start.column() + direction.columnDelta() * offset;
            if (row < 0 || column < 0 || row >= size || column >= size) {
                this.showStatus("Three cells in this direction are out of bounds.");
                return;
            }
            targets.add(new Coordinate(row, column));
        }
        this.performShot(ShotKind.SEQUENTIAL, targets);
    }

    private void performShot(final ShotKind kind, final List<Coordinate> targets) {
        final PlayerId acting = this.game.currentPlayer();
        try {
            final TurnResult result = this.game.playTurn(kind, targets);
            this.view.appendLog(this.harborNames.get(acting) + ": " + actionName(kind) + ".");
            result.shots().forEach(shot -> this.view.appendLog(
                "  " + label(shot.target()) + " — " + outcomeName(shot)
            ));
            if (result.shots().stream().anyMatch(shot -> shot.outcome() != ShotOutcome.MISS)) {
                this.view.beep();
            }
            this.afterAction(acting);
        } catch (final GameRuleException exception) {
            this.showStatus(violationMessage(exception.violation()));
        }
    }

    private void performSonar(final Coordinate center, final GameSnapshot snapshot) {
        if (!snapshot.sonarAvailable()) {
            this.showStatus("Sonar has already been used.");
            return;
        }
        final PlayerId acting = this.game.currentPlayer();
        try {
            final SonarResult result = this.game.useSonar(center);
            this.view.appendLog(this.harborNames.get(acting) + ": Sonar in " + label(center)
                + " ship cells detected: " + result.detectedCells() + ".");
            this.afterAction(acting);
        } catch (final GameRuleException exception) {
            this.showStatus(violationMessage(exception.violation()));
        }
    }

    private void afterAction(final PlayerId acting) {
        this.pendingTargets.clear();
        if (this.game.phase() == GamePhase.FINISHED) {
            this.view.stopRandomEventTimer();
            this.refreshGame();
            final String winner = this.harborNames.get(this.game.winner().orElseThrow());
            this.view.appendLog("Champion: " + winner + "!");
            this.view.showChampion(winner);
            return;
        }
        if (this.game.currentPlayer() != acting) {
            this.passTurn();
        } else {
            this.refreshGame();
        }
    }

    private void passTurn() {
        final String next = this.harborNames.get(this.game.currentPlayer());
        this.view.showPrivacy("Pass the turn to the harbor. «" + next + "»");
        this.view.showInfo(
            "Player change",
            "Pass the device to the next player.\nTurn:" + next
        );
        this.refreshGame();
    }

    private void showStatus(final String message) {
        this.view.appendLog("Failed to perform the action: " + message);
        this.view.showWarning(message);
    }

    private void resetSession() {
        if (this.view != null) {
            this.view.stopRandomEventTimer();
        }
        this.game = null;
        this.pendingTargets.clear();
        this.harborNames.clear();
        this.placementBoards.clear();
        this.placementCounts.clear();
        this.setupRandom = null;
    }

    private void requireView() {
        if (this.view == null) {
            throw new IllegalStateException("The view has not been attached");
        }
    }

    private static Game createGame(
        final Board firstBoard,
        final Board secondBoard,
        final RandomGenerator random
    ) {
        final Map<ShotKind, ShotStrategy> strategies = Map.of(
            ShotKind.NORMAL, new NormalShotStrategy(),
            ShotKind.DOUBLE, new DoubleShotStrategy(),
            ShotKind.SEQUENTIAL, new SequentialShotStrategy()
        );
        return new GameImpl(
            new Player(PlayerId.PLAYER1, firstBoard),
            new Player(PlayerId.PLAYER2, secondBoard),
            strategies,
            new InvisibleSubmarinePolicy(new OwnerOnlyVisibilityPolicy()),
            random
        );
    }

    private static String formatAbilities(final GameSnapshot snapshot) {
        final DoubleShotStatus doubleShot = snapshot.doubleShot();
        final String doubleText = doubleShot.ready()
            ? "Ready"
            : doubleShot.progress() + "/" + doubleShot.requiredHits();
        return "Double Shot: " + doubleText
            + "   |   Sequential: " + available(snapshot.sequentialShotAvailable())
            + "   |   Sonar: " + available(snapshot.sonarAvailable());
    }

    private static String available(final boolean value) {
        return value ? "available" : "utilized";
    }

    private static String label(final Coordinate coordinate) {
        return String.valueOf((char) ('A' + coordinate.column())) + (coordinate.row() + 1);
    }

    private static String typeLabel(final ShipType type) {
        return switch (type) {
            case FLAGSHIP -> "Flagship";
            case BATTLESHIP -> "Battleship";
            case CRUISER -> "Cruiser";
            case INVISIBLE_SUBMARINE -> "Invisible submarine";
            case DESTROYER -> "Destroyer";
            case RECON -> "Recon ship";
            case ARMORED_SHIP -> "Armored ship";
        };
    }

    private static String actionName(final ShotKind kind) {
        return switch (kind) {
            case NORMAL -> "Standard Shot";
            case DOUBLE -> "Double Shot";
            case SEQUENTIAL -> "Sequential Shot";
        };
    }

    private static String outcomeName(final ShotResult result) {
        return switch (result.outcome()) {
            case MISS -> "Miss";
            case HIT -> "Hit";
            case SUNK -> "Ship sunk";
            case ARMOR_ABSORBED -> "Armor absorbed the hit";
        };
    }

    private static String placementViolationMessage(final RuleViolation violation) {
        return switch (violation) {
            case SHIP_OVERLAP -> "A ship is already placed on one or more selected cells.";
            case SHIP_OUTSIDE_BOARD -> "The selected ship would leave the board.";
            case FLEET_LIMIT_REACHED -> "All ships of this type have already been placed.";
            case SHIP_ALREADY_PLACED -> "This ship has already been placed.";
            default -> "The selected ship cannot be placed here.";
        };
    }

    private static String violationMessage(final RuleViolation violation) {
        return switch (violation) {
            case OUTSIDE_BOARD -> "Cell is out of bounds.";
            case ALREADY_TARGETED -> "This cell has already been fired at.";
            case DUPLICATE_TARGET -> "A cell cannot be selected twice.";
            case WRONG_TARGET_COUNT -> "The wrong number of cells has been selected for this action.";
            case DOUBLE_SHOT_NOT_READY -> "Double Shot is not ready yet.";
            case SEQUENTIAL_SHOT_NOT_AVAILABLE -> "Sequential Shot has already been used..";
            case SEQUENTIAL_TARGETS_NOT_IN_LINE -> "Select three adjacent cells in a straight line.";
            case SONAR_NOT_AVAILABLE -> "Sonar has already been used.";
            case GAME_NOT_RUNNING -> "The game has not started yet or has already ended.";
            default -> "The action violates the game rules.";
        };
    }
}
