package it.unibo.battleship.controller;

import it.unibo.battleship.model.Board;
import it.unibo.battleship.model.BoardImpl;
import it.unibo.battleship.model.BoardSnapshot;
import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.FleetRules;
import it.unibo.battleship.model.Game;
import it.unibo.battleship.model.GamePhase;
import it.unibo.battleship.model.GameRuleException;
import it.unibo.battleship.model.GameSnapshot;
import it.unibo.battleship.model.PlayerId;
import it.unibo.battleship.model.RandomEventResult;
import it.unibo.battleship.model.Rotation;
import it.unibo.battleship.model.RuleViolation;
import it.unibo.battleship.model.Ship;
import it.unibo.battleship.model.ShipId;
import it.unibo.battleship.model.ShipType;
import it.unibo.battleship.model.ShotKind;
import it.unibo.battleship.model.ShotResult;
import it.unibo.battleship.model.SonarResult;
import it.unibo.battleship.model.TurnResult;
import it.unibo.battleship.model.factory.GameFactory;
import it.unibo.battleship.model.visibility.OwnerOnlyVisibilityPolicy;
import it.unibo.battleship.view.BattleshipView;
import it.unibo.battleship.view.BattleshipViewObserver;
import it.unibo.battleship.view.GameViewState;
import it.unibo.battleship.view.PlacementFeedback;
import it.unibo.battleship.view.PlacementViewState;
import it.unibo.battleship.view.ShotDirection;
import it.unibo.battleship.view.HandoffReason;
import it.unibo.battleship.view.HandoffViewState;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Owns match and placement state and updates the view after each model change.
 */
