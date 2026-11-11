import javax.swing.*;
import java.awt.*;

public class GameLobby extends JFrame {

    private String playerName;
    private String opponentName = "Waiting...";

    private boolean isHost;

    private GameServer server;
    private GameClient client;

    private JLabel statusLabel;
    private JTextField ipField;

    private JButton connectButton;
    private JButton startButton;

    private ChatPanel chatPanel;
    private GameBoard gameBoard;

    public GameLobby(String name, boolean host) {
        playerName = name;
        isHost = host;

        setTitle("Tic-Tac-Toe | Multiplayer Lobby");
        setSize(620, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JPanel header = new JPanel();
        header.setBackground(UiTheme.BACKGROUND);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("MULTIPLAYER LOBBY");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(UiTheme.TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        statusLabel = new JLabel("Welcome, " + playerName);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(UiTheme.CYAN);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        header.add(title);
        header.add(Box.createVerticalStrut(7));
        header.add(statusLabel);
        root.add(header, BorderLayout.NORTH);

        JPanel connectionPanel = new JPanel();
        connectionPanel.setBackground(UiTheme.SURFACE);
        connectionPanel.setBorder(UiTheme.cardBorder(new Color(54, 72, 103)));
        connectionPanel.setLayout(new BoxLayout(connectionPanel, BoxLayout.Y_AXIS));

        JLabel connectionTitle = new JLabel("CONNECT TO AN OPPONENT");
        connectionTitle.setForeground(UiTheme.TEXT);
        connectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        connectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel addressLabel = new JLabel("HOST IP ADDRESS");
        addressLabel.setForeground(UiTheme.MUTED);
        addressLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        addressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        ipField = new JTextField("127.0.0.1", 18);
        UiTheme.styleTextField(ipField);
        ipField.setMaximumSize(new Dimension(340, 42));
        ipField.setHorizontalAlignment(JTextField.CENTER);

        startButton = UiTheme.button("HOST A GAME", UiTheme.CYAN);
        connectButton = UiTheme.button("JOIN GAME", new Color(100, 117, 244));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(340, 44));
        connectButton.setMaximumSize(new Dimension(340, 44));

        connectionPanel.add(Box.createVerticalStrut(18));
        connectionPanel.add(connectionTitle);
        connectionPanel.add(Box.createVerticalStrut(24));
        connectionPanel.add(addressLabel);
        connectionPanel.add(Box.createVerticalStrut(8));
        connectionPanel.add(ipField);
        connectionPanel.add(Box.createVerticalStrut(20));
        connectionPanel.add(startButton);
        connectionPanel.add(Box.createVerticalStrut(11));
        connectionPanel.add(connectButton);
        connectionPanel.add(Box.createVerticalStrut(18));

        JPanel connectionWrap = new JPanel(new GridBagLayout());
        connectionWrap.setBackground(UiTheme.BACKGROUND);
        connectionWrap.add(connectionPanel);
        root.add(connectionWrap, BorderLayout.CENTER);

        chatPanel = new ChatPanel();
        root.add(chatPanel, BorderLayout.SOUTH);
        add(root);

        chatPanel.setSendAction(message -> {
            if (isHost && server != null) {
                server.sendMessage(playerName + ": " + message);
            }
            if (!isHost && client != null) {
                client.sendMessage(playerName + ": " + message);
            }
        });

        startButton.addActionListener(e -> startServer());
        connectButton.addActionListener(e -> connectToServer());
        setVisible(true);
    }

    private void startServer() {
        server = new GameServer();
        statusLabel.setText("Starting server...");

        server.startServer(playerName, message -> SwingUtilities.invokeLater(() -> {
            if (message.equals("Opponent connected!")) {
                statusLabel.setText("Opponent connected!");
                chatPanel.addMessage("Opponent connected!");
            } else if (message.startsWith("Opponent: NAME,")) {
                opponentName = message.substring("Opponent: NAME,".length());
                chatPanel.addMessage(opponentName + " joined!");
                openGameBoard('X', playerName, opponentName);
            } else if (message.startsWith("Opponent: MOVE,")) {
                if (gameBoard != null) {
                    gameBoard.receiveMove(
                            message.substring("Opponent: ".length()));
                }
            } else if (message.equals("Opponent: RESET")) {
                if (gameBoard != null) {
                    gameBoard.receiveRestart();
                }
            } else if (message.startsWith("Opponent: ")) {
                chatPanel.addMessage(message.substring("Opponent: ".length()));
            } else {
                statusLabel.setText(message);
            }
        }));
    }

    private void connectToServer() {
        String ip = ipField.getText().trim();
        client = new GameClient();
        statusLabel.setText("Connecting...");

        client.connect(ip, playerName, message -> SwingUtilities.invokeLater(() -> {
            if (message.equals("Connected successfully!")) {
                statusLabel.setText("Connected to server!");
                chatPanel.addMessage("Connected successfully!");
            } else if (message.startsWith("Server: NAME,")) {
                opponentName = message.substring("Server: NAME,".length());
                chatPanel.addMessage(opponentName + " is hosting!");
                openGameBoard('O', opponentName, playerName);
            } else if (message.startsWith("Server: MOVE,")) {
                if (gameBoard != null) {
                    gameBoard.receiveMove(
                            message.substring("Server: ".length()));
                }
            } else if (message.equals("Server: RESET")) {
                if (gameBoard != null) {
                    gameBoard.receiveRestart();
                }
            } else if (message.startsWith("Server: ")) {
                chatPanel.addMessage(message.substring("Server: ".length()));
            } else {
                statusLabel.setText(message);
            }
        }));
    }

    private void openGameBoard(char symbol, String xName, String oName) {
        if (gameBoard != null) {
            return;
        }

        if (isHost) {
            gameBoard = new GameBoard(
                    symbol, message -> server.sendMessage(message), xName, oName);
        } else {
            gameBoard = new GameBoard(
                    symbol, message -> client.sendMessage(message), xName, oName);
        }

        gameBoard.setVisible(true);
    }
}
