import java.io.*;
import javax.swing.JFileChooser;
import javax.swing.JFrame;


public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Chip8 chip8 = new Chip8();
        
        String filePath = chooseFile(frame);

        try {
            chip8.loadProgramFromFile(filePath);
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
            File fileToOpen = fileChooser.getSelectedFile();
            return fileToOpen.getAbsolutePath();
        }
        return null;
    }
}
