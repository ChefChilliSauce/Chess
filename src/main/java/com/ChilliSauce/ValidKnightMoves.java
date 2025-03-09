package com.ChilliSauce;

public class ValidKnightMoves extends MoveValidator {
    @Override
    public boolean isValidMove(Board board, char fromFile, int fromRank, char toFile, int toRank) {
        Piece piece = board.getPiece(fromFile, fromRank);
        if (!(piece instanceof Knight)) return false;

        int fileDiff = Math.abs(toFile - fromFile);
        int rankDiff = Math.abs(toRank - fromRank);

        // Get the piece at the target location
        Piece targetPiece = board.getPiece(toFile, toRank);

        // Ensure the move follows the knight’s L-shape pattern
        boolean validPattern = (fileDiff == 2 && rankDiff == 1) || (fileDiff == 1 && rankDiff == 2);

        // Ensure the target is either empty or an opponent piece
        boolean notSelfCapture = targetPiece == null || !targetPiece.getColor().equals(piece.getColor());

        return validPattern && notSelfCapture;
    }
}