public final class BattleshipController
        implements BattleshipViewObserver {

    private static final int SEQUENTIAL_LENGTH = 3;

    private final List<Coordinate> pendingTargets =
        new ArrayList<>();

    private final Map<PlayerId, String> harborNames =
        new EnumMap<>(PlayerId.class);

    private final Map<PlayerId, Board> placementBoards =
        new EnumMap<>(PlayerId.class);

    private final Map<PlayerId, Map<ShipType, Integer>>
        placementCounts = new EnumMap<>(PlayerId.class);

    private final BattleshipView view;
    private final GameFactory gameFactory;
    private final RandomGenerator random;

    private Game game;
    private PlayerId placingPlayer = PlayerId.PLAYER1;
    private Optional<HandoffReason> pendingHandoff = Optional.empty();

    /**
     * Coordinates one view and the game model using injected collaborators.
     *
     * @param view view receiving presentation updates
     * @param gameFactory factory used to create configured games
     * @param random random source used by the session
     * @throws NullPointerException if a collaborator is null
     */
    public BattleshipController(
            final BattleshipView view,
            final GameFactory gameFactory,
            final RandomGenerator random) {
        this.view = Objects.requireNonNull(view, "view");
        this.gameFactory = Objects.requireNonNull(
            gameFactory,
            "gameFactory"
        );
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public void onSetupSubmitted(
            final String firstHarbor,
            final String secondHarbor,
            final boolean useArmoredShip) {
        this.resetSession();

        final FleetRules rules = useArmoredShip
            ? FleetRules.withArmoredShip()
            : FleetRules.classic();

        this.harborNames.put(PlayerId.PLAYER1, firstHarbor);
        this.harborNames.put(PlayerId.PLAYER2, secondHarbor);

        this.placementBoards.put(
            PlayerId.PLAYER1,
            new BoardImpl(BoardImpl.REQUIRED_SIZE, rules)
        );
        this.placementBoards.put(
            PlayerId.PLAYER2,
            new BoardImpl(BoardImpl.REQUIRED_SIZE, rules)
        );

        this.placementCounts.put(
            PlayerId.PLAYER1,
            new EnumMap<>(ShipType.class)
        );
        this.placementCounts.put(
            PlayerId.PLAYER2,
            new EnumMap<>(ShipType.class)
        );

        this.beginPlacement(
            PlayerId.PLAYER1,
            PlacementFeedback.INITIAL
        );
    }

    @Override
    public void onShipPlacementRequested(
            final Coordinate origin,
            final ShipType type,
            final Rotation rotation) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(rotation, "rotation");

        final Board board = this.currentPlacementBoard();
        final Map<ShipType, Integer> counts =
            this.currentPlacementCounts();

        final int number = counts.getOrDefault(type, 0) + 1;

        if (number > board.fleetRules().quantity(type)) {
            this.refreshPlacement(
                PlacementFeedback.FLEET_LIMIT_REACHED
            );
            return;
        }

        try {
            final Ship ship = Ship.place(
                this.nextShipId(type),
                type,
                origin,
                rotation
            );

            board.placeShip(ship);
            counts.put(type, number);

            this.refreshPlacement(PlacementFeedback.PLACED);
        } catch (final GameRuleException exception) {
            this.refreshPlacement(
                PlacementFeedback.fromViolation(
                    exception.violation()
                )
            );
        }
    }

    @Override
    public void onFleetResetRequested() {
        final Board currentBoard = this.currentPlacementBoard();

        this.placementBoards.put(
            this.placingPlayer,
            new BoardImpl(
                currentBoard.size(),
                currentBoard.fleetRules()
            )
        );

        this.placementCounts.put(
            this.placingPlayer,
            new EnumMap<>(ShipType.class)
        );

        this.refreshPlacement(PlacementFeedback.RESET);
    }

    @Override
    public void onFleetConfirmed() {
        if (!this.currentPlacementBoard().hasCompleteFleet()) {
            this.refreshPlacement(PlacementFeedback.INCOMPLETE);
            return;
        }

        if (this.placingPlayer == PlayerId.PLAYER1) {
            this.pendingHandoff = Optional.of(
                HandoffReason.PLACEMENT
            );

            this.view.stopRandomEventTimer();
            this.view.showPrivacy(new HandoffViewState(
                this.harborNames.get(PlayerId.PLAYER2),
                HandoffReason.PLACEMENT
            ));
            return;
        }

        this.startGame(
            this.placementBoards.get(PlayerId.PLAYER1),
            this.placementBoards.get(PlayerId.PLAYER2)
        );
    }

    @Override
    public void onNormalShotRequested(
            final Coordinate target) {
        if (this.isGameRunning()) {
            this.performShot(
                ShotKind.NORMAL,
                List.of(target)
            );
        }
    }

    @Override
    public void onDoubleShotTargetSelected(
            final Coordinate target) {
        if (this.isGameRunning()) {
            this.selectDoubleTarget(target);
        }
    }

    @Override
    public void onSequentialShotRequested(
            final Coordinate start,
            final ShotDirection direction) {
        if (this.isGameRunning()) {
            this.performSequentialShot(start, direction);
        }
    }

    @Override
    public void onSonarRequested(
            final Coordinate center) {
        if (this.isGameRunning()) {
            this.performSonar(center);
        }
    }

    @Override
    public void onActionSelectionChanged() {
        this.pendingTargets.clear();

        if (this.game != null) {
            this.refreshGame();
        }
    }

    @Override
    public void onHandoffConfirmed() {
        if (this.pendingHandoff.isEmpty()) {
            return;
        }

        final HandoffReason reason =
            this.pendingHandoff.orElseThrow();

        this.pendingHandoff = Optional.empty();

        switch (reason) {
            case PLACEMENT ->
                this.beginPlacement(
                    PlayerId.PLAYER2,
                    PlacementFeedback.INITIAL
                );

            case GAME_START, TURN_CHANGE -> {
                if (this.game == null) {
                    throw new IllegalStateException(
                        "Cannot resume a missing game"
                    );
                }

                this.view.startRandomEventTimer();
                this.refreshGame();
            }
        }
    }

    @Override
    public void onNewGameRequested() {
        this.resetSession();
        this.view.showSetup();
    }

    @Override
    public void onRandomEventElapsed() {
        if (!this.isGameRunning()
            || this.pendingHandoff.isPresent()
            || !this.pendingTargets.isEmpty()) {
            return;
        }

        try {
            final Optional<RandomEventResult> result =
                this.game.triggerRandomEvent();

            this.view.appendRandomEvent(
                result,
                Map.copyOf(this.harborNames)
            );
            this.refreshGame();
        } catch (final GameRuleException exception) {
            this.view.showRuleViolation(
                exception.violation()
            );
        }
    }

    private boolean isGameRunning() {
        return this.game != null
            && this.game.phase() == GamePhase.IN_PROGRESS;
    }

    private void beginPlacement(
            final PlayerId player,
            final PlacementFeedback feedback) {
        this.placingPlayer =
            Objects.requireNonNull(player, "player");

        this.refreshPlacement(feedback);
    }

    private void refreshPlacement(
            final PlacementFeedback feedback) {
        final Board board = this.currentPlacementBoard();

        final BoardSnapshot snapshot = board.snapshot(
            this.placingPlayer,
            this.placingPlayer,
            new OwnerOnlyVisibilityPolicy()
        );

        this.view.showPlacement(new PlacementViewState(
            this.harborNames.get(this.placingPlayer),
            snapshot,
            board.fleetRules(),
            Map.copyOf(this.currentPlacementCounts()),
            feedback,
            board.hasCompleteFleet()
        ));
    }

    private void startGame(
        final Board firstBoard,
        final Board secondBoard) {
        this.view.stopRandomEventTimer();

        this.game = this.gameFactory.create(
            Objects.requireNonNull(firstBoard, "firstBoard"),
            Objects.requireNonNull(secondBoard, "secondBoard"),
            this.random
        );

        this.pendingTargets.clear();
        this.view.clearLog();
        this.game.start();

        this.view.appendGameStarted(
            this.harborNames.get(PlayerId.PLAYER1)
        );

        this.pendingHandoff = Optional.of(
            HandoffReason.GAME_START
        );

        this.view.showPrivacy(new HandoffViewState(
            this.harborNames.get(PlayerId.PLAYER1),
            HandoffReason.GAME_START
        ));
    }

    private void refreshGame() {
        if (this.game == null) {
            return;
        }

        final PlayerId viewer = this.game.currentPlayer();
        final GameSnapshot snapshot =
            this.game.snapshotFor(viewer);

        this.view.showGame(new GameViewState(
            snapshot,
            Map.copyOf(this.harborNames),
            List.copyOf(this.pendingTargets)
        ));
    }

    private void selectDoubleTarget(
            final Coordinate coordinate) {
        Objects.requireNonNull(coordinate, "coordinate");

        final GameSnapshot snapshot =
            this.game.snapshotFor(this.game.currentPlayer());

        if (!snapshot.doubleShot().ready()) {
            this.view.showRuleViolation(
                RuleViolation.DOUBLE_SHOT_NOT_READY
            );
            return;
        }

        if (this.pendingTargets.contains(coordinate)) {
            this.view.showRuleViolation(
                RuleViolation.DUPLICATE_TARGET
            );
            return;
        }

        this.pendingTargets.add(coordinate);

        if (this.pendingTargets.size() == 1) {
            this.view.appendDoubleTargetSelected(coordinate);
            this.refreshGame();
            return;
        }

        final List<Coordinate> targets =
            List.copyOf(this.pendingTargets);

        this.pendingTargets.clear();
        this.performShot(ShotKind.DOUBLE, targets);
    }

    private void performSequentialShot(
            final Coordinate start,
            final ShotDirection direction) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(direction, "direction");

        final GameSnapshot snapshot =
            this.game.snapshotFor(this.game.currentPlayer());

        if (!snapshot.sequentialShotAvailable()) {
            this.view.showRuleViolation(
                RuleViolation.SEQUENTIAL_SHOT_NOT_AVAILABLE
            );
            return;
        }

        final List<Coordinate> targets = new ArrayList<>();

        for (int offset = 0;
                offset < SEQUENTIAL_LENGTH;
                offset++) {
            final int row = start.row()
                + direction.rowDelta() * offset;

            final int column = start.column()
                + direction.columnDelta() * offset;

            if (!this.isInsideBoard(row, column)) {
                this.view.showRuleViolation(
                    RuleViolation.OUTSIDE_BOARD
                );
                return;
            }

            targets.add(new Coordinate(row, column));
        }

        this.performShot(
            ShotKind.SEQUENTIAL,
            List.copyOf(targets)
        );
    }

    private void performShot(
            final ShotKind kind,
            final List<Coordinate> targets) {
        final PlayerId actingPlayer =
            this.game.currentPlayer();

        try {
            final TurnResult result =
                this.game.playTurn(kind, targets);

            this.view.appendTurnResult(
                this.harborNames.get(actingPlayer),
                kind,
                result
            );

            if (result.shots().stream()
                    .anyMatch(ShotResult::isHit)) {
                this.view.beep();
            }

            this.pendingTargets.clear();
            this.afterAction(actingPlayer);
        } catch (final GameRuleException exception) {
            this.pendingTargets.clear();
            this.view.showRuleViolation(
                exception.violation()
            );
            this.refreshGame();
        }
    }

    private void performSonar(
            final Coordinate center) {
        Objects.requireNonNull(center, "center");

        final GameSnapshot snapshot =
            this.game.snapshotFor(this.game.currentPlayer());

        if (!snapshot.sonarAvailable()) {
            this.view.showRuleViolation(
                RuleViolation.SONAR_NOT_AVAILABLE
            );
            return;
        }

        final PlayerId actingPlayer =
            this.game.currentPlayer();

        try {
            final SonarResult result =
                this.game.useSonar(center);

            this.view.appendSonarResult(
                this.harborNames.get(actingPlayer),
                result
            );
            this.afterAction(actingPlayer);
        } catch (final GameRuleException exception) {
            this.view.showRuleViolation(
                exception.violation()
            );
            this.refreshGame();
        }
    }

    private void afterAction(
            final PlayerId actingPlayer) {
        this.pendingTargets.clear();

        if (this.game.phase() == GamePhase.FINISHED) {
            this.view.stopRandomEventTimer();
            this.refreshGame();

            final PlayerId winner =
                this.game.winner().orElseThrow();

            this.view.showChampion(
                this.harborNames.get(winner)
            );
            return;
        }

        if (this.game.currentPlayer() != actingPlayer) {
            this.passTurn();
        } else {
            this.refreshGame();
        }
    }

