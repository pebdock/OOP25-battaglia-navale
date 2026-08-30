package it.unibo.battleship.view;

import it.unibo.battleship.model.CellState;
import it.unibo.battleship.model.ShipType;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

/**
 * Builds coloured board cells from a snapshot state.
 */
final class BoardCells {

    private static final int CELL_SIZE = 36;
    private static final Color NAVY = new Color(20, 46, 77);
    private static final Color WATER = new Color(218, 239, 250);
    private static final Color UNKNOWN = new Color(176, 211, 229);
    private static final Color SHIP = new Color(95, 112, 129);
    private static final Color HIT = new Color(232, 98, 79);
    private static final Color MISS = new Color(237, 244, 247);
    private static final Color SUNK = new Color(123, 44, 55);
    private static final Color FLAGSHIP_COLOR = new Color(28, 78, 121);
    private static final Color BATTLESHIP_COLOR = new Color(75, 95, 110);
    private static final Color CRUISER_COLOR = new Color(36, 123, 105);
    private static final Color SUBMARINE_COLOR = new Color(92, 67, 132);
    private static final Color DESTROYER_COLOR = new Color(0, 128, 145);
    private static final Color RECON_COLOR = new Color(203, 112, 42);
    private static final Color ARMORED_SHIP_COLOR = new Color(185, 132, 30);

    private BoardCells() { }

    static JButton create(final CellState state, final ShipType shipType) {
        final JButton cell = new JButton(symbol(state, shipType));
        cell.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
        cell.setMargin(new Insets(0, 0, 0, 0));
        cell.setOpaque(true);
        cell.setBackground(color(state, shipType));
        cell.setForeground(state == CellState.SHIP || state == CellState.SUNK ? Color.WHITE : NAVY);
        cell.setToolTipText(stateDescription(state, shipType));
        return cell;
    }

    private static String symbol(final CellState state, final ShipType shipType) {
        if (state == CellState.SHIP && shipType != null) {
            return switch (shipType) {
                case FLAGSHIP -> "A";
                case BATTLESHIP -> "B";
                case CRUISER -> "C";
                case INVISIBLE_SUBMARINE -> "S";
                case DESTROYER -> "D";
                case RECON -> "R";
                case ARMORED_SHIP -> "X";
            };
        }
        return switch (state) {
            case SEA, UNKNOWN -> "·";
            case SHIP -> "■";
            case HIT -> "✕";
            case MISS -> "•";
            case SUNK -> "";
        };
    }

    private static Color color(final CellState state, final ShipType shipType) {
        if (state == CellState.SHIP && shipType != null) {
            return switch (shipType) {
                case FLAGSHIP -> FLAGSHIP_COLOR;
                case BATTLESHIP -> BATTLESHIP_COLOR;
                case CRUISER -> CRUISER_COLOR;
                case INVISIBLE_SUBMARINE -> SUBMARINE_COLOR;
                case DESTROYER -> DESTROYER_COLOR;
                case RECON -> RECON_COLOR;
                case ARMORED_SHIP -> ARMORED_SHIP_COLOR;
            };
        }
        return switch (state) {
            case SEA -> WATER;
            case UNKNOWN -> UNKNOWN;
            case SHIP -> SHIP;
            case HIT -> HIT;
            case MISS -> MISS;
            case SUNK -> SUNK;
        };
    }

    private static String stateDescription(final CellState state, final ShipType shipType) {
        if (state == CellState.SHIP && shipType != null) {
            return switch (shipType) {
                case FLAGSHIP -> "Flagship";
                case BATTLESHIP -> "Battleship";
                case CRUISER -> "Cruiser";
                case INVISIBLE_SUBMARINE -> "Invisible submarine";
                case DESTROYER -> "Destroyer";
                case RECON -> "Recon ship";
                case ARMORED_SHIP -> "Armored ship";
            };
        }
        return switch (state) {
            case SEA -> "Sea";
            case UNKNOWN -> "Unknown cell";
            case SHIP -> "Ship";
            case HIT -> "Hit";
            case MISS -> "Miss";
            case SUNK -> "Sunk ship";
        };
    }
}
