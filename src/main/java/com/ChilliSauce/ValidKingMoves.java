package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

public class ValidKingMoves {
    public static List<Integer> getValidMoves(Board board, int index, boolean isWhite) {
        List<Integer> validMoves = new ArrayList<>();
        int[] allPossible = {8, -8, 1, -1, 9, 7, -7, -9}; // King moves in all 8 directions

        for (int move : allPossible) {
            int target = index + move;

            // Ensure target is within bounds
            if (target < 0 || target >= 64) continue;

            // Prevent horizontal wrap-around (left-right edge cases)
            if ((index % 8 == 0 && (move == -1 || move == -9 || move == 7)) ||  // Left Edge Wrap
                    (index % 8 == 7 && (move == 1 || move == 9 || move == -7)))     // Right Edge Wrap
                continue;

            int targetPiece = board.getPiece(target);

            // ✅ Ensure the king does NOT capture its own color
            boolean isTargetWhite = (targetPiece & PieceConstants.WHITE) != 0;
            boolean isTargetBlack = (targetPiece & PieceConstants.BLACK) != 0;

            if ((isWhite && isTargetWhite) || (!isWhite && isTargetBlack)) {
                continue; // ❌ Skip if it's the same color
            }

            validMoves.add(target); // ✅ Only add legal moves
        }
        return validMoves;
    }
}
