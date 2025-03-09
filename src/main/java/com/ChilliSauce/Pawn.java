package com.ChilliSauce;

public class Pawn extends Piece {
    public Pawn(String color) {
        super(color,color.equals("white") ? "wp.png" : "bp.png");
    }
}
