package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

public class ValidBishopMoves {
    private static final int[] BISHOP_OFFSETS = {9, 7, -9, -7}; // Diagonal moves

    public static List<Integer> getValidMoves(Board board, int index, boolean isWhite) {
        List<Integer> validMoves = new ArrayList<>();

        for (int offset : BISHOP_OFFSETS) {
            int target = index;

            while (true) {
                int prevFile = target % 8; // Store previous column
                target += offset;

                // Boundary check
                if (target < 0 || target >= 64) break;

                int currFile = target % 8;
                int fileDiff = Math.abs(currFile - prevFile);

                // Ensure bishop doesn't wrap across rows
                if (fileDiff != 1) break;

                int targetPiece = board.getPiece(target);

                if (targetPiece == PieceConstants.NONE) {
                    validMoves.add(target); // Add empty square
                } else {
                    // Capture opponent piece
                    if ((targetPiece & PieceConstants.WHITE) != (isWhite ? PieceConstants.WHITE : PieceConstants.BLACK)) {
                        validMoves.add(target);
                    }
                    break; // Stop on first obstacle
                }
            }
        }

        return validMoves;
    }
}
