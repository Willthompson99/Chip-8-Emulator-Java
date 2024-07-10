import java.awt.*;
import javax.swing.*;

public class Display extends JPanel {
    private boolean[][] display;

    public Display(boolean[][] display) {
        this.display = display;
        setPreferredSize(new Dimension(640, 320));
    }

    public void refresh() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized (display) {
            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 64; x++) {
                    g.setColor(display[x][y] ? Color.WHITE : Color.BLACK);
                    g.fillRect(x * 10, y * 10, 10, 10);
                }
            }
        }
    }
}
