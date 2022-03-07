package com.codebind;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class Utils {
    // Transform the remaining time to a human-readable format.
    public static String convertRemainingMillisToMinutes(long millis, long periodOfTime) {
        long selectedLength = periodOfTime * 60 * 1000;
        long remainingTime = selectedLength - millis;
        long millisToSeconds = (remainingTime / 1000) % 60;
        long millisToMinutes = remainingTime / (1000 * 60);

        if (remainingTime <= 0) {
            return "00:00";
        }

        return String.format("%02d", millisToMinutes) + ":" + String.format("%02d", millisToSeconds);
    }

    public static void playAlarmSound() {
        playSound("alarm.wav");
    }

    public static void playClickSound() {
        playSound("click.wav");
    }

    public static void playSound(String fileName) {
        URL url = Utils.class.getClassLoader().getResource(fileName);
        try {
            assert url != null;
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();

            clip.open(audioIn);
            clip.start();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
