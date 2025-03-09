package com.ChilliSauce;

public class King extends Piece {
    private boolean hasMoved;
    public King(String color) {
        super(color, color.equals("white") ? "wk.png" : "bk.png");
    }
    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
}
