import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    private JTextField nameField;

    public MainMenu() {
        setTitle("Tic-Tac-Toe | Online Arena");
        setSize(560, 590);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(UiTheme.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(34, 60, 34, 60));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("TIC TAC TOE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(UiTheme.TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("ONLINE MULTIPLAYER");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subtitle.setForeground(UiTheme.CYAN);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("PLAYER USERNAME");
        nameLabel.setForeground(UiTheme.MUTED);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(320, 44));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        UiTheme.styleTextField(nameField);

        JButton createButton = createButton("CREATE GAME", UiTheme.CYAN);
        JButton joinButton = createButton("JOIN GAME", new Color(100, 117, 244));
        JButton exitButton = createButton("EXIT", UiTheme.SURFACE_LIGHT);

        createButton.addActionListener(e -> {
            if (getPlayerName()) {
                String name = nameField.getText().trim();
                dispose();
                new GameLobby(name, true);
            }
        });

        joinButton.addActionListener(e -> {
            if (getPlayerName()) {
                String name = nameField.getText().trim();
                dispose();
                new GameLobby(name, false);
            }
        });

        exitButton.addActionListener(e -> System.exit(0));

        mainPanel.add(Box.createVerticalStrut(52));
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createVerticalStrut(54));
        mainPanel.add(nameLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(nameField);
        mainPanel.add(Box.createVerticalStrut(34));
        mainPanel.add(createButton);
        mainPanel.add(Box.createVerticalStrut(14));
        mainPanel.add(joinButton);
        mainPanel.add(Box.createVerticalStrut(14));
        mainPanel.add(exitButton);

        add(mainPanel);
        setVisible(true);
    }

    private JButton createButton(String text, Color color) {
        JButton button = UiTheme.button(text, color);
        button.setMaximumSize(new Dimension(320, 46));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private boolean getPlayerName() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your player name.",
                    "Username required", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
}
