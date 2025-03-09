package com.ChilliSauce;

public class ValidBishopMoves extends MoveValidator {

    @Override
    public boolean isValidMove(Board board, char fromFile, int fromRank, char toFile, int toRank) {
        Piece piece = board.getPiece(fromFile, fromRank);
        if (!(piece instanceof Bishop)) return false;

        // Bishop moves diagonally
        if (Math.abs(fromFile - toFile) != Math.abs(fromRank - toRank)) return false;

        // Check for obstacles
        int fileStep = (toFile > fromFile) ? 1 : -1;
        int rankStep = (toRank > fromRank) ? 1 : -1;

        char f = (char) (fromFile + fileStep);
        int r = fromRank + rankStep;
        while (f != toFile && r != toRank) {
            if (board.getPiece(f, r) != null) return false;
            f += fileStep;
            r += rankStep;
        }

        // Ensure destination is empty or occupied by an opponent
        Piece target = board.getPiece(toFile, toRank);
        return target == null || !target.getColor().equals(piece.getColor());
    }
}