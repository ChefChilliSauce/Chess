package com.ChilliSauce;

public class Knight extends Piece {
    public Knight(String color) {
        super(color,color.equals("white") ? "wn.png" : "bn.png");
    }
}
