
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame;
        frame = new JFrame("CHIP-8 Emulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 320);
        frame.setLocationRelativeTo(null);
        
        Chip8 chip8 = new Chip8();
        Display display = new Display(chip8.getDisplay());
        frame.add(display);
        frame.setVisible(true);

        String filePath = chooseFile(frame);
        if (filePath == null) {
            System.exit(0);
        }

        try {
            chip8.loadProgramFromFile(filePath);
            long lastCycleTime = System.nanoTime();
            long lastTimerUpdate = System.currentTimeMillis();

            while (true) {
                long now = System.nanoTime();
                if (now - lastCycleTime >= 1000000000 / 500) {
                    chip8.emulateCycle();
                    display.refresh();
                    lastCycleTime = now;

                    if (System.currentTimeMillis() - lastTimerUpdate >= 1000 / 60) {
                        chip8.updateTimers();
                        lastTimerUpdate = System.currentTimeMillis();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load CHIP-8 program: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String chooseFile(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a CHIP-8 Program");
        int userSelection = fileChooser.showOpenDialog(parent);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }
}
