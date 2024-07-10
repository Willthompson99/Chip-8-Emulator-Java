
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

public class Chip8 {

    private byte[] memory = new byte[4096];
    private byte[] V = new byte[16];
    private int I;
    private int pc = 0x200;
    private int delayTimer;
    private int soundTimer;
    private boolean[][] display = new boolean[64][32];
    private boolean[] keys = new boolean[16];
    private int[] stack = new int[16];
    private int sp = 0;
    private Random random = new Random();

    public Chip8() {
        initialize();
    }

    private void initialize() {
        pc = 0x200;
        I = 0;
        sp = 0;
        delayTimer = 0;
        soundTimer = 0;
        Arrays.fill(memory, (byte) 0);
        Arrays.fill(V, (byte) 0);
        Arrays.fill(stack, 0);
        for (boolean[] row : display) {
            Arrays.fill(row, false);
        }
        Arrays.fill(keys, false);
    }

    public void loadProgramFromFile(String filePath) throws IOException {
        byte[] program = Files.readAllBytes(Paths.get(filePath));
        System.arraycopy(program, 0, memory, 0x200, program.length);
    }

    public void emulateCycle() {
        int opcode = ((memory[pc] << 8) | (memory[pc + 1] & 0xFF));
        int X, Y, NN;
        switch (opcode & 0xF000) {
            case 0x0000:
                switch (opcode & 0x00FF) {
                    case 0x00E0:
                        for (boolean[] row : display) {
                            Arrays.fill(row, false);
                        }
                        pc += 2;
                        break;
                    case 0x00EE:
                        sp--;
                        pc = stack[sp] + 2;
                        break;
                    default:
                        System.out.printf("Unknown opcode [0x0000]: 0x%X\n", opcode);
                        break;
                }
                break;
            case 0x1000:
                pc = opcode & 0x0FFF;
                break;
            case 0x2000:
                stack[sp] = pc;
                sp++;
                pc = opcode & 0x0FFF;
                break;
            case 0x3000:
                X = (opcode & 0x0F00) >> 8;
                NN = opcode & 0x00FF;
                pc += (V[X] == NN) ? 4 : 2;
                break;
            case 0x4000:
                X = (opcode & 0x0F00) >> 8;
                NN = opcode & 0x00FF;
                pc += (V[X] != NN) ? 4 : 2;
                break;
            case 0x5000:
                X = (opcode & 0x0F00) >> 8;
                Y = (opcode & 0x00F0) >> 4;
                pc += (V[X] == V[Y]) ? 4 : 2;
                break;
            case 0x6000:
                X = (opcode & 0x0F00) >> 8;
                NN = opcode & 0x00FF;
                V[X] = (byte) NN;
                pc += 2;
                break;
            case 0x7000:
                X = (opcode & 0x0F00) >> 8;
                NN = opcode & 0x00FF;
                V[X] = (byte) ((V[X] + NN) & 0xFF);
                pc += 2;
                break;
            case 0x8000:
                X = (opcode & 0x0F00) >> 8;
                Y = (opcode & 0x00F0) >> 4;
                switch (opcode & 0x000F) {
                    case 0x0000:
                        V[X] = V[Y];
                        break;
                    case 0x0001:
                        V[X] |= V[Y];
                        break;
                    case 0x0002:
                        V[X] &= V[Y];
                        break;
                    case 0x0003:
                        V[X] ^= V[Y];
                        break;
                    case 0x0004:
                        int sum = (V[X] & 0xFF) + (V[Y] & 0xFF);
                        V[0xF] = (byte) ((sum > 255) ? 1 : 0);
                        V[X] = (byte) sum;
                        break;
                    case 0x0005:
                        V[0xF] = (byte) ((V[X] > V[Y]) ? 1 : 0);
                        V[X] -= V[Y];
                        break;
                    case 0x0006:
                        V[0xF] = (byte) (V[X] & 0x1);
                        V[X] >>= 1;
                        break;
                    case 0x0007:
                        V[0xF] = (byte) ((V[Y] > V[X]) ? 1 : 0);
                        V[X] = (byte) (V[Y] - V[X]);
                        break;
                    case 0x000E:
                        V[0xF] = (byte) ((V[X] & 0x80) >> 8);
                        V[X] <<= 1;
                        break;
                }
                pc += 2;
                break;
            case 0x9000:
                X = (opcode & 0x0F00) >> 8;
                Y = (opcode & 0x00F0) >> 4;
                pc += (V[X] != V[Y]) ? 4 : 2;
                break;
            case 0xA000:
                I = opcode & 0x0FFF;
                pc += 2;
                break;
            case 0xB000:
                pc = (char) (V[0] + (opcode & 0x0FFF));
                break;
            case 0xC000:
                X = (opcode & 0x0F00) >> 8;
                NN = opcode & 0x00FF;
                V[X] = (byte) (random.nextInt(256) & NN);
                pc += 2;
                break;
            case 0xD000:
                X = V[(opcode & 0x0F00) >> 8];
                Y = V[(opcode & 0x00F0) >> 4];
                int height = opcode & 0x000F;
                V[0xF] = 0;

                for (int yLine = 0; yLine < height; yLine++) {
                    int pixel = memory[I + yLine];
                    for (int xLine = 0; xLine < 8; xLine++) {
                        if ((pixel & (0x80 >> xLine)) != 0) {
                            if (display[(X + xLine) % 64][(Y + yLine) % 32]) {
                                V[0xF] = 1;
                            }
                            display[(X + xLine) % 64][(Y + yLine) % 32] ^= true;
                        }
                    }
                }
                pc += 2;
                break;
            case 0xE000:
                X = V[(opcode & 0x0F00) >> 8];
                switch (opcode & 0x00FF) {
                    case 0x009E:
                        if (keys[X]) {
                            pc += 4;
                        } else {
                            pc += 2;
                        }
                        break;
                    case 0x00A1:
                        if (!keys[X]) {
                            pc += 4;
                        } else {
                            pc += 2;
                        }
                        break;
                    default:
                        System.out.printf("Unknown E-Series opcode: 0x%X\n", opcode);
                        break;
                }
                break;
            case 0xF000:
                X = (opcode & 0x0F00) >> 8;
                switch (opcode & 0x00FF) {
                    case 0x0007:
                        V[X] = (byte) delayTimer;
                        pc += 2;
                        break;
                    case 0x000A:
                        boolean keyPress = false;
                        for (int i = 0; i < keys.length; i++) {
                            if (keys[i]) {
                                V[X] = (byte) i;
                                keyPress = true;
                                break;
                            }
                        }
                        if (!keyPress) {
                            return;
                        }
                        pc += 2;
                        break;
                    case 0x0015:
                        delayTimer = V[X];
                        pc += 2;
                        break;
                    case 0x0018:
                        soundTimer = V[X];
                        pc += 2;
                        break;
                    case 0x001E:
                        I += V[X];
                        pc += 2;
                        break;
                    case 0x0029:
                        I = V[X] * 5;
                        pc += 2;
                        break;
                    case 0x0033:
                        memory[I] = (byte) (V[X] / 100);
                        memory[I + 1] = (byte) ((V[X] / 10) % 10);
                        memory[I + 2] = (byte) (V[X] % 10);
                        pc += 2;
                        break;
                    case 0x0055:
                        for (int i = 0; i <= X; i++) {
                            memory[I + i] = V[i];
                        }
                        pc += 2;
                        break;
                    case 0x0065:
                        for (int i = 0; i <= X; i++) {
                            V[i] = memory[I + i];
                        }
                        pc += 2;
                        break;
                    default:
                        System.out.printf("Unknown F-Series opcode: 0x%X\n", opcode);
                        break;
                }
                break;
            
            default:
                System.out.printf("Unknown opcode: 0x%X\n", opcode);
                break;
        }
        updateTimers();
    }

    public boolean[][] getDisplay() {
        return display;
    }

    public void updateTimers() {
        if (delayTimer > 0) {
            delayTimer--;
        }
        if (soundTimer > 0) {
            soundTimer--;
        }
    }
}
