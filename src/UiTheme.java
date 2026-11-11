import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class UiTheme {

    public static final Color BACKGROUND = new Color(13, 18, 32);
    public static final Color SURFACE = new Color(25, 33, 52);
    public static final Color SURFACE_LIGHT = new Color(37, 48, 71);
    public static final Color TEXT = new Color(238, 243, 255);
    public static final Color MUTED = new Color(150, 164, 190);
    public static final Color CYAN = new Color(42, 211, 235);
    public static final Color PINK = new Color(255, 105, 145);
    public static final Color GREEN = new Color(84, 220, 160);
    public static final Color GOLD = new Color(255, 202, 92);

    private UiTheme() {
    }

    public static JButton button(String text, Color color) {
        return new ThemeButton(text, color);
    }

    public static void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(TEXT);
        field.setCaretColor(CYAN);
        field.setBackground(SURFACE_LIGHT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(62, 78, 106)),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        field.setSelectionColor(new Color(42, 211, 235, 110));
        field.setSelectedTextColor(TEXT);
    }

    public static Border cardBorder(Color accent) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14));
    }

    private static final class ThemeButton extends JButton {

        private final Color baseColor;
        private final Color hoverColor;
        private final Color pressedColor;
        private Color displayedColor;
        private Color targetColor;
        private Timer animation;

        private ThemeButton(String text, Color color) {
            super(text);
            baseColor = color;
            hoverColor = color.brighter();
            pressedColor = color.darker();
            displayedColor = color;
            targetColor = color;

            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(TEXT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(10, 18, 10, 18));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    animateTo(hoverColor);
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    animateTo(baseColor);
                }

                @Override
                public void mousePressed(MouseEvent event) {
                    if (isEnabled()) {
                        animateTo(pressedColor);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    if (isEnabled()) {
                        animateTo(contains(event.getPoint())
                                ? hoverColor : baseColor);
                    }
                }
            });
        }

        private void animateTo(Color color) {
            targetColor = color;
            if (animation != null && animation.isRunning()) {
                return;
            }

            animation = new Timer(15, event -> {
                displayedColor = approach(displayedColor, targetColor, 0.28);
                repaint();

                if (closeEnough(displayedColor, targetColor)) {
                    displayedColor = targetColor;
                    animation.stop();
                    repaint();
                }
            });
            animation.start();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled()
                    ? displayedColor : new Color(65, 72, 88));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(graphics);
        }

        private static Color approach(Color from, Color to, double amount) {
            return new Color(
                    (int) (from.getRed() + (to.getRed() - from.getRed()) * amount),
                    (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * amount),
                    (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * amount));
        }

        private static boolean closeEnough(Color first, Color second) {
            return Math.abs(first.getRed() - second.getRed()) < 2
                    && Math.abs(first.getGreen() - second.getGreen()) < 2
                    && Math.abs(first.getBlue() - second.getBlue()) < 2;
        }
    }
}
