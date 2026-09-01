package it.unibo.battleship.view;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.battleship.model.BoardSnapshot;
import it.unibo.battleship.model.Coordinate;
import it.unibo.battleship.model.GamePhase;
import it.unibo.battleship.model.GameSnapshot;
import it.unibo.battleship.model.PlayerId;
import it.unibo.battleship.model.RandomEventResult;
import it.unibo.battleship.model.Rotation;
import it.unibo.battleship.model.RuleViolation;
import it.unibo.battleship.model.ShipType;
import it.unibo.battleship.model.ShotKind;
import it.unibo.battleship.model.SonarResult;
import it.unibo.battleship.model.TurnResult;

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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Swing implementation of the Battleship graphical view.
 *
 */
@SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", 
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
    private static final int HANDOFF_PADDING = 12;
    private static final int HANDOFF_BUTTON_TOP_MARGIN = 24;
    private static final int START_BUTTON_TOP_MARGIN = 22;
    private static final float MAIN_TITLE_FONT_SIZE = 32F;
    private static final int ARMORED_HINT_ROW = 5;
    private static final int START_BUTTON_ROW = 6;
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
    private final JLabel privacyInstruction = new JLabel(" ", JLabel.CENTER);
    private final JButton confirmHandoff = new JButton("Continue");
    private final JTextArea logArea = new JTextArea(8, 70);
    private final JComboBox<ActionMode> actionMode = new JComboBox<>(ActionMode.values());
    private final JComboBox<ShotDirection> direction = new JComboBox<>(ShotDirection.values());
    private final JComboBox<PlacementOption> placementShip = new JComboBox<>();
    private final JComboBox<Rotation> placementRotation = new JComboBox<>(Rotation.values());
    private final JButton resetFleet = new JButton("Reset fleet");
    private final JButton confirmFleet = new JButton("Confirm fleet");
    private final JLabel placementTitle = new JLabel(" ", JLabel.CENTER);
    private final JLabel placementStatus = new JLabel(" ", JLabel.CENTER);
    private final JLabel placementProgress = new JLabel(" ", JLabel.CENTER);
    private final JLabel actionHint = new JLabel(" ", JLabel.CENTER);

    private transient BattleshipViewObserver observer;
    private Timer randomEventTimer;

    /**
     * Creates all screens without starting a game.
     */
    public BattleshipFrame() {
        super(APPLICATION_TITLE);
        this.placementRotation.setRenderer(new RotationRenderer());
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
    public void setObserver(
            final BattleshipViewObserver observer) {
        this.observer = Objects.requireNonNull(observer, "observer");
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
    public void showPlacement(final PlacementViewState state) {
        this.placementTitle.setText("Fleet placement - " + state.harborName());

        this.placementStatus.setText(SwingText.placementFeedback(state.feedback()));

        this.confirmFleet.setEnabled(state.complete());

        this.configurePlacementShips(state);
        this.renderPlacementBoard(state.board());
        this.cards.show(this.root, PLACEMENT_CARD);
    }

    @Override
    public void showPrivacy(final HandoffViewState state) {
        Objects.requireNonNull(state, "state");

        this.privacyLabel.setText(
            "Pass the device to " + state.recipientHarbor()
        );

        this.privacyInstruction.setText(
            switch (state.reason()) {
                case PLACEMENT ->
                    "The next player may now place their fleet.";
                case GAME_START ->
                    "The first player may now reveal the battle screen.";
                case TURN_CHANGE ->
                    "The next player may now reveal their boards.";
            }
        );

        this.confirmHandoff.setText(
            "I am " + state.recipientHarbor() + " — Continue"
        );

        this.confirmHandoff.getAccessibleContext()
            .setAccessibleDescription(
                "Reveal the next private game screen"
            );

        this.cards.show(this.root, PRIVACY_CARD);
    }

    @Override
    public void showGame(final GameViewState state) {
        final GameSnapshot snapshot = state.snapshot();
        final PlayerId viewer = snapshot.viewer();

        this.turnLabel.setText(
                "Turn: " + state.harborNames().get(
                        snapshot.currentPlayer()));

        this.abilitiesLabel.setText(
                SwingText.abilities(snapshot));

        this.ownTitle.setText(
                "Your board — " + state.harborNames().get(viewer));

        this.opponentTitle.setText(
                "Opponent — "
                        + state.harborNames().get(viewer.other()));

        this.renderBoard(
                this.ownGrid,
                snapshot.ownBoard(),
                false,
                List.of(),
                snapshot.phase());

        this.renderBoard(
                this.opponentGrid,
                snapshot.opponentBoard(),
                true,
                state.pendingTargets(),
                snapshot.phase());

        this.cards.show(this.root, GAME_CARD);
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
            if (this.observer != null) {
                this.observer.onRandomEventElapsed();
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
                SETUP_PADDING));
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
                JLabel.CENTER);
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

        this.includeArmoredShip.setToolTipText(
            "Adds one armored ship to both fleets. "
            + "Each section absorbs its first impact."
        );
        this.includeArmoredShip.getAccessibleContext()
            .setAccessibleDescription(
                "Include one armored ship in both equal fleets"
            );

        panel.add(this.includeArmoredShip, constraints);

        final JLabel armoredHint = new JLabel(
            "Optional: adds one ship to both fleets; "
            + "every section absorbs its first impact.",
            JLabel.CENTER
        );
        constraints.gridy = ARMORED_HINT_ROW;
        constraints.insets = new Insets(0, 8, 8, 8);
        panel.add(armoredHint, constraints);

        final JButton start = new JButton("Place the fleets");
        start.addActionListener(event -> this.startFromForm());

        constraints.gridx = 0;
        constraints.gridy = START_BUTTON_ROW;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(
                START_BUTTON_TOP_MARGIN,
                8,
                8,
                8);
        panel.add(start, constraints);
        return panel;
    }

    private JPanel buildPlacementPanel() {
        final JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(
                PLACEMENT_PADDING,
                PLACEMENT_PADDING,
                PLACEMENT_PADDING,
                PLACEMENT_PADDING));
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
        controls.add(this.placementProgress);
        controls.add(Box.createVerticalStrut(PLACEMENT_SECTION_GAP));
        controls.add(new JLabel("Rotation:"));
        controls.add(this.placementRotation);
        controls.add(Box.createVerticalStrut(PLACEMENT_CONFIRM_GAP));
        this.resetFleet.addActionListener(event -> {
            final int choice = JOptionPane.showConfirmDialog(
                this,
                "Remove every ship placed by this player?",
                "Reset fleet",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                this.requireObserver().onFleetResetRequested();
            }
        });
        controls.add(this.resetFleet);
        controls.add(Box.createVerticalStrut(PLACEMENT_SECTION_GAP));
        this.confirmFleet.addActionListener(event -> this.requireObserver().onFleetConfirmed());
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
        this.actionHint.setAlignmentX(CENTER_ALIGNMENT);
        header.add(Box.createVerticalStrut(4));
        header.add(this.actionHint);
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
        restart.addActionListener(event -> this.observer.onNewGameRequested());
        actionControls.add(restart);
        controls.add(actionControls, BorderLayout.NORTH);
        controls.add(new JScrollPane(this.logArea), BorderLayout.CENTER);
        controls.add(new JLabel(
                "<html>Owner's fleet: A Flagship · B Battleship · C Cruiser · S Submarine · "
                        + "D Destroyer · R Recon · X Armored<br>✕ hit · • miss · sunk · "
                        + "Shield: first hit absorbed</html>",
                JLabel.CENTER), BorderLayout.SOUTH);
        gamePanel.add(controls, BorderLayout.SOUTH);

        this.actionMode.addActionListener(event -> {
            this.direction.setEnabled(this.actionMode.getSelectedItem() == ActionMode.SEQUENTIAL);
            this.updateActionHint();

            if (this.observer != null) {
                this.observer.onActionSelectionChanged();
            }
        });
        this.direction.setEnabled(false);
        this.updateActionHint();
        return gamePanel;
    }

    /**
     * Updates the instruction associated with the selected action.
     */
    private void updateActionHint() {
        final ActionMode selected =
                (ActionMode) this.actionMode.getSelectedItem();

        if (selected == null) {
            this.actionHint.setText("Select an action.");
            return;
        }

        final String text = switch (selected) {
            case NORMAL ->
                "Select one opponent cell. A hit keeps the turn.";
            case DOUBLE ->
                "Select two different cells after three normal hits.";
            case SEQUENTIAL ->
                "Choose a direction, then select the first of three cells.";
            case SONAR ->
               "Select the center of a 3×3 scan; sonar ends the turn.";
        };

        this.actionHint.setText(text);
    }

    private JPanel buildPrivacyPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints constraints =
            new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(
            HANDOFF_PADDING,
            HANDOFF_PADDING,
            HANDOFF_PADDING,
            HANDOFF_PADDING
        );
        constraints.fill = GridBagConstraints.HORIZONTAL;

        this.privacyLabel.setFont(
            this.privacyLabel.getFont().deriveFont(
                Font.BOLD,
                PRIVACY_FONT_SIZE
            )
        );
        this.privacyLabel.setForeground(NAVY);
        panel.add(this.privacyLabel, constraints);

        constraints.gridy = 1;
        this.privacyInstruction.setForeground(NAVY);
        panel.add(this.privacyInstruction, constraints);

        constraints.gridy = 2;
        constraints.insets = new Insets(
            HANDOFF_BUTTON_TOP_MARGIN,
            HANDOFF_PADDING,
            HANDOFF_PADDING,
            HANDOFF_PADDING
        );

        this.confirmHandoff.addActionListener(event ->
            this.requireObserver().onHandoffConfirmed()
        );
        panel.add(this.confirmHandoff, constraints);

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
        this.observer.onSetupSubmitted(first, second, this.includeArmoredShip.isSelected());
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
                        coordinate,
                        snapshot.stateAt(coordinate),
                        snapshot.shipTypeAt(coordinate).orElse(null));
                cell.addActionListener(event -> this.placeSelectedShip(coordinate));
                this.placementGrid.add(cell);
            }
        }
        this.placementGrid.revalidate();
        this.placementGrid.repaint();
    }

    /**
     * Updates the placement controls.
     *
     * @param state current placement state
     */
    private void configurePlacementShips(final PlacementViewState state) {
        this.placementShip.removeAllItems();
        int placedTotal = 0;

        for (final ShipType type : ShipType.values()) {
            final int required = state.rules().quantity(type);
            final int placed = state.placedShips().getOrDefault(type, 0);
            final int remaining = required - placed;
            placedTotal += placed;

            if (remaining > 0) {
                this.placementShip.addItem(new PlacementOption(type, remaining));
            }
        }

        final int total = state.rules().totalShips();
        this.placementProgress.setText("Fleet progress: " + placedTotal + "/" + total);
        this.placementShip.setEnabled(!state.complete());
        this.placementRotation.setEnabled(!state.complete());
        this.resetFleet.setEnabled(placedTotal > 0);
    }

    private void placeSelectedShip(final Coordinate origin) {
        final PlacementOption selected = (PlacementOption) this.placementShip.getSelectedItem();
        final Rotation selectedRotation = (Rotation) this.placementRotation.getSelectedItem();
        if (selected == null || selectedRotation == null) {
            return;
        }
        this.observer.onShipPlacementRequested(origin, selected.type(), selectedRotation);
    }

    private void renderBoard(
            final JPanel panel,
            final BoardSnapshot snapshot,
            final boolean interactive,
            final List<Coordinate> pendingTargets,
            final GamePhase phase) {
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
                        coordinate,
                        snapshot.stateAt(coordinate),
                        snapshot.shipTypeAt(coordinate).orElse(null));
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
        switch (mode) {
            case NORMAL ->
                this.observer.onNormalShotRequested(coordinate);
            case DOUBLE ->
                this.observer.onDoubleShotTargetSelected(coordinate);
            case SEQUENTIAL ->
                this.observer.onSequentialShotRequested(
                        coordinate,
                        selectedDirection);
            case SONAR ->
                this.observer.onSonarRequested(coordinate);
        }
    }

    @Override
    public void appendGameStarted(
            final String firstHarborName) {
        this.appendLogLine(
                "Fleets deployed. First move: " + firstHarborName + ".");
    }

    @Override
    public void appendDoubleTargetSelected(
            final Coordinate target) {
        this.appendLogLine(
                "First Double Shot target selected: "
                        + SwingText.coordinate(target)
                        + ".");
    }

    @Override
    public void appendTurnResult(
            final String harborName,
            final ShotKind kind,
            final TurnResult result) {
        this.appendLogLine(
                harborName + ": " + SwingText.action(kind) + ".");

        result.shots().forEach(shot -> this.appendLogLine(
                "  "
                        + SwingText.coordinate(shot.target())
                        + " — "
                        + SwingText.outcome(shot.outcome())));
    }

    @Override
    public void appendSonarResult(
            final String harborName,
            final SonarResult result) {
        this.appendLogLine(
                harborName
                        + ": Sonar at "
                        + SwingText.coordinate(result.center())
                        + " — detected ship cells: "
                        + result.detectedCells()
                        + ".");
    }

    @Override
    public void showRuleViolation(
            final RuleViolation violation) {
        final String message = SwingText.violation(violation);

        this.appendLogLine(
                "Action rejected: " + message);

        JOptionPane.showMessageDialog(
                this,
                message,
                "Game rule",
                JOptionPane.WARNING_MESSAGE);
    }

    private void appendLogLine(final String text) {
        this.logArea.append(text + System.lineSeparator());
        this.logArea.setCaretPosition(
                this.logArea.getDocument().getLength());
    }

    @Override
    public void appendRandomEvent(
            final Optional<RandomEventResult> result,
            final Map<PlayerId, String> harborNames) {
        this.appendLogLine(
                SwingText.randomEvent(result, harborNames));
    }

    private BattleshipViewObserver requireObserver() {
        return Objects.requireNonNull(
            this.observer,
            "View observer has not been configured"
        );
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
                final boolean cellHasFocus) {
            final JLabel label = (JLabel) super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus);

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

    /**
     * One ship type displayed in the placement selector.
     *
     * @param type ship type
     * @param remaining remaining quantity
     */
    private record PlacementOption(ShipType type, int remaining) {
        private PlacementOption {
            Objects.requireNonNull(type, "type");
            if (remaining <= 0) {
                throw new IllegalArgumentException(
                        "Quantity must be positive");
            }
        }

        @Override
        public String toString() {
            return SwingText.shipType(this.type) + " - " + this.remaining + " remaining";
        }
    }
}
