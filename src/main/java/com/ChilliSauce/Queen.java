package com.ChilliSauce;

public class Queen extends Piece {
    public Queen(String color) {
        super(color, color.equals("white") ? "wq.png" : "bq.png");
    }
}
