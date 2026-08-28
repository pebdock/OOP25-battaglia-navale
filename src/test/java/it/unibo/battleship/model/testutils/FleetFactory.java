package it.unibo.battleship.model.testutils;

import java.util.Objects;
import java.util.Locale;
import java.util.random.RandomGenerator;

import it.unibo.battleship.model.Board;
import it.unibo.battleship.model.BoardImpl;
import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.GameRuleException;
import it.unibo.battleship.model.Rotation;
import it.unibo.battleship.model.Ship;
import it.unibo.battleship.model.ShipId;
import it.unibo.battleship.model.ShipType;

/**
 * Utility for tests: it builds valid complete automatically placed fleets for a local game. 
 */
public final class FleetFactory {

    private static final int BOARD_ATTEMPTS = 100;
    private static final int SHIP_ATTEMPTS = 1_000;

    private FleetFactory() { }

    /**
     * Creates a randomly placed fleet including the special armoured ship.
     *
     * @param random source of randomness
     * @param idPrefix prefix used for unique ship identifiers
     * @return populated 10x10 board
     */
    public static Board createRandomBoard(final RandomGenerator random, final String idPrefix) {
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(idPrefix, "idPrefix");
        for (int boardAttempt = 0; boardAttempt < BOARD_ATTEMPTS; boardAttempt++) {
            final Board board = new BoardImpl(BoardImpl.REQUIRED_SIZE);
            if (placeFleet(board, random, idPrefix)) {
                return board;
            }
        }
        throw new IllegalStateException("Unable to place a complete fleet");
    }

    private static boolean placeFleet(
        final Board board,
        final RandomGenerator random,
        final String idPrefix
    ) {
        int sequence = 0;
        for (final ShipType type : ShipType.values()) {
            for (int quantity = 0; quantity < type.requiredQuantity(); quantity++) {
                final ShipId id = new ShipId(
                    idPrefix + '-' + type.name().toLowerCase(Locale.ROOT) + '-' + sequence
                );
                sequence++;
                if (!placeOne(board, random, id, type)) {
                    return false;
                }
            }
        }
        return board.hasCompleteFleet();
    }

    private static boolean placeOne(
        final Board board,
        final RandomGenerator random,
        final ShipId id,
        final ShipType type
    ) {
        final Rotation[] rotations = Rotation.values();
        for (int attempt = 0; attempt < SHIP_ATTEMPTS; attempt++) {
            final Ship ship = Ship.place(
                id,
                type,
                new Coordinate(random.nextInt(board.size()), random.nextInt(board.size())),
                rotations[random.nextInt(rotations.length)]
            );
            try {
                board.placeShip(ship);
                return true;
            } catch (final GameRuleException ignored) {
                // Try another valid coordinate and rotation on the same fresh board.
            }
        }
        return false;
    }
}