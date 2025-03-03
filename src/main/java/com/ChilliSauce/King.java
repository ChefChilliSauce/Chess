package com.ChilliSauce;

public class King extends Piece {
    public King(String color) {
        super(color);
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♔" : "♚";
    }
}
