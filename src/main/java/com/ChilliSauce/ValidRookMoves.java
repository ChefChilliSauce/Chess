package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

public class ValidRookMoves {
    private static final int[] ROOK_OFFSETS = {8, -8, 1, -1}; // Vertical & Horizontal moves

    public static List<Integer> getValidMoves(Board board, int index, boolean isWhite) {
        List<Integer> validMoves = new ArrayList<>();

        for (int offset : ROOK_OFFSETS) {
            int target = index;

            while (true) {
                int prevFile = target % 8; // Column before moving
                target += offset;

                // Boundary check
                if (target < 0 || target >= 64) break;

                int currFile = target % 8;
                int fileDiff = Math.abs(currFile - prevFile);

                // Ensure proper movement (horizontal or vertical only)
                if ((offset == 1 || offset == -1) && fileDiff != 1) break;

                int targetPiece = board.getPiece(target);

                if (targetPiece == PieceConstants.NONE) {
                    validMoves.add(target);
                } else {
                    // Allow capturing opponent piece
                    if ((targetPiece & PieceConstants.WHITE) != (isWhite ? PieceConstants.WHITE : PieceConstants.BLACK)) {
                        validMoves.add(target);
                    }
                    break; // Stop after capturing
                }
            }
        }
        return validMoves;
    }
}
