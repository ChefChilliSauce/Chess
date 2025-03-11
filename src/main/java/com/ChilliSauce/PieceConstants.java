package com.ChilliSauce;

import javax.swing.*;
import java.awt.*;

public class PieceConstants {
    public static final int NONE = 0;
    public static final int KING = 1;
    public static final int QUEEN = 2;
    public static final int ROOK = 3;
    public static final int KNIGHT = 4;
    public static final int BISHOP = 5;
    public static final int PAWN = 6;

    public static final int WHITE = 8;
    public static final int BLACK = 16;

    public static Image getPieceImage(int piece) {
        if (piece == NONE) return null; // No piece, return nothing

        String pieceKey = "";

        if ((piece & WHITE) != 0) pieceKey += "w";
        else if ((piece & BLACK) != 0) pieceKey += "b";

        switch (piece & 7) { // Mask the piece type
            case KING -> pieceKey += "k";
            case QUEEN -> pieceKey += "q";
            case ROOK -> pieceKey += "r";
            case BISHOP -> pieceKey += "b";
            case KNIGHT -> pieceKey += "n";
            case PAWN -> pieceKey += "p";
            default -> { return null; } // Prevents invalid types
        }

        return loadImage(pieceKey);
    }

    private static Image loadImage(String pieceKey) {
        String path = "assets/" + pieceKey + ".png";
        java.net.URL imgURL = PieceConstants.class.getClassLoader().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL).getImage();
        } else {
            System.err.println("Error loading image: " + path);
            return null;
        }
    }
}
