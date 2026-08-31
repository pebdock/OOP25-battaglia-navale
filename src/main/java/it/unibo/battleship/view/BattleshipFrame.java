package it.unibo.battleship.view;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.battleship.controller.ActionMode;
import it.unibo.battleship.controller.GameController;
import it.unibo.battleship.controller.ShotDirection;
import it.unibo.battleship.model.BoardSnapshot;
import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.GamePhase;
import it.unibo.battleship.model.GameSnapshot;
import it.unibo.battleship.model.Rotation;
import it.unibo.battleship.model.ShipType;
import it.unibo.battleship.model.FleetRules;

import javax.swing.JCheckBox;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.Timer;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.List;
import java.util.Objects;

/**
 * Local two-player hot-seat interface.
 */
@SuppressFBWarnings(
    value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
    justification = "The application frame is runtime UI state and is never serialized."
)
public final class BattleshipFrame extends JFrame implements BattleshipView {

    private static final long serialVersionUID = 1L;
    private static final int BOARD_SIZE = 10;
    private static final int RANDOM_EVENT_DELAY_MS = 120_000;
    private static final int MINIMUM_WIDTH = 1_050;
    private static final int MINIMUM_HEIGHT = 760;
    private static final int SETUP_PADDING = 40;
    private static final int PLACEMENT_PADDING = 20;
    private static final int PLACEMENT_SECTION_GAP = 10;
    private static final int PLACEMENT_CONFIRM_GAP = 14;
    private static final int PLACEMENT_HINT_GAP = 12;
    private static final int START_BUTTON_TOP_MARGIN = 22;
    private static final float MAIN_TITLE_FONT_SIZE = 32F;
    private static final float TURN_FONT_SIZE = 18F;
    private static final float PRIVACY_FONT_SIZE = 28F;
    private static final float BOARD_TITLE_FONT_SIZE = 15F;
    private static final String APPLICATION_TITLE = "Battleship";
    private static final String SETUP_CARD = "setup";
    private static final String PLACEMENT_CARD = "placement";
    private static final String GAME_CARD = "game";
    private static final String PRIVACY_CARD = "privacy";
    private static final Color NAVY = new Color(20, 46, 77);
    private static final Color ARMOR = new Color(236, 178, 69);

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(this.cards);
    private final JTextField firstHarbor = new JTextField("Harbor 1", 18);
    private final JTextField secondHarbor = new JTextField("Harbor 2", 18);
    private final JCheckBox includeArmoredShip = new JCheckBox("Include armored ship for both players", false);
    private final JPanel ownGrid = new JPanel();
    private final JPanel opponentGrid = new JPanel();
    private final JPanel placementGrid = new JPanel();
    private final JLabel turnLabel = new JLabel(" ");
    private final JLabel abilitiesLabel = new JLabel(" ");
    private final JLabel ownTitle = new JLabel("Your field", JLabel.CENTER);
    private final JLabel opponentTitle = new JLabel("Opponent's field", JLabel.CENTER);
    private final JLabel privacyLabel = new JLabel("Pass the turn", JLabel.CENTER);
    private final JTextArea logArea = new JTextArea(8, 70);
    private final JComboBox<ActionMode> actionMode = new JComboBox<>(ActionMode.values());
    private final JComboBox<ShotDirection> direction = new JComboBox<>(ShotDirection.values());
    private final JComboBox<PlacementOption> placementShip = new JComboBox<>();
    private final JComboBox<Rotation> placementRotation = new JComboBox<>(Rotation.values());
    private final JButton resetFleet = new JButton("Reset fleet");
    private final JButton confirmFleet = new JButton("Confirm fleet");
    private final JLabel placementTitle = new JLabel(" ", JLabel.CENTER);
    private final JLabel placementStatus = new JLabel(" ", JLabel.CENTER);
    private transient FleetRules displayedFleetRules;

    private transient GameController controller;
    private Timer randomEventTimer;

    /**
     * Creates all screens without starting a game.
     */
    public BattleshipFrame() {
        super(APPLICATION_TITLE);
        this.placementRotation.setRenderer(new RotationRenderer());
        this.applySystemLookAndFeel();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
        this.root.add(this.buildSetupPanel(), SETUP_CARD);
        this.root.add(this.buildPlacementPanel(), PLACEMENT_CARD);
        this.root.add(this.buildGamePanel(), GAME_CARD);
        this.root.add(this.buildPrivacyPanel(), PRIVACY_CARD);
        this.setContentPane(this.root);
        this.pack();
        this.setLocationRelativeTo(null);
        this.cards.show(this.root, SETUP_CARD);
    }

