package com.ChilliSauce;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {

    private static void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void playMoveSound() {
        playSound("src/main/resources/sounds/move.wav");
    }

    public static void playCaptureSound() {
        playSound("src/main/resources/sounds/capture.wav");
    }
    public static void playCastlingSound() {
        playSound("src/main/resources/sounds/castle.wav");
    }
    public static void playPromotionSound() {
        playSound("src/main/resources/sounds/promote.wav");
    }
}
