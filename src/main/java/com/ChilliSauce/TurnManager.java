package com.ChilliSauce;

public class TurnManager {
    private String currentPlayer;  // "white" or "black"

    public TurnManager() {
        this.currentPlayer = "white";  // White starts first
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isPlayerTurn(String color) {
        return currentPlayer.equals(color);
    }

    public void switchTurn() {
        currentPlayer = currentPlayer.equals("white") ? "black" : "white";
    }

    // Fix: Ensure this method correctly returns the current player color
    public String getCurrentPlayerColor() {
        return currentPlayer;
    }
}
