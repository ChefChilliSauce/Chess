package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

public class ValidPawnMoves {
    public static List<Integer> getValidMoves(Board board, int index, boolean isWhite) {
        List<Integer> validMoves = new ArrayList<>();

        int direction = isWhite ? 8 : -8;  // White moves up (+8), Black moves down (-8)
        int startRank = isWhite ? 1 : 6;   // White starts at rank 1, Black at rank 6
        int promotionRank = isWhite ? 7 : 0; // White promotes at rank 7, Black at rank 0

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
                int prevFile = index % 8; // Get file before move
                int currFile = target % 8; // Get file after move
                int fileDiff = Math.abs(currFile - prevFile);

                if (fileDiff == 1) { // ✅ Ensure diagonal movement
                    int targetPiece = board.getPiece(target);

                    // ✅ Capture only opponent pieces
                    boolean isTargetWhite = (targetPiece & PieceConstants.WHITE) != 0;
                    boolean isTargetBlack = (targetPiece & PieceConstants.BLACK) != 0;

                    if ((isWhite && isTargetBlack) || (!isWhite && isTargetWhite)) {
                        validMoves.add(target);
                    }
                }
            }
        }

        // 4️⃣ Handle Promotion
        if ((oneStep / 8) == promotionRank) {
            System.out.println("♛ Pawn promotion possible at index: " + oneStep);
        }

        return validMoves;
    }
}
