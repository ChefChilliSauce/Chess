package com.ChilliSauce;

public class ValidQueenMoves extends MoveValidator {

    @Override
    public boolean isValidMove(Board board, char fromFile, int fromRank, char toFile, int toRank) {
        Piece piece = board.getPiece(fromFile, fromRank);
        if (!(piece instanceof Queen)) return false;

        // Queen moves like a Rook or a Bishop
        boolean isRookMove = isValidRookMove(board, fromFile, fromRank, toFile, toRank);
        boolean isBishopMove = isValidBishopMove(board, fromFile, fromRank, toFile, toRank);

        if (!isRookMove && !isBishopMove) return false; // Move must be either like a Rook or a Bishop

        // Ensure destination is empty or occupied by an opponent
        Piece target = board.getPiece(toFile, toRank);
        return target == null || !target.getColor().equals(piece.getColor());
    }

    private boolean isValidRookMove(Board board, char fromFile, int fromRank, char toFile, int toRank) {
        if (fromFile != toFile && fromRank != toRank) return false; // Must be straight-line movement

        int step = (fromFile == toFile) ? (toRank > fromRank ? 1 : -1) : (toFile > fromFile ? 1 : -1);
        if (fromFile == toFile) { // Vertical move
            for (int r = fromRank + step; r != toRank; r += step) {
                if (board.getPiece(fromFile, r) != null) return false;
            }
        } else { // Horizontal move
            for (char f = (char) (fromFile + step); f != toFile; f += step) {
                if (board.getPiece(f, fromRank) != null) return false;
            }
        }
        return true;
    }

    private boolean isValidBishopMove(Board board, char fromFile, int fromRank, char toFile, int toRank) {
        if (Math.abs(fromFile - toFile) != Math.abs(fromRank - toRank)) return false; // Must be diagonal move

        int fileStep = (toFile > fromFile) ? 1 : -1;
        int rankStep = (toRank > fromRank) ? 1 : -1;
        char f = (char) (fromFile + fileStep);
        int r = fromRank + rankStep;

        while (f != toFile && r != toRank) {
            if (board.getPiece(f, r) != null) return false;
            f += fileStep;
            r += rankStep;
        }
        return true;
    }
}
