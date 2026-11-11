import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.function.Consumer;

public class ChatPanel extends JPanel {

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    private Consumer<String> sendAction;

    public ChatPanel() {
        setLayout(new BorderLayout(5, 5));
        setBackground(UiTheme.SURFACE);

        TitledBorder titleBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(56, 70, 96)),
                "  LIVE CHAT  ");
        titleBorder.setTitleColor(UiTheme.CYAN);
        titleBorder.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        setBorder(BorderFactory.createCompoundBorder(
                titleBorder, BorderFactory.createEmptyBorder(8, 10, 10, 10)));
        setPreferredSize(new Dimension(400, 205));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chatArea.setBackground(UiTheme.BACKGROUND);
        chatArea.setForeground(UiTheme.TEXT);
        chatArea.setCaretColor(UiTheme.CYAN);
        chatArea.setBorder(BorderFactory.createEmptyBorder(8, 9, 8, 9));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                new Color(45, 57, 80)));
        scrollPane.getViewport().setBackground(UiTheme.BACKGROUND);

        messageField = new JTextField();
        UiTheme.styleTextField(messageField);
        sendButton = UiTheme.button("SEND", UiTheme.CYAN);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(UiTheme.SURFACE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && sendAction != null) {
            sendAction.accept(message);
            messageField.setText("");
        }
    }

    public void setSendAction(Consumer<String> action) {
        this.sendAction = action;
    }

    public void addMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
}
