package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;
public class ValidKingMoves{
    public static List<Integer> getValidMoves(Board board, int index, boolean isWhite) {
        List<Integer> validMoves = new ArrayList<>();
        int[] allPossible = {8, -8, 1, -1, 9, 7, -7, -9}; // King moves in all 8 directions

        for (int x : allPossible) {
            int target = index + x;

            // Ensure target is within bounds
            if (target < 0 || target >= 64) continue;

            // Prevent horizontal wrap-around
            if ((index % 8 == 0 && (x == -1 || x == -9 || x == 7)) ||
                    (index % 8 == 7 && (x == 1 || x == 9 || x == -7)))
                continue;

            int targetPiece = board.getPiece(target);

            // Stop if the square is occupied by the same color
            if (targetPiece != PieceConstants.NONE &&
                    (targetPiece & PieceConstants.WHITE) == (isWhite ? PieceConstants.WHITE : PieceConstants.BLACK)) {
                continue;
            }

            validMoves.add(target); // Add empty square or opponent capture move
        }
        return validMoves;
    }

}
