package com.ChilliSauce;

class ValidPawnMoves extends MoveValidator {
    @Override
    public boolean isValidMove(Board board, char fromFile, int fromRank, char toFile, int toRank) {
        Piece piece = board.getPiece(fromFile, fromRank);
        if (!(piece instanceof Pawn)) return false; // Ensure it's a Pawn

        Pawn pawn = (Pawn) piece;
        int direction = pawn.getColor().equals("white") ? 1 : -1;
        int startRank = pawn.getColor().equals("white") ? 2 : 7;

        // Move forward
        if (fromFile == toFile) {
            if (toRank == fromRank + direction && board.getPiece(toFile, toRank) == null) {
                return true; // One square forward
            }
            if (fromRank == startRank && toRank == fromRank + 2 * direction && board.getPiece(toFile, toRank) == null) {
                return true; // Two squares forward from start
            }
        }

        // Capture move
        if (Math.abs(toFile - fromFile) == 1 && toRank == fromRank + direction) {
            Piece target = board.getPiece(toFile, toRank);
            return target != null && !target.getColor().equals(pawn.getColor());
        }

        return false;
    }
}