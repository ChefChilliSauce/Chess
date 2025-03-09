package com.ChilliSauce;

public class Rook extends Piece {
    private boolean hasMoved;
    public Rook(String color) {
        super(color, color.equals("white") ? "wr.png" : "br.png");
    }
    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
}
