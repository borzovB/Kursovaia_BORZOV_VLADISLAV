package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ToggleSwitch extends JPanel {
    private boolean isOn = false;
    private int circleX = 5;
    private ChatClientWithLogin client; // Reference to main client for theme toggling
    private int lightenFactor = 20; // Уровень осветления тёмно-серого

    public ToggleSwitch(ChatClientWithLogin client) {
        this.client = client;
        setPreferredSize(new Dimension(170, 50));
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                isOn = !isOn;
                client.toggleTheme(isOn); // Call theme toggle on main client
                animateSwitch();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(isOn ? Color.LIGHT_GRAY : Color.DARK_GRAY);
        g.fillRoundRect(0, 10, getWidth() - 1, 30, 30, 30);

        g.setColor(Color.WHITE);
        g.fillOval(circleX, 15, 20, 20);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String modeText = isOn ? "ТЁМНАЯ ТЕМА" : "СВЕТЛАЯ ТЕМА";
        g.drawString(modeText, getWidth() / 2 - 30, 30);
    }

    private void animateSwitch() {
        Timer timer = new Timer(10, e -> {
            if (isOn && circleX < getWidth() - 25) {
                circleX += 2;
                repaint();
            } else if (!isOn && circleX > 5) {
                circleX -= 2;
                repaint();
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    // Возвращает текущий цвет панели
    public Color getPanelColor() {
        System.out.println(isOn);
        if (isOn) {
            // Базовый темно-серый
            Color darkGray = Color.DARK_GRAY;

            // Осветляем темно-серый
            int r = Math.min(255, darkGray.getRed() + lightenFactor);
            int g = Math.min(255, darkGray.getGreen() + lightenFactor);
            int b = Math.min(255, darkGray.getBlue() + lightenFactor);

            return new Color(r, g, b);
        } else {
            return Color.LIGHT_GRAY; // Светло-серый для светлой темы
        }
    }

    public Color getTextColor() {
        System.out.println(isOn);
        return isOn ? Color.WHITE : Color.BLACK;
    }

}
