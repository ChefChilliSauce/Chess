package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final int[] board;
    private boolean isWhiteTurn = true;
    private Integer lastMoveFrom = null;
    private Integer lastMoveTo = null;
    private Integer enPassantTarget = null;

    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteKingsideRookMoved = false;
    private boolean whiteQueensideRookMoved = false;
    private boolean blackKingsideRookMoved = false;
    private boolean blackQueensideRookMoved = false;

    final String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public Board() {
        board = new int[64];
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

    // Returns legal moves for a piece (only for the side whose turn it is).
    public List<Integer> getValidMoves(int index) {
        int piece = getPiece(index);
        if (piece == PieceConstants.NONE) return new ArrayList<>();
        boolean isWhite = (piece & PieceConstants.WHITE) != 0;
        if (isWhite != isWhiteTurn) return new ArrayList<>();

        List<Integer> validMoves = new ArrayList<>();
        switch (piece & 7) {
            case PieceConstants.PAWN -> validMoves = ValidPawnMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.KNIGHT -> validMoves = ValidKnightMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.BISHOP -> validMoves = ValidBishopMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.ROOK -> validMoves = ValidRookMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.QUEEN -> validMoves = ValidQueenMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.KING -> validMoves = ValidKingMoves.getValidMoves(this, index, isWhite);
        }
        return validMoves;
    }

    // New method: returns the squares attacked by a piece regardless of turn.
    public List<Integer> getAttackSquares(int index) {
        int piece = getPiece(index);
        if (piece == PieceConstants.NONE) return new ArrayList<>();
        boolean isWhite = (piece & PieceConstants.WHITE) != 0;
        List<Integer> attackSquares = new ArrayList<>();
        switch (piece & 7) {
            case PieceConstants.PAWN -> attackSquares = ValidPawnMoves.getAttackSquares(this, index, isWhite);
            case PieceConstants.KNIGHT -> attackSquares = ValidKnightMoves.getValidMoves(this, index, isWhite); // knight moves = attack squares
            case PieceConstants.BISHOP -> attackSquares = ValidBishopMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.ROOK -> attackSquares = ValidRookMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.QUEEN -> attackSquares = ValidQueenMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.KING -> attackSquares = ValidKingMoves.getAttackSquares(this, index, isWhite);
        }
        return attackSquares;
    }

    // Uses the attack squares to decide if a square is under threat.
    public boolean isSquareUnderAttack(int index, boolean byWhite) {
        for (int i = 0; i < 64; i++) {
            int piece = getPiece(i);
            if (piece == PieceConstants.NONE) continue;
            boolean isWhitePiece = (piece & PieceConstants.WHITE) != 0;
            if (isWhitePiece != byWhite) continue;
            List<Integer> attackSquares = getAttackSquares(i);
            if (attackSquares.contains(index)) return true;
        }
        return false;
    }

    public boolean hasKingMoved(boolean isWhite) {
        return isWhite ? whiteKingMoved : blackKingMoved;
    }

    public boolean hasRookMoved(int rookIndex) {
        return switch (rookIndex) {
            case 56 -> whiteQueensideRookMoved;
            case 63 -> whiteKingsideRookMoved;
            case 0 -> blackQueensideRookMoved;
            case 7 -> blackKingsideRookMoved;
            default -> true;
        };
    }

    public boolean makeMove(int fromIndex, int toIndex, ChessGUI gui) {
        int piece = getPiece(fromIndex);
        if (piece == PieceConstants.NONE) return false;

        // 1) Check if this move is in the valid moves list
        List<Integer> validMoves = getValidMoves(fromIndex);
        if (!validMoves.contains(toIndex)) {
            System.out.println("❌ Invalid move!");
            return false;
        }

        int targetPiece = getPiece(toIndex);
        boolean isCapture = (targetPiece != PieceConstants.NONE);
        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);

        // 2) Check for castling (king moves 2 squares)
        if ((piece & 7) == PieceConstants.KING && Math.abs(fromIndex - toIndex) == 2) {
            // Has the king already moved?
            if (isWhite ? whiteKingMoved : blackKingMoved) {
                System.out.println("❌ Castling not allowed! King has already moved.");
                return false;
            }

            // Determine if it’s kingside or queenside
            int rookFrom, rookTo;
            if (toIndex == (isWhite ? 6 : 62)) {
                // Kingside
                rookFrom = isWhite ? 7 : 63;
                rookTo   = isWhite ? 5 : 61;
            } else {
                // Queenside
                rookFrom = isWhite ? 0 : 56;
                rookTo   = isWhite ? 3 : 59;
            }

            // Has the rook moved?
            if ((rookFrom == 7 && whiteKingsideRookMoved) ||
                    (rookFrom == 0 && whiteQueensideRookMoved) ||
                    (rookFrom == 63 && blackKingsideRookMoved) ||
                    (rookFrom == 56 && blackQueensideRookMoved)) {
                System.out.println("❌ Castling not allowed! Rook has already moved.");
                return false;
            }

            // Is there actually a rook of the correct color on rookFrom?
            int expectedRook = PieceConstants.ROOK | (isWhite ? PieceConstants.WHITE : PieceConstants.BLACK);
            int actualRook   = getPiece(rookFrom);
            if (actualRook != expectedRook) {
                System.out.println("❌ Castling not allowed! Rook missing or incorrect.");
                return false;
            }

            // Make sure squares in between are empty
            if (isWhite) {
                if (toIndex == 6) { // White kingside: f1(5), g1(6)
                    if (getPiece(5) != PieceConstants.NONE || getPiece(6) != PieceConstants.NONE) {
                        System.out.println("❌ Castling not allowed! Pieces in between.");
                        return false;
                    }
                } else {            // White queenside: b1(1), c1(2), d1(3)
                    if (getPiece(1) != PieceConstants.NONE ||
                            getPiece(2) != PieceConstants.NONE ||
                            getPiece(3) != PieceConstants.NONE) {
                        System.out.println("❌ Castling not allowed! Pieces in between.");
                        return false;
                    }
                }
            } else {
                if (toIndex == 62) { // Black kingside: f8(61), g8(62)
                    if (getPiece(61) != PieceConstants.NONE || getPiece(62) != PieceConstants.NONE) {
                        System.out.println("❌ Castling not allowed! Pieces in between.");
                        return false;
                    }
                } else {             // Black queenside: b8(57), c8(58), d8(59)
                    if (getPiece(57) != PieceConstants.NONE ||
                            getPiece(58) != PieceConstants.NONE ||
                            getPiece(59) != PieceConstants.NONE) {
                        System.out.println("❌ Castling not allowed! Pieces in between.");
                        return false;
                    }
                }
            }

            // Check that the king is not in check and does not pass through or land on attacked squares
            int middleSquare = (fromIndex + toIndex) / 2;
            if (isSquareUnderAttack(fromIndex, !isWhite) ||
                    isSquareUnderAttack(middleSquare, !isWhite) ||
                    isSquareUnderAttack(toIndex, !isWhite)) {
                System.out.println("❌ Castling not allowed! King passes through or lands on an attacked square.");
                return false;
            }

            // Perform the castling move
            setPiece(rookTo, actualRook);
            setPiece(rookFrom, PieceConstants.NONE);
            setPiece(toIndex, piece);
            setPiece(fromIndex, PieceConstants.NONE);

            // Mark king and rook as moved
            if (isWhite) {
                whiteKingMoved = true;
                if (rookFrom == 7)  whiteKingsideRookMoved = true;
                else if (rookFrom == 0) whiteQueensideRookMoved = true;
            } else {
                blackKingMoved = true;
                if (rookFrom == 63) blackKingsideRookMoved = true;
                else if (rookFrom == 56) blackQueensideRookMoved = true;
            }

            SoundManager.playCastlingSound();
            gui.repaintBoard();
            isWhiteTurn = !isWhiteTurn;
            return true;
        }

        // 3) En Passant check: If a pawn lands on the enPassantTarget, it's an en passant capture
        Integer epTarget = getEnPassantTarget();  // can be null
        boolean isEnPassant = false;
        if ((piece & 7) == PieceConstants.PAWN && epTarget != null && epTarget == toIndex) {
            isEnPassant = true;
        }

        // 4) Normal move (non-castling)
        setPiece(toIndex, piece);
        setPiece(fromIndex, PieceConstants.NONE);

        // If en passant, remove the captured pawn
        if (isEnPassant) {
            int capturedPawnIndex = isWhite ? (toIndex - 8) : (toIndex + 8);
            setPiece(capturedPawnIndex, PieceConstants.NONE);
            isCapture = true; // so we can play the capture sound
        }

        // If the king or a rook moves normally, update the corresponding moved flags
        if ((piece & 7) == PieceConstants.KING) {
            if (isWhite) whiteKingMoved = true;
            else blackKingMoved = true;
        } else if ((piece & 7) == PieceConstants.ROOK) {
            switch (fromIndex) {
                case 7  -> whiteKingsideRookMoved = true;
                case 0  -> whiteQueensideRookMoved = true;
                case 63 -> blackKingsideRookMoved = true;
                case 56 -> blackQueensideRookMoved = true;
            }
        }

        // 5) Pawn Promotion
        int promotionRank = isWhite ? 7 : 0;
        boolean isPromotion = ((piece & 7) == PieceConstants.PAWN) && (toIndex / 8 == promotionRank);
        if (isPromotion) {
            //setPiece(toIndex, PieceConstants.QUEEN | (isWhite ? PieceConstants.WHITE : PieceConstants.BLACK));
            // e.g. SoundManager.playPromotionSound();
        } else if (isCapture) {
            SoundManager.playCaptureSound();
        } else {
            SoundManager.playMoveSound();
        }

        // 6) Update last-move info, handle new enPassant target if a pawn moved 2 squares
        lastMoveFrom = fromIndex;
        lastMoveTo = toIndex;
        enPassantTarget = null;
        if (((piece & 7) == PieceConstants.PAWN) && Math.abs(fromIndex - toIndex) == 16) {
            enPassantTarget = (fromIndex + toIndex) / 2;
        }

        isWhiteTurn = !isWhiteTurn;
        gui.repaintBoard();
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
