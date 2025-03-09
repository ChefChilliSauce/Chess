package com.ChilliSauce;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MoveHighlighter {
    public static List<Point> getLegalMoves(Board board, char file, int rank) {
        List<Point> moves = new ArrayList<>();
        Piece piece = board.getPiece(file, rank);

        if (piece == null) return moves;

        MoveValidator validator = board.getMoveValidator(piece);
        if (validator == null) return moves;

        for (char f = 'a'; f <= 'h'; f++) {
            for (int r = 1; r <= 8; r++) {
                // Don't highlight the square the piece is already on (especially for King)
                if (f == file && r == rank) continue;

                // Get the piece at the target location
                Piece targetPiece = board.getPiece(f, r);

                // Check if the move is valid
                if (validator.isValidMove(board, file, rank, f, r)) {
                    // Prevent capturing own pieces
                    if (targetPiece == null || !targetPiece.getColor().equals(piece.getColor())) {
                        moves.add(new Point(fileToIndex(f), rankToIndex(r)));
                    }
                }
            }
        }
        return moves;
    }

    private static int fileToIndex(char file) {
        return file - 'a';
    }

    private static int rankToIndex(int rank) {
        return 8 - rank;
    }
}
