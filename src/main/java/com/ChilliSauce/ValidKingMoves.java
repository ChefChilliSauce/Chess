package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

public class ValidKingMoves {

    /**
     * Returns all fully legal moves for the king at {@code index}, including valid castling.
     */
    public static List<Integer> getValidMoves(Board board, int index, boolean isWhite) {
        List<Integer> moves = new ArrayList<>();
        int rank = index / 8;
        int file = index % 8;

        // 1) Standard one-square moves
        for (int dr = -1; dr <= 1; dr++) {
            for (int df = -1; df <= 1; df++) {
                if (dr == 0 && df == 0) continue; // skip no-move
                int newRank = rank + dr;
                int newFile = file + df;
                if (newRank < 0 || newRank > 7 || newFile < 0 || newFile > 7) continue;

                int targetIndex = newRank * 8 + newFile;
                int targetPiece = board.getPiece(targetIndex);

                // Skip squares occupied by a friendly piece
                if (targetPiece != PieceConstants.NONE) {
                    boolean isTargetWhite = ((targetPiece & PieceConstants.WHITE) != 0);
                    if (isTargetWhite == isWhite) {
                        continue; // same color => can't capture
                    }
                }

                // Also ensure the king does not move into check
                if (!board.isSquareUnderAttack(targetIndex, !isWhite)) {
                    moves.add(targetIndex);
                }
            }
        }

        // 2) Castling checks: only add if:
        //    - King not in check right now
        //    - King and rook haven't moved
        //    - Squares between them are empty
        //    - Those squares are not attacked
        if (!board.isSquareUnderAttack(index, !isWhite)) {
            // We'll need to figure out which squares to check for kingside vs queenside
            // and confirm the rook hasn't moved, etc.

            boolean kingHasMoved = board.hasKingMoved(isWhite);

            // KINGSIDE
            // White's king is on e1 (index=4); castling would land on g1 (index=6)
            // Black's king is on e8 (index=60); castling would land on g8 (index=62)
            int kingSideTarget = isWhite ? 6 : 62;
            int rookFromKingSide = isWhite ? 7 : 63;  // where the rook sits
            if (!kingHasMoved) {
                // Check if that rook has moved:
                boolean rookHasMoved = board.hasRookMoved(rookFromKingSide);
                if (!rookHasMoved) {
                    // Check squares between king & rook: f1,g1 or f8,g8
                    int sq1 = isWhite ? 5 : 61;
                    int sq2 = isWhite ? 6 : 62;
                    if (board.getPiece(sq1) == PieceConstants.NONE &&
                            board.getPiece(sq2) == PieceConstants.NONE) {
                        // Check if those squares are attacked
                        if (!board.isSquareUnderAttack(sq1, !isWhite) &&
                                !board.isSquareUnderAttack(sq2, !isWhite)) {
                            moves.add(kingSideTarget);
                        }
                    }
                }
            }

            // QUEENSIDE
            // White's king is on e1 (4); castling would land on c1 (2)
            // Black's king is on e8 (60); castling would land on c8 (58)
            int queenSideTarget = isWhite ? 2 : 58;
            int rookFromQueenSide = isWhite ? 0 : 56; // where that rook sits
            if (!kingHasMoved) {
                boolean rookHasMoved = board.hasRookMoved(rookFromQueenSide);
                if (!rookHasMoved) {
                    // Check squares between king & rook: (b1=1, c1=2, d1=3) or (b8=57, c8=58, d8=59)
                    int sqB = isWhite ? 1 : 57;
                    int sqC = isWhite ? 2 : 58;
                    int sqD = isWhite ? 3 : 59;
                    if (board.getPiece(sqB) == PieceConstants.NONE &&
                            board.getPiece(sqC) == PieceConstants.NONE &&
                            board.getPiece(sqD) == PieceConstants.NONE) {
                        // Check if c1/c8 & d1/d8 are not attacked
                        // (the king only "passes" d1/d8 & lands on c1/c8)
                        if (!board.isSquareUnderAttack(sqD, !isWhite) &&
                                !board.isSquareUnderAttack(sqC, !isWhite)) {
                            moves.add(queenSideTarget);
                        }
                    }
                }
            }
        }

        return moves;
    }

    /**
     * Returns the squares the king "attacks" (for check detection), ignoring occupancy & color.
     * (No castling here, because castling is not an attack.)
     */
    public static List<Integer> getAttackSquares(Board board, int index, boolean isWhite) {
        List<Integer> attacks = new ArrayList<>();
        int rank = index / 8;
        int file = index % 8;
        for (int dr = -1; dr <= 1; dr++) {
            for (int df = -1; df <= 1; df++) {
                if (dr == 0 && df == 0) continue;
                int newRank = rank + dr;
                int newFile = file + df;
                if (newRank < 0 || newRank > 7 || newFile < 0 || newFile > 7) continue;
                int targetIndex = newRank * 8 + newFile;
                attacks.add(targetIndex);
            }
        }
        return attacks;
    }
}
