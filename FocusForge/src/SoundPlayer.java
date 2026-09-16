import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.awt.*;

public final class SoundPlayer {

    private SoundPlayer() {
    }

    public static void playAlert() {
        new Thread(() -> {
            try {
                byte[] buf = new byte[16000 * 2];
                for (int i = 0; i < buf.length; i++) {
                    double angle = i / (16000.0 / 800.0) * 2.0 * Math.PI;
                    buf[i] = (byte) (Math.sin(angle) * 127);
                }
                AudioFormat af = new AudioFormat(16000, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                sdl.write(buf, 0, buf.length);
                sdl.drain();
                sdl.close();
            } catch (Exception ex) {
                Toolkit.getDefaultToolkit().beep();
            }
        }).start();
    }
}
