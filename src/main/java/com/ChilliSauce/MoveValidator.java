package com.ChilliSauce;

public abstract class MoveValidator {
    public abstract boolean isValidMove(Board board, char fromFile, int fromRank, char toFile, int toRank);
}

