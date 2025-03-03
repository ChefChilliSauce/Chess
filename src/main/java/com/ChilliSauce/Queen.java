package com.ChilliSauce;

public class Queen extends Piece {
    public Queen(String color) {
        super(color);
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♕" : "♛";
    }
}
