package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

public class ValidRookMoves {
    private static final int[] ROOK_OFFSETS = {8, -8, 1, -1}; // Vertical and horizontal moves

    public static List<Integer> getValidMoves(Board board, int index, boolean isWhite) {
        List<Integer> validMoves = new ArrayList<>();

        for (int offset : ROOK_OFFSETS) {
            int target = index;

            while (true) {
                int prevFile = target % 8;
                target += offset;

                if (target < 0 || target >= 64) break;  // ✅ Prevents out-of-bounds

                int currFile = target % 8;
                int fileDiff = Math.abs(currFile - prevFile);

                if ((offset == 1 || offset == -1) && fileDiff != 1) break; // ✅ Ensure horizontal movement

                int targetPiece = board.getPiece(target);

                if (targetPiece == PieceConstants.NONE) {
                    validMoves.add(target);
                } else {
                    boolean isTargetWhite = (targetPiece & PieceConstants.WHITE) != 0;
                    boolean isTargetBlack = (targetPiece & PieceConstants.BLACK) != 0;

                    if (isWhite && isTargetBlack || !isWhite && isTargetWhite) {
                        validMoves.add(target); // ✅ Capture allowed
                    }
                    break; // ✅ Stop after encountering a piece
                }
            }
        }
        return validMoves;
    }
}
