package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

public class ValidQueenMoves {
    private static final int[] QUEEN_OFFSETS = {8, -8, 1, -1, 9, 7, -9, -7}; // Rook + Bishop moves

    public static List<Integer> getValidMoves(Board board, int index, boolean isWhite) {
        List<Integer> validMoves = new ArrayList<>();

        for (int offset : QUEEN_OFFSETS) {
            int target = index;

            while (true) {
                int prevFile = target % 8; // Column before moving
                target += offset;

                if (target < 0 || target >= 64) break;  // ✅ Prevents out-of-bounds

                int currFile = target % 8;
                int fileDiff = Math.abs(currFile - prevFile);
                if ((offset == 1 || offset == -1) && fileDiff != 1) break;
                if ((offset == 9 || offset == -9 || offset == 7 || offset == -7) && fileDiff != 1) break;

                int targetPiece = board.getPiece(target);

                if (targetPiece == PieceConstants.NONE) {
                    validMoves.add(target);
                } else {
                    boolean isTargetWhite = (targetPiece & PieceConstants.WHITE) != 0;
                    boolean isTargetBlack = (targetPiece & PieceConstants.BLACK) != 0;

                    // ✅ Corrected: Allow capturing opponent piece, but block friendly fire
                    if (isWhite && isTargetBlack || !isWhite && isTargetWhite) {
                        validMoves.add(target);
                    }
                    break; // Stop after encountering a piece
                }
            }
        }
        return validMoves;
    }
}
