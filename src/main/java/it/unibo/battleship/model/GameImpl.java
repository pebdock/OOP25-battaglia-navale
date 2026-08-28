package it.unibo.battleship.model;

import it.unibo.battleship.model.shot.ShotStrategy;
import it.unibo.battleship.model.shot.SequentialShotStrategy;
import it.unibo.battleship.model.visibility.ShipVisibilityPolicy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * The game engine, turn-based with action checking.
 */
public final class GameImpl implements Game {
    private final Map<PlayerId, Player> players = new EnumMap<>(PlayerId.class);
    private final Map<ShotKind, ShotStrategy> strategies = new EnumMap<>(ShotKind.class);
    private final ShipVisibilityPolicy visibilityPolicy;
    private final RandomGenerator random;
    private GamePhase phase = GamePhase.READY;
    private PlayerId currentPlayer = PlayerId.PLAYER1;
    private PlayerId winner;

    /**
     * Creates a game with two players, strategies for the shots and visibility policy.
     * 
     * @param player1 the first player
     * @param player2 the second player
     * @param strategies strategies associated with each type of shot
     * @param visibilityPolicy policy used for board visibility and sonar
     */
    public GameImpl(
        final Player player1,
        final Player player2,
        final Map<ShotKind, ShotStrategy> strategies,
        final ShipVisibilityPolicy visibilityPolicy
    ) {
        this(player1, player2, strategies, visibilityPolicy, RandomGenerator.getDefault());
    }

    /**
     * Creates a game with an injectable random source for deterministic tests.
     *
     * @param player1 the first player
     * @param player2 the second player
     * @param strategies strategies associated with shot types
     * @param visibilityPolicy board visibility policy
     * @param random source used by random events
     */
    public GameImpl(
        final Player player1,
        final Player player2,
        final Map<ShotKind, ShotStrategy> strategies,
        final ShipVisibilityPolicy visibilityPolicy,
        final RandomGenerator random
    ) {
        this.addPlayer(Objects.requireNonNull(player1, "player1"));
        this.addPlayer(Objects.requireNonNull(player2, "player2"));
        Objects.requireNonNull(strategies, "strategies");
        for (final ShotKind kind : ShotKind.values()) {
            ShotStrategy strategy = strategies.get(kind);
            if (strategy == null && kind == ShotKind.SEQUENTIAL) {
                strategy = new SequentialShotStrategy();
            }
            if (strategy == null) {
                throw new IllegalArgumentException("Missing strategy for " + kind);
            }
            this.strategies.put(kind, strategy);
        }
        this.visibilityPolicy = Objects.requireNonNull(visibilityPolicy, "visibilityPolicy");
        this.random = Objects.requireNonNull(random, "random");
    }

    /**
     * Add a player to the game.
     * 
     * @param player the player to add
     */
    private void addPlayer(final Player player) {
        if (this.players.putIfAbsent(player.id(), player) != null) {
            throw new IllegalArgumentException("Duplicate player id " + player.id());
        }
    }

    @Override
    public void start() {
        if (this.phase != GamePhase.READY) {
            throw new GameRuleException(RuleViolation.GAME_ALREADY_STARTED, "Game already started");
        }
        if (this.players.size() != PlayerId.values().length || this.players.values().stream()
                .anyMatch(player -> !player.board().hasCompleteFleet())
            ) {
            throw new GameRuleException(RuleViolation.FLEET_INCOMPLETE, "The players must complete their fleet");
        }
        this.phase = GamePhase.IN_PROGRESS;
    }

    @Override
    public TurnResult playTurn(final ShotKind kind, final List<Coordinate> targets) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(targets, "targets");
        if (this.phase != GamePhase.IN_PROGRESS) {
            throw new GameRuleException(RuleViolation.GAME_NOT_RUNNING, "game not running");
        }

