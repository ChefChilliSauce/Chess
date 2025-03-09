package com.ChilliSauce;

public class Bishop extends Piece {
    public Bishop(String color) {
        super(color, color.equals("white") ? "wb.png" : "bb.png");
    }
}
