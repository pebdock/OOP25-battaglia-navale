package it.unibo.battleship.view;

import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.DoubleShotStatus;
import it.unibo.battleship.model.GameSnapshot;
import it.unibo.battleship.model.PlayerId;
import it.unibo.battleship.model.RandomEventResult;
import it.unibo.battleship.model.RuleViolation;
import it.unibo.battleship.model.ShipType;
import it.unibo.battleship.model.ShotKind;
import it.unibo.battleship.model.ShotOutcome;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Converts semantic model values into user-facing text.
 */
final class SwingText {

    private SwingText() { }

    /**
     * Formats a coordinate using a letter and a one-based row.
     *
     * @param coordinate coordinate to format
     * @return coordinate label such as {@code A1}
     */
    static String coordinate(final Coordinate coordinate) {
        Objects.requireNonNull(coordinate, "coordinate");

        return String.valueOf(
            (char) ('A' + coordinate.column())
        ) + (coordinate.row() + 1);
    }

    /**
     * Returns the user-facing name of a ship type.
     *
     * @param type ship type
     * @return displayed ship name
     */
    static String shipType(final ShipType type) {
        return switch (Objects.requireNonNull(type, "type")) {
            case FLAGSHIP -> "Flagship";
            case BATTLESHIP -> "Battleship";
            case CRUISER -> "Cruiser";
            case INVISIBLE_SUBMARINE -> "Invisible submarine";
            case DESTROYER -> "Destroyer";
            case RECON -> "Recon ship";
            case ARMORED_SHIP -> "Armored ship";
        };
    }

    /**
     * Returns the user-facing name of a shot kind.
     *
     * @param kind shot kind
     * @return displayed action name
     */
    static String action(final ShotKind kind) {
        return switch (Objects.requireNonNull(kind, "kind")) {
            case NORMAL -> "Standard Shot";
            case DOUBLE -> "Double Shot";
            case SEQUENTIAL -> "Sequential Shot";
        };
    }

    /**
     * Returns the user-facing description of a shot outcome.
     *
     * @param outcome shot outcome
     * @return displayed result description
     */
    static String outcome(final ShotOutcome outcome) {
        return switch (
            Objects.requireNonNull(outcome, "outcome")
        ) {
            case MISS -> "Miss";
            case HIT -> "Hit";
            case SUNK -> "Ship sunk";
            case ARMOR_ABSORBED ->
                "Armor absorbed the impact";
        };
    }

    /**
     * Returns a user-facing explanation of a rule violation.
     *
     * @param violation violated rule
     * @return displayed violation message
     */
    static String violation(final RuleViolation violation) {
        return switch (
            Objects.requireNonNull(violation, "violation")
        ) {
            case OUTSIDE_BOARD ->
                "The selected cell is outside the board.";
            case ALREADY_TARGETED ->
                "This cell has already been fired at.";
            case DUPLICATE_TARGET ->
                "A cell cannot be selected twice.";
            case WRONG_TARGET_COUNT ->
                "The wrong number of targets was selected.";
            case SHIP_OVERLAP ->
                "A ship already occupies one or more cells.";
            case SHIP_OUTSIDE_BOARD ->
                "The ship would leave the board.";
            case SHIP_ALREADY_PLACED ->
                "This ship has already been placed.";
            case FLEET_LIMIT_REACHED ->
                "All ships of this type have been placed.";
            case FLEET_INCOMPLETE ->
                "The fleet is not complete.";
            case DOUBLE_SHOT_NOT_READY ->
                "Double Shot is not ready yet.";
            case SEQUENTIAL_SHOT_NOT_AVAILABLE ->
                "Sequential Shot has already been used.";
            case SEQUENTIAL_TARGETS_NOT_IN_LINE ->
                "Select three consecutive cells in a straight line.";
            case SONAR_NOT_AVAILABLE ->
                "Sonar has already been used.";
            case GAME_NOT_RUNNING ->
                "The game is not currently running.";
            case GAME_ALREADY_STARTED ->
                "The game has already started.";
        };
    }

    /**
     * Returns the text associated with placement feedback.
     *
     * @param feedback semantic placement feedback
     * @return displayed placement message
     */
    static String placementFeedback(
        final PlacementFeedback feedback
    ) {
        return switch (
            Objects.requireNonNull(feedback, "feedback")
        ) {
            case INITIAL ->
                "Select a ship, choose its rotation, then click a cell.";
            case PLACED ->
                "The ship was placed successfully.";
            case RESET ->
                "The fleet was reset. Place the ships again.";
            case INCOMPLETE ->
                "Place every required ship before confirming.";
            case SHIP_OVERLAP ->
                "A ship already occupies one or more selected cells.";
            case SHIP_OUTSIDE_BOARD ->
                "The selected ship would leave the board.";
            case FLEET_LIMIT_REACHED ->
                "All ships of this type have already been placed.";
            case SHIP_ALREADY_PLACED ->
                "This ship has already been placed.";
        };
    }

    /**
     * Formats the availability of the current player's abilities.
     *
     * @param snapshot visible game snapshot
     * @return displayed abilities description
     */
    static String abilities(final GameSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");

        final DoubleShotStatus doubleShot =
            snapshot.doubleShot();

        final String doubleText = doubleShot.ready()
            ? "Ready"
            : doubleShot.progress()
                + "/"
                + doubleShot.requiredHits();

        return "Double Shot: " + doubleText
            + " | Sequential: "
            + availability(snapshot.sequentialShotAvailable())
            + " | Sonar: "
            + availability(snapshot.sonarAvailable());
    }

    private static String availability(
        final boolean available
    ) {
        return available ? "available" : "used";
    }

    /**
     * Formats the result of a random movement event.
     *
     * @param result event result, or empty if no ship could move
     * @param harborNames player harbor names
     * @return text describing the event
     */
    static String randomEvent(
        final Optional<RandomEventResult> result,
        final Map<PlayerId, String> harborNames
    ) {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(harborNames, "harborNames");

        if (result.isEmpty()) {
            return "The storm passed without moving any ship.";
        }

        final RandomEventResult event = result.orElseThrow();
        final String harborName = Objects.requireNonNull(
            harborNames.get(event.boardOwner()),
            "Missing harbor name for " + event.boardOwner()
        );

        return "The storm moved an unsunk ship belonging to "
            + harborName
            + ". Existing damage was preserved.";
    }
}
