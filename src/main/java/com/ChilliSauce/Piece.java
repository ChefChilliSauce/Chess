package com.ChilliSauce;

public abstract class Piece {
    protected String color;  // "white" or "black"

    public Piece(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    // 🔹 Add this abstract method to be implemented by subclasses
    public abstract String getSymbol();
}