private void passTurn() {
        final String nextHarbor =
            this.harborNames.get(this.game.currentPlayer());

        this.view.stopRandomEventTimer();

        this.pendingHandoff = Optional.of(
            HandoffReason.TURN_CHANGE
        );

        this.view.showPrivacy(new HandoffViewState(
            nextHarbor,
            HandoffReason.TURN_CHANGE
        ));
    }

    private boolean isInsideBoard(
            final int row,
            final int column) {
        return row >= 0
            && column >= 0
            && row < BoardImpl.REQUIRED_SIZE
            && column < BoardImpl.REQUIRED_SIZE;
    }

    private Board currentPlacementBoard() {
        final Board board =
            this.placementBoards.get(this.placingPlayer);

        if (board == null) {
            throw new IllegalStateException(
                "Placement board has not been initialized"
            );
        }

        return board;
    }

    private Map<ShipType, Integer>
            currentPlacementCounts() {
        final Map<ShipType, Integer> counts =
            this.placementCounts.get(this.placingPlayer);

        if (counts == null) {
            throw new IllegalStateException(
                "Placement counters have not been initialized"
            );
        }

        return counts;
    }

    private ShipId nextShipId(
            final ShipType type) {
        final int number = this.currentPlacementCounts()
            .getOrDefault(type, 0) + 1;

        final String playerName = this.placingPlayer.name()
            .toLowerCase(Locale.ROOT);

        final String typeName = type.name()
            .toLowerCase(Locale.ROOT);

        return new ShipId(
            playerName + "-" + typeName + "-" + number
        );
    }

    private void resetSession() {
        this.view.stopRandomEventTimer();
        this.game = null;
        this.pendingHandoff = Optional.empty();
        this.placingPlayer = PlayerId.PLAYER1;
        this.pendingTargets.clear();
        this.harborNames.clear();
        this.placementBoards.clear();
        this.placementCounts.clear();
    }
}
