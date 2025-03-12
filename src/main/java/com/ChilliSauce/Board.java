package com.ChilliSauce;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final int[] board;
    private boolean isWhiteTurn = true;

    private Integer lastMoveFrom = null;
    private Integer lastMoveTo = null;
    private Integer enPassantTarget = null;

    // Track which piece was captured on the last move (for GUI notation)
    private int lastCapturedPiece = PieceConstants.NONE;

    // King/Rook moved flags for castling
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

    // ---------------------------------------------------------
    // 1) FEN loading (basic)
    // ---------------------------------------------------------
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

    private int getIndex(char file, int rank) {
        return (rank - 1) * 8 + (file - 'a');
    }

    // ---------------------------------------------------------
    // 2) Basic getters/setters
    // ---------------------------------------------------------
    public int getPiece(int index) {
        if (index < 0 || index >= 64) return PieceConstants.NONE;
        return board[index];
    }

    public void setPiece(int index, int piece) {
        if (index < 0 || index >= 64) return;
        board[index] = piece;
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

    // **Getter for the last captured piece** so the GUI can check if we did a capture
    public int getLastMoveCapturedPiece() {
        return lastCapturedPiece;
    }

    // ---------------------------------------------------------
    // 3) Generating valid moves
    // ---------------------------------------------------------
    // (This calls out to your Valid*Moves classes)
    public List<Integer> getValidMoves(int index) {
        int piece = getPiece(index);
        if (piece == PieceConstants.NONE) return new ArrayList<>();
        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);

        // Must match the side to move
        if (isWhite != isWhiteTurn) {
            return new ArrayList<>();
        }

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

    // For check detection
    public boolean isSquareUnderAttack(int index, boolean byWhite) {
        for (int i = 0; i < 64; i++) {
            int piece = getPiece(i);
            if (piece == PieceConstants.NONE) continue;

            boolean isWhitePiece = ((piece & PieceConstants.WHITE) != 0);
            if (isWhitePiece != byWhite) continue;

            // Attack squares ignoring turn
            List<Integer> attackSquares = getAttackSquares(i);
            if (attackSquares.contains(index)) {
                return true;
            }
        }
        return false;
    }

    // "Attack squares" version that doesn't check whose turn it is
    // We rely on the "getAttackSquares" methods in each piece class
    public List<Integer> getAttackSquares(int index) {
        int piece = getPiece(index);
        if (piece == PieceConstants.NONE) return new ArrayList<>();
        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);

        // For each piece type, call a separate "attack squares" method
        switch (piece & 7) {
            case PieceConstants.PAWN -> {
                return ValidPawnMoves.getAttackSquares(this, index, isWhite);
            }
            case PieceConstants.KNIGHT -> {
                return ValidKnightMoves.getValidMoves(this, index, isWhite);
            }
            case PieceConstants.BISHOP -> {
                return ValidBishopMoves.getValidMoves(this, index, isWhite);
            }
            case PieceConstants.ROOK -> {
                return ValidRookMoves.getValidMoves(this, index, isWhite);
            }
            case PieceConstants.QUEEN -> {
                return ValidQueenMoves.getValidMoves(this, index, isWhite);
            }
            case PieceConstants.KING -> {
                return ValidKingMoves.getAttackSquares(this, index, isWhite);
            }
        }
        return new ArrayList<>();
    }

    // ---------------------------------------------------------
    // 4) Make Move (includes castling, en passant, promotion)
    // ---------------------------------------------------------
    public boolean makeMove(int fromIndex, int toIndex, ChessGUI gui) {
        int piece = getPiece(fromIndex);
        if (piece == PieceConstants.NONE) return false;

        // Check if the move is in the valid moves list
        List<Integer> validMoves = getValidMoves(fromIndex);
        if (!validMoves.contains(toIndex)) {
            System.out.println("❌ Invalid move!");
            return false;
        }

        int targetPiece = getPiece(toIndex);
        boolean isCapture = (targetPiece != PieceConstants.NONE);
        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);

        // Reset lastCapturedPiece each time we start a new move
        lastCapturedPiece = PieceConstants.NONE;

        // Check if castling (king moves 2 squares)
        if ((piece & 7) == PieceConstants.KING && Math.abs(fromIndex - toIndex) == 2) {
            // Has the king moved?
            if (isWhite ? whiteKingMoved : blackKingMoved) {
                System.out.println("❌ Castling not allowed! King has already moved.");
                return false;
            }

            int rookFrom, rookTo;
            if (toIndex == (isWhite ? 6 : 62)) {
                // Kingside
                rookFrom = isWhite ? 7 : 63;
                rookTo = isWhite ? 5 : 61;
            } else {
                // Queenside
                rookFrom = isWhite ? 0 : 56;
                rookTo = isWhite ? 3 : 59;
            }

            // Check if the rook has moved
            if ((rookFrom == 7 && whiteKingsideRookMoved) ||
                    (rookFrom == 0 && whiteQueensideRookMoved) ||
                    (rookFrom == 63 && blackKingsideRookMoved) ||
                    (rookFrom == 56 && blackQueensideRookMoved)) {
                System.out.println("❌ Castling not allowed! Rook has already moved.");
                return false;
            }

            // Ensure there's a correct rook there
            int expectedRook = PieceConstants.ROOK | (isWhite ? PieceConstants.WHITE : PieceConstants.BLACK);
            int actualRook = getPiece(rookFrom);
            if (actualRook != expectedRook) {
                System.out.println("❌ Castling not allowed! Rook missing or incorrect.");
                return false;
            }

            // Check squares in between are empty
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

            // Check that the king is not in check, and doesn't move through or land on an attacked square
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

            // Mark king/rook as moved
            if (isWhite) {
                whiteKingMoved = true;
                if (rookFrom == 7) whiteKingsideRookMoved = true;
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

        // Check en passant
        Integer epTarget = getEnPassantTarget();  // could be null
        boolean isEnPassant = false;
        if ((piece & 7) == PieceConstants.PAWN && epTarget != null && epTarget == toIndex) {
            // Pawn lands on the enPassantTarget => en passant capture
            isEnPassant = true;
        }

        // Normal move (non-castling)
        setPiece(toIndex, piece);
        setPiece(fromIndex, PieceConstants.NONE);

        // If en passant, remove the captured pawn behind the new square
        if (isEnPassant) {
            int capturedPawnIndex = isWhite ? (toIndex - 8) : (toIndex + 8);
            int epPawn = getPiece(capturedPawnIndex);
            lastCapturedPiece = epPawn; // store it so the GUI knows we captured
            setPiece(capturedPawnIndex, PieceConstants.NONE);
            isCapture = true;
        }

        // If it was a normal capture, store the captured piece
        if (!isEnPassant && isCapture) {
            lastCapturedPiece = targetPiece;
        }

        // If king or rook moves normally, mark them as moved
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

        // Pawn promotion
        int promotionRank = isWhite ? 7 : 0;
        boolean isPromotion = ((piece & 7) == PieceConstants.PAWN) && ((toIndex / 8) == promotionRank);
        if (isPromotion) {
            // We do NOT auto-queen here, so the GUI can show the popup
            // If you wanted to auto-queen, you'd do:
            // setPiece(toIndex, PieceConstants.QUEEN | (isWhite ? PieceConstants.WHITE : PieceConstants.BLACK));
        } else if (isCapture) {
            SoundManager.playCaptureSound();
        } else {
            SoundManager.playMoveSound();
        }

        // Update last move info
        lastMoveFrom = fromIndex;
        lastMoveTo = toIndex;

        // Set or reset enPassantTarget
        enPassantTarget = null;
        if (((piece & 7) == PieceConstants.PAWN) && Math.abs(fromIndex - toIndex) == 16) {
            enPassantTarget = (fromIndex + toIndex) / 2;
        }

        isWhiteTurn = !isWhiteTurn;
        gui.repaintBoard();
        return true;
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
}