        final PlayerId actingId = this.currentPlayer;
        final Player actingPlayer = this.players.get(actingId);
        final Player targetPlayer = this.players.get(actingId.other());
        if (!actingPlayer.canUse(kind)) {
            final RuleViolation violation = kind == ShotKind.SEQUENTIAL
                ? RuleViolation.SEQUENTIAL_SHOT_NOT_AVAILABLE
                : RuleViolation.DOUBLE_SHOT_NOT_READY;
            throw new GameRuleException(violation, "The selected special shot is not available");
        }

        final List<ShotResult> results = this.strategies.get(kind).execute(targetPlayer.board(), targets);
        final boolean hasHit = results.stream().anyMatch(ShotResult::isHit);
        if (kind == ShotKind.NORMAL && hasHit) {
            actingPlayer.registerSingleHit();
        } else if (kind == ShotKind.DOUBLE) {
            actingPlayer.consumeDoubleShot();
        } else if (kind == ShotKind.SEQUENTIAL) {
            actingPlayer.consumeSequentialShot();
        }

        if (targetPlayer.board().allShipsSunk()) {
            this.phase = GamePhase.FINISHED;
            this.winner = actingId;
            return new TurnResult(
                actingId,
                results, 
                this.phase,
                Optional.empty(),
                Optional.of(actingId)
            );
        }

        this.currentPlayer = hasHit ? actingId : actingId.other();
        return new TurnResult(
            actingId,
            results,
            this.phase,
            Optional.of(this.currentPlayer),
            Optional.empty()
        );
    }

    @Override
    public SonarResult useSonar(final Coordinate center) {
        Objects.requireNonNull(center, "center");
        if (this.phase != GamePhase.IN_PROGRESS) {
            throw new GameRuleException(RuleViolation.GAME_NOT_RUNNING, "Game not running");
        }
        final PlayerId actingId = this.currentPlayer;
        final Player actingPlayer = this.players.get(actingId);
        if (!actingPlayer.sonarAvailable()) {
            throw new GameRuleException(RuleViolation.SONAR_NOT_AVAILABLE, "Sonar already used");
        }

        final int detectedCells = this.players.get(actingId.other()).board().scan3x3(center, this.visibilityPolicy);
        actingPlayer.consumeSonar();
        this.currentPlayer = actingId.other();
        return new SonarResult(actingId, center, detectedCells, this.currentPlayer);
    }

    @Override
    public GameSnapshot snapshotFor(final PlayerId viewer) {
        Objects.requireNonNull(viewer, "viewer");
        final Player own = this.players.get(viewer);
        final Player opponent = this.players.get(viewer.other());
        if (own == null || opponent == null) {
            throw new IllegalArgumentException("Unknown viewer" + viewer);
        }
        return new GameSnapshot(
            this.phase,
            viewer, 
            this.currentPlayer,
            this.winner(),
            own.board().snapshot(viewer, viewer, this.visibilityPolicy),
            opponent.board().snapshot(viewer.other(), viewer, this.visibilityPolicy),
            own.doubleShotStatus(),
            own.sonarAvailable(),
            own.sequentialShotAvailable()
        );
    }

    @Override
    public GamePhase phase() {
        return this.phase;
    }

    @Override
    public PlayerId currentPlayer() {
        return this.currentPlayer;
    }

    @Override
    public Optional<PlayerId> winner() {
        return Optional.ofNullable(this.winner);
    }

    @Override
    public Optional<RandomEventResult> triggerRandomEvent() {
        if (this.phase != GamePhase.IN_PROGRESS) {
            throw new GameRuleException(RuleViolation.GAME_NOT_RUNNING, "Game not running");
        }
        final PlayerId first = this.random.nextBoolean() ? PlayerId.PLAYER1 : PlayerId.PLAYER2;
        for (final PlayerId playerId : List.of(first, first.other())) {
            final Optional<ShipMove> move = this.players.get(playerId).board()
                .moveRandomUnsunkShip(this.random);
            if (move.isPresent()) {
                return Optional.of(new RandomEventResult(playerId, move.orElseThrow()));
            }
        }
        return Optional.empty();
    }
}
