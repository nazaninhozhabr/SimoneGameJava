package org.example;

import javax.sound.sampled.*;

public class SoundManager {
    public static void playTone(int hz) {
        try {
            byte[] buf = new byte[1];
            AudioFormat af = new AudioFormat(8000f, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(af);
            line.open(af);
            line.start();

            for (int i = 0; i < 700; i++) {
                double angle = i / (8000f / hz) * 2 * Math.PI;
                buf[0] = (byte) (Math.sin(angle) * 100);
                line.write(buf, 0, 1);
            }

            line.drain();
            line.stop();
            line.close();
        } catch (Exception ignored) {}
    }

    public static void playFail() {
        playTone(180);
    }
}