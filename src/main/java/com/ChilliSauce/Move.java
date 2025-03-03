package com.ChilliSauce;

public class Move {
    private int startX, startY, endX, endY;
    private Piece piece;

    public Move(int startX, int startY, int endX, int endY, Piece piece) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.piece = piece;
    }

    public String toString() {
        return piece.getClass().getSimpleName() + " moved to (" + endX + ", " + endY + ")";
    }
}

