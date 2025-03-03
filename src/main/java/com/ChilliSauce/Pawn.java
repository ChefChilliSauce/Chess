package com.ChilliSauce;

public class Pawn extends Piece {
    public Pawn(String color) {
        super(color);
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♙" : "♟";
    }
}
