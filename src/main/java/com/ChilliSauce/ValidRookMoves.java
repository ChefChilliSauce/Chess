package com.ChilliSauce;

class ValidRookMoves extends MoveValidator {
    @Override
    public boolean isValidMove(Board board, char fromFile, int fromRank, char toFile, int toRank) {
        Piece piece = board.getPiece(fromFile, fromRank);
        if (!(piece instanceof Rook)) return false;

        // Rook moves in a straight line either horizontally or vertically
        if (fromFile != toFile && fromRank != toRank) return false;

        // Check for obstacles in the path
        if (fromFile == toFile) { // Vertical move
            int step = (toRank > fromRank) ? 1 : -1;
            for (int r = fromRank + step; r != toRank; r += step) {
                if (board.getPiece(fromFile, r) != null) return false;
            }
        } else { // Horizontal move
            int step = (toFile > fromFile) ? 1 : -1;
            for (char f = (char) (fromFile + step); f != toFile; f += step) {
                if (board.getPiece(f, fromRank) != null) return false;
            }
        }

        // Ensure destination is empty or occupied by an opponent
        Piece target = board.getPiece(toFile, toRank);
        return target == null || !target.getColor().equals(piece.getColor());
    }
}
