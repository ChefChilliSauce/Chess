package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final int[] board;
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
                    setPiece(index, piece | color); // ✅ FIXED: Pass only 2 arguments

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

        boolean isWhite = (piece & PieceConstants.WHITE) != 0;
        List<Integer> validMoves = new ArrayList<>();

        switch (piece & 7) { // Mask to get piece type
            case PieceConstants.PAWN -> validMoves = ValidPawnMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.KNIGHT -> validMoves = ValidKnightMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.BISHOP -> validMoves = ValidBishopMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.ROOK -> validMoves = ValidRookMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.QUEEN -> validMoves = ValidQueenMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.KING -> validMoves = ValidKingMoves.getValidMoves(this, index, isWhite);
        }

        return validMoves;
    }

    private int getIndex(char file, int rank) {
        return (rank - 1) * 8 + (file - 'a');
    }
}
