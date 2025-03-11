package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

class ValidKnightMoves {
    public static List<Integer> getValidMoves(Board board, int index, boolean isWhite) {
        List<Integer> validMoves = new ArrayList<>();

        // All knight move offsets (L-shape moves)
        int[] allPossible = {+17, +15, +10, -6, +6, -10, -15, -17};

        for (int offset : allPossible) {
            int targetIndex = index + offset;

            // ✅ Step 1: Ensure the move is within board bounds
            if (targetIndex < 0 || targetIndex >= 64) continue;

            // ✅ Step 2: Prevent knight from wrapping across the board (invalid moves)
            int currentFile = index % 8;
            int targetFile = targetIndex % 8;

            if (Math.abs(currentFile - targetFile) > 2) continue; // A knight can only move max 2 files left/right

            int targetPiece = board.getPiece(targetIndex);

            // ✅ Step 3: Check if the target square is empty or occupied by an opponent
            if (targetPiece == PieceConstants.NONE || (targetPiece & PieceConstants.WHITE) != (isWhite ? PieceConstants.WHITE : PieceConstants.BLACK)) {
                validMoves.add(targetIndex);
            }
        }
        return validMoves;
    }
}
