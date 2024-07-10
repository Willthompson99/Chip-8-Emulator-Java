import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.sound.sampled.*;

public class Sound {
    private Clip clip;

    public Sound() {
        try {
            byte[] buf = new byte[1];
            AudioFormat af = new AudioFormat(8000,8,1,true,false);
            try (SourceDataLine sdl = AudioSystem.getSourceDataLine(af)) {
                sdl.open(af);
                sdl.start();
                for(int i=0; i < 8000 * 1; i++) {
                    double angle = i / (8000.0 / 800) * 2.0 * Math.PI;
                    buf[0] = (byte)(Math.sin(angle) * 100);
                    sdl.write(buf,0,1);
                }
                sdl.drain();
                sdl.stop();
                sdl.close();

                ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                AudioInputStream ais = new AudioInputStream(bais, af, buf.length);
                clip = AudioSystem.getClip();
                clip.open(ais);
            } catch (LineUnavailableException e) {
                System.err.println("Line unavailable for sound generation: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("I/O error during sound system initialization: " + e.getMessage());
        }
    }

    public void play() {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}
