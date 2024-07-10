import java.io.*;


public class Main {
    public static void main(String[] args) {
        Chip8 chip8 = new Chip8();
        
        String filePath = "";

        try {
            chip8.loadProgramFromFile(filePath);
        } catch (IOException e) {
            System.err.println("Failed to load CHIP-8 program: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
}
