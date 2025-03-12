package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final int[] board;
    private boolean isWhiteTurn = true;  // ✅ Track current turn
    private Integer lastMoveFrom = null;
    private Integer lastMoveTo = null;
    private Integer enPassantTarget = null; // ✅ Track En Passant target square

    final String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public Board() {
        board = new int[64]; // 1D board representation
        loadFEN();
    }

    private void loadFEN() {
        final String[] parts = startFEN.split(" ");
        String[] rows = parts[0].split("/");

        for (int rank = 8; rank >= 1; rank--) {
            String row = rows[8 - rank];
            int fileIndex = 0;

            for (char ch : row.toCharArray()) {
                if (Character.isDigit(ch)) {
                    fileIndex += Character.getNumericValue(ch);
                } else {
                    int color = Character.isUpperCase(ch) ? PieceConstants.WHITE : PieceConstants.BLACK;
                    int piece = switch (Character.toLowerCase(ch)) {
                        case 'k' -> PieceConstants.KING;
                        case 'q' -> PieceConstants.QUEEN;
                        case 'r' -> PieceConstants.ROOK;
                        case 'n' -> PieceConstants.KNIGHT;
                        case 'b' -> PieceConstants.BISHOP;
                        case 'p' -> PieceConstants.PAWN;
                        default -> PieceConstants.NONE;
                    };

                    int index = getIndex((char) ('a' + fileIndex), rank);
                    setPiece(index, piece | color);

                    fileIndex++;
                }
            }
        }
    }

    public int getPiece(int index) {
        if (index < 0 || index >= 64) return PieceConstants.NONE;
        return board[index];
    }

    public void setPiece(int index, int piece) {
        if (index < 0 || index >= 64) return;
        board[index] = piece;
    }

    public List<Integer> getValidMoves(int index) {
        int piece = getPiece(index);
        if (piece == PieceConstants.NONE) return new ArrayList<>();

        boolean isWhitePiece = (piece & PieceConstants.WHITE) != 0;

        // ✅ Ensure correct player's turn
        if (isWhitePiece != isWhiteTurn) {
            System.out.println("❌ Not your turn! It's " + (isWhiteTurn ? "White's" : "Black's") + " turn.");
            return new ArrayList<>();
        }

        List<Integer> validMoves = new ArrayList<>();

        switch (piece & 7) { // Mask to get piece type
            case PieceConstants.PAWN -> validMoves = ValidPawnMoves.getValidMoves(this, index, isWhitePiece);
            case PieceConstants.KNIGHT -> validMoves = ValidKnightMoves.getValidMoves(this, index, isWhitePiece);
            case PieceConstants.BISHOP -> validMoves = ValidBishopMoves.getValidMoves(this, index, isWhitePiece);
            case PieceConstants.ROOK -> validMoves = ValidRookMoves.getValidMoves(this, index, isWhitePiece);
            case PieceConstants.QUEEN -> validMoves = ValidQueenMoves.getValidMoves(this, index, isWhitePiece);
            case PieceConstants.KING -> validMoves = ValidKingMoves.getValidMoves(this, index, isWhitePiece);
        }

        return validMoves;
    }

    public boolean makeMove(int fromIndex, int toIndex) {
        int piece = getPiece(fromIndex);
        if (piece == PieceConstants.NONE) return false;

        List<Integer> validMoves = getValidMoves(fromIndex);
        if (!validMoves.contains(toIndex)) {
            System.out.println("❌ Invalid move!");
            return false;
        }

        int targetPiece = getPiece(toIndex);
        boolean isCapture = targetPiece != PieceConstants.NONE;

        // ✅ En Passant Capture Logic
        if (enPassantTarget != null && toIndex == enPassantTarget) {
            int capturedPawnIndex = isWhiteTurn ? toIndex - 8 : toIndex + 8;
            setPiece(capturedPawnIndex, PieceConstants.NONE); // ✅ Remove bypassed pawn
            isCapture = true; // ✅ Mark as capture
        }

        // ✅ Handle Pawn Promotion
        boolean isWhite = (piece & PieceConstants.WHITE) != 0;
        int promotionRank = isWhite ? 7 : 0;
        boolean isPromotion = (toIndex / 8) == promotionRank && (piece & 7) == PieceConstants.PAWN;

        if (isPromotion) {
            System.out.println("♛ Pawn promoted at index: " + toIndex);
            setPiece(toIndex, PieceConstants.QUEEN | (isWhite ? PieceConstants.WHITE : PieceConstants.BLACK));
        } else {
            setPiece(toIndex, piece);
        }

        setPiece(fromIndex, PieceConstants.NONE);

        // ✅ Store last move (for En Passant)
        lastMoveFrom = fromIndex;
        lastMoveTo = toIndex;

        // ✅ Reset En Passant target unless a pawn moves two squares
        enPassantTarget = null;
        if ((piece & 7) == PieceConstants.PAWN && Math.abs(fromIndex - toIndex) == 16) {
            enPassantTarget = (fromIndex + toIndex) / 2;
        }

        // ✅ Play the correct sound
        if (isPromotion) {
            System.out.println("Promotion sound handled in GUI");
            // Promotion sound is already played above
        } else if (isCapture) {
            SoundManager.playCaptureSound(); // ✅ Capture sound for En Passant or normal capture
        } else {
            SoundManager.playMoveSound(); // ✅ Normal move sound
        }

        // ✅ Switch turn after a valid move
        isWhiteTurn = !isWhiteTurn;
        System.out.println("✅ Turn switched to: " + (isWhiteTurn ? "White" : "Black"));

        return true;
    }



    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public Integer getLastMoveFrom() {
        return lastMoveFrom;
    }

    public Integer getLastMoveTo() {
        return lastMoveTo;
    }

    public Integer getEnPassantTarget() {
        return enPassantTarget;
    }

    private int getIndex(char file, int rank) {
        return (rank - 1) * 8 + (file - 'a');
    }
}
