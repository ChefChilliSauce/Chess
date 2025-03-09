package com.ChilliSauce;

import javax.swing.*;
import java.awt.*;

public abstract class Piece {
    protected String color;
    protected String imagePath;
    protected Image image;

    public Piece(String color, String imagePath) {
        this.color = color;
        this.imagePath = "assets/" + imagePath; // Adjust path to match your structure

        try {
            java.net.URL imgURL = getClass().getClassLoader().getResource(this.imagePath);
            if (imgURL != null) {
                this.image = new ImageIcon(imgURL).getImage();
            } else {
                throw new Exception("Image not found: " + this.imagePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + this.imagePath);
            this.image = null;
        }
    }

    public String getColor() {
        return color;
    }

    public Image getImage() {
        return image;
    }
}
