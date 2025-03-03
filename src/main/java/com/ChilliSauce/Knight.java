package com.ChilliSauce;

public class Knight extends Piece {
    public Knight(String color) {
        super(color);
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♘" : "♞";
    }
}