    @Override
    public void setController(final GameController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    @Override
    public void showSetup() {
        this.clearLog();
        this.cards.show(this.root, SETUP_CARD);
    }

    @Override
    public void clearLog() {
        this.logArea.setText("");
    }

    @Override
    public void showPlacement(
        final String title,
        final String status,
        final BoardSnapshot board,
        final FleetRules rules,
        final boolean confirmEnabled
    ) {
        final FleetRules checkedRules = Objects.requireNonNull(rules, "rules");

        if (!checkedRules.equals(this.displayedFleetRules)) {
            this.displayedFleetRules = checkedRules;
            this.configurePlacementShips(checkedRules);
        }

        this.placementTitle.setText(title);
        this.placementStatus.setText(status);
        this.confirmFleet.setEnabled(confirmEnabled);
        this.renderPlacementBoard(board);
        this.cards.show(this.root, PLACEMENT_CARD);
    }

    @Override
    public void showPrivacy(final String message) {
        this.privacyLabel.setText(message);
        this.cards.show(this.root, PRIVACY_CARD);
    }

    @Override
    public void showGame(
        final GameSnapshot snapshot,
        final String turnText,
        final String abilitiesText,
        final String ownBoardTitle,
        final String opponentBoardTitle,
        final List<Coordinate> pendingTargets
    ) {
        this.turnLabel.setText(turnText);
        this.abilitiesLabel.setText(abilitiesText);
        this.ownTitle.setText(ownBoardTitle);
        this.opponentTitle.setText(opponentBoardTitle);
        this.renderBoard(this.ownGrid, snapshot.ownBoard(), false, List.of(), snapshot.phase());
        this.renderBoard(this.opponentGrid, snapshot.opponentBoard(), true, pendingTargets, snapshot.phase());
        this.cards.show(this.root, GAME_CARD);
    }

    @Override
    public void appendLog(final String text) {
        this.logArea.append(text + System.lineSeparator());
        this.logArea.setCaretPosition(this.logArea.getDocument().getLength());
    }

    @Override
    public void showInfo(final String title, final String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void showWarning(final String message) {
        JOptionPane.showMessageDialog(this, message, "Game Rule", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void showChampion(final String winnerName) {
        JOptionPane.showMessageDialog(this, "Champion: " + winnerName + "!");
    }

    @Override
    public void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    @Override
    public void startRandomEventTimer() {
        this.stopRandomEventTimer();
        this.randomEventTimer = new Timer(RANDOM_EVENT_DELAY_MS, event -> {
            if (this.controller != null) {
                this.controller.onRandomEventTick();
            }
        });
        this.randomEventTimer.setRepeats(true);
        this.randomEventTimer.start();
    }

    @Override
    public void stopRandomEventTimer() {
        if (this.randomEventTimer != null) {
            this.randomEventTimer.stop();
            this.randomEventTimer = null;
        }
    }

    private JPanel buildSetupPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(
            SETUP_PADDING,
            SETUP_PADDING,
            SETUP_PADDING,
            SETUP_PADDING
        ));
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        final JLabel title = new JLabel("BATTLESHIP", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, MAIN_TITLE_FONT_SIZE));
        title.setForeground(NAVY);
        constraints.gridy = 0;
        panel.add(title, constraints);

        final JLabel intro = new JLabel(
            "Local two-player game. Each player places their own fleet before the battle.",
            JLabel.CENTER
        );
        constraints.gridy = 1;
        panel.add(intro, constraints);

        constraints.gridwidth = 1;
        constraints.gridy = 2;
        panel.add(new JLabel("Name of the first harbor:"), constraints);
        constraints.gridx = 1;
        panel.add(this.firstHarbor, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        panel.add(new JLabel("Name of the second harbor:"), constraints);

        constraints.gridx = 1;
        panel.add(this.secondHarbor, constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(8, 8, 8, 8);
        panel.add(this.includeArmoredShip, constraints);

        final JButton start = new JButton("Place the fleets");
        start.addActionListener(event -> this.startFromForm());

        constraints.gridx = 0;
        final int gridy = 5;
        constraints.gridy = gridy;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(
            START_BUTTON_TOP_MARGIN,
            8,
            8,
            8
        );
        panel.add(start, constraints);
        return panel;
    }

    private JPanel buildPlacementPanel() {
        final JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(
            PLACEMENT_PADDING,
            PLACEMENT_PADDING,
            PLACEMENT_PADDING,
            PLACEMENT_PADDING
        ));
        this.placementTitle.setFont(this.placementTitle.getFont().deriveFont(Font.BOLD, TURN_FONT_SIZE));
        this.placementTitle.setForeground(NAVY);
        panel.add(this.placementTitle, BorderLayout.NORTH);

        final JPanel center = new JPanel(new BorderLayout(PLACEMENT_SECTION_GAP, PLACEMENT_SECTION_GAP));
        center.add(this.placementGrid, BorderLayout.CENTER);
        final JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(new JLabel("Ship to place:"));
        controls.add(this.placementShip);
        controls.add(Box.createVerticalStrut(PLACEMENT_SECTION_GAP));
        controls.add(new JLabel("Rotation:"));
        controls.add(this.placementRotation);
        controls.add(Box.createVerticalStrut(PLACEMENT_CONFIRM_GAP));
        this.resetFleet.addActionListener(event -> this.controller.resetCurrentFleet());
        controls.add(this.resetFleet);
        controls.add(Box.createVerticalStrut(PLACEMENT_SECTION_GAP));
        this.confirmFleet.addActionListener(event -> this.controller.confirmCurrentFleet());
        controls.add(this.confirmFleet);
        controls.add(Box.createVerticalStrut(PLACEMENT_HINT_GAP));
        controls.add(new JLabel("Click the first cell of the selected ship."));
        controls.add(new JLabel("Ships cannot overlap or leave the board."));
        center.add(controls, BorderLayout.EAST);
        panel.add(center, BorderLayout.CENTER);

        this.placementStatus.setHorizontalAlignment(JLabel.CENTER);
        panel.add(this.placementStatus, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildGamePanel() {
        final JPanel gamePanel = new JPanel(new BorderLayout(8, 8));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        final JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        this.turnLabel.setFont(this.turnLabel.getFont().deriveFont(Font.BOLD, TURN_FONT_SIZE));
        this.turnLabel.setAlignmentX(CENTER_ALIGNMENT);
        this.abilitiesLabel.setAlignmentX(CENTER_ALIGNMENT);
        header.add(this.turnLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(this.abilitiesLabel);
        gamePanel.add(header, BorderLayout.NORTH);

        final JPanel ownPanel = this.wrapBoard(this.ownTitle, this.ownGrid);
        final JPanel opponentPanel = this.wrapBoard(this.opponentTitle, this.opponentGrid);
        final JSplitPane boards = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ownPanel, opponentPanel);
        boards.setResizeWeight(0.5);
        boards.setEnabled(false);
        gamePanel.add(boards, BorderLayout.CENTER);

        this.logArea.setEditable(false);
        this.logArea.setLineWrap(true);
        this.logArea.setWrapStyleWord(true);

        final JPanel controls = new JPanel(new BorderLayout(8, 8));
        final JPanel actionControls = new JPanel();
        actionControls.add(new JLabel("Action:"));
        actionControls.add(this.actionMode);
        actionControls.add(new JLabel("Direction:"));
        actionControls.add(this.direction);
        final JButton restart = new JButton("New Game");
        restart.addActionListener(event -> this.controller.returnToSetup());
        actionControls.add(restart);
        controls.add(actionControls, BorderLayout.NORTH);
        controls.add(new JScrollPane(this.logArea), BorderLayout.CENTER);
        controls.add(new JLabel(
            "<html>Owner's fleet: A Flagship · B Battleship · C Cruiser · S Submarine · "
                + "D Destroyer · R Recon · X Armored<br>✕ hit · • miss · sunk · "
                + "Shield: first hit absorbed</html>",
            JLabel.CENTER
        ), BorderLayout.SOUTH);
        gamePanel.add(controls, BorderLayout.SOUTH);

        this.actionMode.addActionListener(event -> {
            this.direction.setEnabled(this.actionMode.getSelectedItem() == ActionMode.SEQUENTIAL);
            if (this.controller != null) {
                this.controller.actionModeChanged();
            }
        });
        this.direction.setEnabled(false);
        return gamePanel;
    }

    private JPanel buildPrivacyPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        this.privacyLabel.setFont(this.privacyLabel.getFont().deriveFont(Font.BOLD, PRIVACY_FONT_SIZE));
        this.privacyLabel.setForeground(NAVY);
        panel.add(this.privacyLabel);
        return panel;
    }

    private JPanel wrapBoard(final JLabel title, final JPanel grid) {
        final JPanel wrapper = new JPanel(new BorderLayout(4, 4));
        title.setFont(title.getFont().deriveFont(Font.BOLD, BOARD_TITLE_FONT_SIZE));
        wrapper.add(title, BorderLayout.NORTH);
        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private void startFromForm() {
        final String first = this.firstHarbor.getText().trim();
        final String second = this.secondHarbor.getText().trim();
        if (first.isEmpty() || second.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the names of both harbors.");
            return;
        }
        this.controller.startPlacement(first, second, this.includeArmoredShip.isSelected());
    }

    private void renderPlacementBoard(final BoardSnapshot snapshot) {
        this.placementGrid.removeAll();
        this.placementGrid.setLayout(new GridLayout(BOARD_SIZE + 1, BOARD_SIZE + 1, 1, 1));
        this.placementGrid.add(new JLabel("", JLabel.CENTER));
        for (int column = 0; column < BOARD_SIZE; column++) {
            this.placementGrid.add(new JLabel(String.valueOf((char) ('A' + column)), JLabel.CENTER));
        }
        for (int row = 0; row < BOARD_SIZE; row++) {
            this.placementGrid.add(new JLabel(Integer.toString(row + 1), JLabel.CENTER));
            for (int column = 0; column < BOARD_SIZE; column++) {
                final Coordinate coordinate = new Coordinate(row, column);
                final JButton cell = BoardCells.create(
                    snapshot.stateAt(coordinate),
                    snapshot.shipTypeAt(coordinate).orElse(null)
                );
                cell.addActionListener(event -> this.placeSelectedShip(coordinate));
                this.placementGrid.add(cell);
            }
        }
        this.placementGrid.revalidate();
        this.placementGrid.repaint();
    }

    /**
     * Updates the ship selector using the active fleet rules.
     *
     * @param rules active fleet rules
     */
    private void configurePlacementShips(final FleetRules rules) {
        this.placementShip.removeAllItems();

        for (final ShipType type : ShipType.values()) {
            final int quantity = rules.quantity(type);

            if (quantity > 0) {
                this.placementShip.addItem(
                    new PlacementOption(type, quantity)
                );
            }
        }
    }

    private void placeSelectedShip(final Coordinate origin) {
        final PlacementOption selected = (PlacementOption) this.placementShip.getSelectedItem();
        final Rotation selectedRotation = (Rotation) this.placementRotation.getSelectedItem();
        if (selected == null || selectedRotation == null) {
            return;
        }
        this.controller.placeShip(origin, selected.type(), selectedRotation);
    }

    private void renderBoard(
        final JPanel panel,
        final BoardSnapshot snapshot,
        final boolean interactive,
        final List<Coordinate> pendingTargets,
        final GamePhase phase
    ) {
        panel.removeAll();
        panel.setLayout(new GridLayout(BOARD_SIZE + 1, BOARD_SIZE + 1, 1, 1));
        panel.add(new JLabel("", JLabel.CENTER));
        for (int column = 0; column < BOARD_SIZE; column++) {
            panel.add(new JLabel(String.valueOf((char) ('A' + column)), JLabel.CENTER));
        }
        for (int row = 0; row < BOARD_SIZE; row++) {
            panel.add(new JLabel(Integer.toString(row + 1), JLabel.CENTER));
            for (int column = 0; column < BOARD_SIZE; column++) {
                final Coordinate coordinate = new Coordinate(row, column);
                final JButton cell = BoardCells.create(
                    snapshot.stateAt(coordinate),
                    snapshot.shipTypeAt(coordinate).orElse(null)
                );
                if (interactive && phase == GamePhase.IN_PROGRESS) {
                    cell.addActionListener(event -> this.handleTarget(coordinate));
                } else {
                    cell.setEnabled(false);
                }
                if (pendingTargets.contains(coordinate)) {
                    cell.setBorder(BorderFactory.createLineBorder(ARMOR, 3));
                }
                panel.add(cell);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private void handleTarget(final Coordinate coordinate) {
        final ActionMode mode = (ActionMode) this.actionMode.getSelectedItem();
        if (mode == null) {
            return;
        }
        final ShotDirection selectedDirection = (ShotDirection) this.direction.getSelectedItem();
        this.controller.handleTarget(coordinate, mode, selectedDirection);
    }

    private void applySystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignored) {
            // The portable Metal look and feel remains available.
        }
    }

    /**
     * One ship type displayed in the placement selector.
     *
     * @param type ship type
     * @param quantity required quantity
     */
    private record PlacementOption(ShipType type, int quantity) {
        private PlacementOption {
            Objects.requireNonNull(type, "type");

            if (quantity <= 0) {
                throw new IllegalArgumentException(
                    "Quantity must be positive"
                );
            }
        }

        @Override
        public String toString() {
            return label(this.type) + " (" + this.quantity + ")";
        }

        private static String label(final ShipType type) {
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
    }

    /**
     * Renders rotations using user-friendly degree labels.
     */
    private static final class RotationRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(
            final JList<?> list,
            final Object value,
            final int index,
            final boolean isSelected,
            final boolean cellHasFocus
        ) {
            final JLabel label = (JLabel) super.getListCellRendererComponent(
                list,
                value,
                index,
                isSelected,
                cellHasFocus
            );

            if (value instanceof Rotation rotation) {
                label.setText(rotationLabel(rotation));
            }

            return label;
        }

        /**
         * Tells the angle of a determined rotation.
         * 
         * @param rotation the selected rotation
         * @return the angle of that rotation
         */
        private static String rotationLabel(final Rotation rotation) {
            return switch (rotation) {
                case DEGREES_0 -> "0°";
                case DEGREES_90 -> "90°";
                case DEGREES_180 -> "180°";
                case DEGREES_270 -> "270°";
            };
        }
    }
}
