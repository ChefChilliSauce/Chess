package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

public class ValidKnightMoves {
    private static final int[] KNIGHT_MOVES = {17, 15, 10, 6, -6, -10, -15, -17}; // L-shape moves

    public static List<Integer> getValidMoves(Board board, int index, boolean isWhite) {
        List<Integer> validMoves = new ArrayList<>();

        for (int move : KNIGHT_MOVES) {
            int target = index + move;

            // ✅ Ensure target is within bounds
            if (target < 0 || target >= 64) continue;

            // ✅ Check if knight crosses board edges (wrap-around prevention)
            int startFile = index % 8;
            int targetFile = target % 8;
            int fileDiff = Math.abs(startFile - targetFile);

            if (fileDiff != 1 && fileDiff != 2) continue; // ✅ Ensure L-shape is correct

            int targetPiece = board.getPiece(target);

            boolean isTargetWhite = (targetPiece & PieceConstants.WHITE) != 0;
            boolean isTargetBlack = (targetPiece & PieceConstants.BLACK) != 0;

            if (targetPiece == PieceConstants.NONE || (isWhite && isTargetBlack) || (!isWhite && isTargetWhite)) {
                validMoves.add(target); // ✅ Capture enemy or move to empty square
            }
        }
        return validMoves;
    }
}
