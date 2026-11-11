import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class GameBoard extends JFrame {

    private GameLogic game = new GameLogic();
    private JButton[][] buttons = new JButton[3][3];

    private JLabel statusLabel;
    private JLabel xScoreValue;
    private JLabel oScoreValue;
    private JLabel drawScoreValue;

    private JPanel xPlayerCard;
    private JPanel oPlayerCard;

    private int xScore = 0;
    private int oScore = 0;
    private int drawScore = 0;

    private char mySymbol;
    private boolean multiplayer;
    private boolean roundFinished;

    private String xPlayerName;
    private String oPlayerName;

    private Consumer<String> networkSend;

    public GameBoard() {
        this('X', null, "RABIN", "CHETTRI");
    }

    public GameBoard(char symbol, Consumer<String> sender,
                     String xName, String oName) {
        mySymbol = symbol;
        networkSend = sender;
        multiplayer = sender != null;
        xPlayerName = xName;
        oPlayerName = oName;

        setTitle("Tic-Tac-Toe | Game Arena");
        setSize(730, 780);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 16));
        mainPanel.setBackground(UiTheme.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel header = new JPanel();
        header.setBackground(UiTheme.BACKGROUND);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("TIC TAC TOE");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UiTheme.TEXT);

        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        statusLabel.setForeground(UiTheme.CYAN);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(statusLabel);
        header.add(Box.createVerticalStrut(17));

        JPanel scoreboard = new JPanel(new GridLayout(1, 3, 10, 0));
        scoreboard.setBackground(UiTheme.BACKGROUND);

        xPlayerCard = createPlayerCard("PLAYER X", xPlayerName, UiTheme.CYAN);
        JPanel drawCard = createScoreCard("DRAWS", UiTheme.GOLD);
        oPlayerCard = createPlayerCard("PLAYER O", oPlayerName, UiTheme.PINK);
        scoreboard.add(xPlayerCard);
        scoreboard.add(drawCard);
        scoreboard.add(oPlayerCard);
        header.add(scoreboard);

        JPanel grid = new JPanel(new GridLayout(3, 3, 10, 10));
        grid.setBackground(UiTheme.BACKGROUND);
        grid.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int row = i;
                final int col = j;
                buttons[i][j] = UiTheme.button("", UiTheme.SURFACE);
                buttons[i][j].setFont(new Font("Segoe UI", Font.BOLD, 58));
                buttons[i][j].setPreferredSize(new Dimension(165, 155));
                buttons[i][j].addActionListener(e -> handleMove(row, col));
                grid.add(buttons[i][j]);
            }
        }

        JButton restartButton = UiTheme.button("RESTART ROUND", UiTheme.SURFACE_LIGHT);
        restartButton.addActionListener(e -> {
            if (multiplayer) {
                networkSend.accept("RESET");
            }
            resetBoard();
        });

        JPanel bottom = new JPanel();
        bottom.setBackground(UiTheme.BACKGROUND);
        bottom.add(restartButton);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(grid, BorderLayout.CENTER);
        mainPanel.add(bottom, BorderLayout.SOUTH);
        add(mainPanel);

        updateScore();
        updateTurn();
        setVisible(true);
    }

    private JPanel createPlayerCard(String role, String name, Color accent) {
        JPanel card = new JPanel(new GridLayout(3, 1, 2, 2));
        card.setBackground(UiTheme.SURFACE);
        card.setBorder(UiTheme.cardBorder(new Color(54, 68, 94)));

        JLabel roleLabel = new JLabel(role, JLabel.CENTER);
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleLabel.setForeground(accent);

        JLabel nameLabel = new JLabel(name, JLabel.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(UiTheme.TEXT);

        JLabel scoreLabel = new JLabel("0 WINS", JLabel.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scoreLabel.setForeground(UiTheme.MUTED);

        card.add(roleLabel);
        card.add(nameLabel);
        card.add(scoreLabel);
        if (role.equals("PLAYER X")) {
            xScoreValue = scoreLabel;
        } else {
            oScoreValue = scoreLabel;
        }
        return card;
    }

    private JPanel createScoreCard(String title, Color accent) {
        JPanel card = new JPanel(new GridLayout(2, 1, 2, 2));
        card.setBackground(UiTheme.SURFACE);
        card.setBorder(UiTheme.cardBorder(new Color(54, 68, 94)));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLabel.setForeground(accent);

        drawScoreValue = new JLabel("0", JLabel.CENTER);
        drawScoreValue.setFont(new Font("Segoe UI", Font.BOLD, 21));
        drawScoreValue.setForeground(UiTheme.TEXT);

        card.add(titleLabel);
        card.add(drawScoreValue);
        return card;
    }

    private void handleMove(int row, int col) {
        if (roundFinished || game.getCurrentPlayer() != mySymbol) {
            return;
        }

        char player = game.getCurrentPlayer();
        if (!game.makeMove(row, col)) {
            return;
        }

        updateButton(row, col, player);
        if (multiplayer) {
            networkSend.accept("MOVE," + row + "," + col);
        }

        checkGameStatus();
        if (!roundFinished) {
            game.switchPlayer();
            updateTurn();
        }
    }

    public void receiveMove(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                String[] parts = message.split(",");
                int row = Integer.parseInt(parts[1]);
                int col = Integer.parseInt(parts[2]);
                char player = game.getCurrentPlayer();

                if (roundFinished || player == mySymbol) {
                    return;
                }
                if (!game.makeMove(row, col)) {
                    return;
                }

                updateButton(row, col, player);
                checkGameStatus();
                if (!roundFinished) {
                    game.switchPlayer();
                    updateTurn();
                }
            } catch (Exception e) {
                System.out.println("Invalid move received.");
            }
        });
    }

    public void receiveRestart() {
        SwingUtilities.invokeLater(this::resetBoard);
    }

    private void updateButton(int row, int col, char player) {
        buttons[row][col].setText(String.valueOf(player));
        buttons[row][col].setForeground(
                player == 'X' ? UiTheme.CYAN : UiTheme.PINK);
    }

    private void checkGameStatus() {
        char winner = game.checkWinner();
        if (winner != '\0') {
            String winnerName = winner == 'X' ? xPlayerName : oPlayerName;
            roundFinished = true;
            statusLabel.setText(winnerName + " TAKES THE ROUND!");
            if (winner == 'X') {
                xScore++;
            } else {
                oScore++;
            }
            updateScore();
            disableBoard();
            refreshPlayerCards();
            showResultDialog("ROUND WON", winnerName + " wins this round!");
        } else if (game.isDraw()) {
            roundFinished = true;
            drawScore++;
            statusLabel.setText("IT'S A DRAW!");
            updateScore();
            disableBoard();
            refreshPlayerCards();
            showResultDialog("ROUND DRAW", "A close match. The round is a draw!");
        }
    }

    private void showResultDialog(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setBackground(UiTheme.SURFACE);
        content.setBorder(BorderFactory.createEmptyBorder(24, 34, 24, 34));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        heading.setForeground(UiTheme.CYAN);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel result = new JLabel(message);
        result.setAlignmentX(Component.CENTER_ALIGNMENT);
        result.setForeground(UiTheme.TEXT);
        result.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton continueButton = UiTheme.button("CONTINUE", UiTheme.CYAN);
        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueButton.addActionListener(e -> dialog.dispose());

        content.add(heading);
        content.add(Box.createVerticalStrut(10));
        content.add(result);
        content.add(Box.createVerticalStrut(20));
        content.add(continueButton);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateTurn() {
        char current = game.getCurrentPlayer();
        String currentName = current == 'X' ? xPlayerName : oPlayerName;

        if (multiplayer) {
            statusLabel.setText(current == mySymbol
                    ? "YOUR TURN  |  " + currentName
                    : currentName + "'S TURN");
        } else {
            statusLabel.setText(currentName + "'S TURN");
        }
        refreshPlayerCards();
    }

    private void refreshPlayerCards() {
        Color inactive = new Color(54, 68, 94);
        xPlayerCard.setBorder(UiTheme.cardBorder(
                !roundFinished && game.getCurrentPlayer() == 'X'
                        ? UiTheme.CYAN : inactive));
        oPlayerCard.setBorder(UiTheme.cardBorder(
                !roundFinished && game.getCurrentPlayer() == 'O'
                        ? UiTheme.PINK : inactive));
        xPlayerCard.setBackground(
                !roundFinished && game.getCurrentPlayer() == 'X'
                        ? new Color(30, 48, 65) : UiTheme.SURFACE);
        oPlayerCard.setBackground(
                !roundFinished && game.getCurrentPlayer() == 'O'
                        ? new Color(55, 39, 57) : UiTheme.SURFACE);
    }

    private void resetBoard() {
        game.resetGame();
        roundFinished = false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        }
        updateTurn();
    }

    private void disableBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setEnabled(false);
            }
        }
    }

    private void updateScore() {
        xScoreValue.setText(xScore + (xScore == 1 ? " WIN" : " WINS"));
        oScoreValue.setText(oScore + (oScore == 1 ? " WIN" : " WINS"));
        drawScoreValue.setText(String.valueOf(drawScore));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameBoard());
    }
}
