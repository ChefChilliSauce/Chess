package com.ChilliSauce;

import javax.swing.*;
import java.awt.*;

public class Piece {
    private final int type;   // Encoded piece type (e.g., KING | WHITE)
    private final Image image;

    public Piece(int type) { // Only store type now
        this.type = type;
        this.image = PieceConstants.getPieceImage(type); // Load the correct image
    }

    public int getType() {
        return type;
    }

    public Image getImage() {
        return image;
    }

    private Image loadImage(String imagePath) {
        try {
            java.net.URL imgURL = getClass().getClassLoader().getResource("assets/" + imagePath);
            if (imgURL != null) {
                return new ImageIcon(imgURL).getImage();
            } else {
                throw new Exception("Image not found: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath);
            return null;
        }
    }

}
