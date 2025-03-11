package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

class ValidPawnMoves {
    public static List<Integer> getValidMoves(Board board, int index, boolean isWhite) {
        List<Integer> validMoves = new ArrayList<>();

        int direction = isWhite ? 8 : -8;  // White moves up (+8), Black moves down (-8)
        int startRank = isWhite ? 1 : 6;   // White starts at rank 1, Black at rank 6

        // 1️⃣ Normal Move (Single Step)
        int oneStep = index + direction;
        if (oneStep >= 0 && oneStep < 64 && board.getPiece(oneStep) == PieceConstants.NONE) {
            validMoves.add(oneStep);

            // 2️⃣ First Move (Two Steps) - Only if first step is empty
            if ((index / 8) == startRank) {  // Check if pawn is at its starting rank
                int twoSteps = index + (2 * direction);
                if (board.getPiece(twoSteps) == PieceConstants.NONE) {
                    validMoves.add(twoSteps);
                }
            }
        }

        // 3️⃣ Capturing Moves (Diagonal Left & Right)
        int[] captureOffsets = {7, 9};
        for (int offset : captureOffsets) {
            int target = index + (isWhite ? offset : -offset);
            if (target >= 0 && target < 64) {
                int targetPiece = board.getPiece(target);
                if (targetPiece != PieceConstants.NONE && (targetPiece & PieceConstants.WHITE) != (isWhite ? PieceConstants.WHITE : PieceConstants.BLACK)) {
                    validMoves.add(target);
                }
            }
        }

        return validMoves;
    }
}
