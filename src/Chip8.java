import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Chip8 {
    private byte[] memory = new byte[4096];
    private byte[] V = new byte[16];
    private int I;
    private int pc = 0x200;
    private int delayTimer;
    private int soundTimer;
    private boolean[][] display = new boolean[64][32];
    private boolean[] keys = new boolean[16];

    public Chip8 () {
        initialize();
    }

    private void initialize () {
        pc = 0x200;
        I = 0;
        delayTimer = 0;
        soundTimer = 0;
    }

    public void loadProgramFromFile(String filePath) throws IOException {
        byte[] program = Files.readAllBytes(Paths.get(filePath));
        System.arraycopy(program, 0, memory, 0x200, program.length);
    }

    public void emulateCycle() {
        int opcode = ((memory[pc] << 8) | (memory[pc + 1] & 0xFF));
        switch (opcode & 0xF000) {
        case 0xA000:
            I = opcode & 0x0FFF;
            pc += 2;
            break;
        }
        if (delayTimer > 0) delayTimer--;
        if (soundTimer > 0) soundTimer--;
    }

}