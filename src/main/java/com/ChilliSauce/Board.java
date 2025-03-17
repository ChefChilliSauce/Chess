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

    // Copy constructor for simulating moves.
    public Board(Board original) {
        this.board = original.board.clone();
        this.isWhiteTurn = original.isWhiteTurn;
        this.lastMoveFrom = original.lastMoveFrom;
        this.lastMoveTo = original.lastMoveTo;
        this.enPassantTarget = original.enPassantTarget;
        this.lastCapturedPiece = original.lastCapturedPiece;
        this.whiteKingMoved = original.whiteKingMoved;
        this.blackKingMoved = original.blackKingMoved;
        this.whiteKingsideRookMoved = original.whiteKingsideRookMoved;
        this.whiteQueensideRookMoved = original.whiteQueensideRookMoved;
        this.blackKingsideRookMoved = original.blackKingsideRookMoved;
        this.blackQueensideRookMoved = original.blackQueensideRookMoved;
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

    // Getter for the last captured piece so the GUI can check if a capture occurred.
    public int getLastMoveCapturedPiece() {
        return lastCapturedPiece;
    }

    // ---------------------------------------------------------
    // 3) Generating valid moves (filtering out moves that leave king in check)
    // ---------------------------------------------------------
    public List<Integer> getValidMoves(int index) {
        int piece = getPiece(index);
        if (piece == PieceConstants.NONE) return new ArrayList<>();
        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);

        // Must match the side to move
        if (isWhite != isWhiteTurn) {
            return new ArrayList<>();
        }

        List<Integer> moves;
        switch (piece & 7) {
            case PieceConstants.PAWN -> moves = ValidPawnMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.KNIGHT -> moves = ValidKnightMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.BISHOP -> moves = ValidBishopMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.ROOK -> moves = ValidRookMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.QUEEN -> moves = ValidQueenMoves.getValidMoves(this, index, isWhite);
            case PieceConstants.KING -> moves = ValidKingMoves.getValidMoves(this, index, isWhite);
            default -> moves = new ArrayList<>();
        }

        // Filter out moves that leave the king in check.
        List<Integer> legalMoves = new ArrayList<>();
        for (Integer move : moves) {
            Board simulation = new Board(this);
            simulation.setPiece(move, simulation.getPiece(index));
            simulation.setPiece(index, PieceConstants.NONE);
            if (!simulation.isKingInCheck(isWhite)) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    // Helper method: find the king's position for a given side.
    private int findKing(boolean isWhite) {
        for (int i = 0; i < 64; i++) {
            int p = getPiece(i);
            if (p != PieceConstants.NONE && (p & 7) == PieceConstants.KING &&
                    (((p & PieceConstants.WHITE) != 0) == isWhite)) {
                return i;
            }
        }
        return -1; // Should never happen.
    }

    // Helper method: returns true if the king for the given side is in check.
    private boolean isKingInCheck(boolean isWhite) {
        int kingIndex = findKing(isWhite);
        if (kingIndex == -1) return true; // Treat missing king as in check.
        return isSquareUnderAttack(kingIndex, !isWhite);
    }

    // ---------------------------------------------------------
    // 4) Check if a square is under attack and get attack squares
    // ---------------------------------------------------------
    public boolean isSquareUnderAttack(int index, boolean byWhite) {
        for (int i = 0; i < 64; i++) {
            int p = getPiece(i);
            if (p == PieceConstants.NONE) continue;
            boolean isWhitePiece = ((p & PieceConstants.WHITE) != 0);
            if (isWhitePiece != byWhite) continue;
            List<Integer> attackSquares = getAttackSquares(i);
            if (attackSquares.contains(index)) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> getAttackSquares(int index) {
        int piece = getPiece(index);
        if (piece == PieceConstants.NONE) return new ArrayList<>();
        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);

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
    // 5) Make Move (includes castling, en passant, promotion)
    // ---------------------------------------------------------
    /**
     * Executes a move from the given fromIndex to toIndex.
     * Also calls gui.onPieceCaptured(capturedPiece, capturingSideIsWhite) if a capture occurs.
     */
    public boolean makeMove(int fromIndex, int toIndex, AlternateChessGUI gui) {
        int piece = getPiece(fromIndex);
        if (piece == PieceConstants.NONE) return false;

        List<Integer> validMoves = getValidMoves(fromIndex);
        if (!validMoves.contains(toIndex)) {
            System.out.println("❌ Invalid move!");
            return false;
        }

        int targetPiece = getPiece(toIndex);
        // 1. Prevent capturing the king.
        if (targetPiece != PieceConstants.NONE && (targetPiece & 7) == PieceConstants.KING) {
            System.out.println("❌ Cannot capture the king!");
            return false;
        }

        boolean isCapture = (targetPiece != PieceConstants.NONE);
        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);

        // Reset lastCapturedPiece at the start of a move.
        lastCapturedPiece = PieceConstants.NONE;

        // ----------------------
        // Handle Castling
        // ----------------------
        if ((piece & 7) == PieceConstants.KING && Math.abs(fromIndex - toIndex) == 2) {
            if (isWhite ? whiteKingMoved : blackKingMoved) {
                System.out.println("❌ Castling not allowed! King has already moved.");
                return false;
            }
            int rookFrom, rookTo;
            if (toIndex == (isWhite ? 6 : 62)) { // Kingside
                rookFrom = isWhite ? 7 : 63;
                rookTo = isWhite ? 5 : 61;
            } else { // Queenside
                rookFrom = isWhite ? 0 : 56;
                rookTo = isWhite ? 3 : 59;
            }
            if ((rookFrom == 7 && whiteKingsideRookMoved) ||
                    (rookFrom == 0 && whiteQueensideRookMoved) ||
                    (rookFrom == 63 && blackKingsideRookMoved) ||
                    (rookFrom == 56 && blackQueensideRookMoved)) {
                System.out.println("❌ Castling not allowed! Rook has already moved.");
                return false;
            }
            int expectedRook = PieceConstants.ROOK | (isWhite ? PieceConstants.WHITE : PieceConstants.BLACK);
            int actualRook = getPiece(rookFrom);
            if (actualRook != expectedRook) {
                System.out.println("❌ Castling not allowed! Rook missing or incorrect.");
                return false;
            }
            if (isWhite) {
                if (toIndex == 6) {
                    if (getPiece(5) != PieceConstants.NONE || getPiece(6) != PieceConstants.NONE) {
                        System.out.println("❌ Castling not allowed! Pieces in between.");
                        return false;
                    }
                } else {
                    if (getPiece(1) != PieceConstants.NONE ||
                            getPiece(2) != PieceConstants.NONE ||
                            getPiece(3) != PieceConstants.NONE) {
                        System.out.println("❌ Castling not allowed! Pieces in between.");
                        return false;
                    }
                }
            } else {
                if (toIndex == 62) {
                    if (getPiece(61) != PieceConstants.NONE || getPiece(62) != PieceConstants.NONE) {
                        System.out.println("❌ Castling not allowed! Pieces in between.");
                        return false;
                    }
                } else {
                    if (getPiece(57) != PieceConstants.NONE ||
                            getPiece(58) != PieceConstants.NONE ||
                            getPiece(59) != PieceConstants.NONE) {
                        System.out.println("❌ Castling not allowed! Pieces in between.");
                        return false;
                    }
                }
            }
            int middleSquare = (fromIndex + toIndex) / 2;
            if (isSquareUnderAttack(fromIndex, !isWhite) ||
                    isSquareUnderAttack(middleSquare, !isWhite) ||
                    isSquareUnderAttack(toIndex, !isWhite)) {
                System.out.println("❌ Castling not allowed! King passes through or lands on an attacked square.");
                return false;
            }
            // Perform castling move
            setPiece(rookTo, actualRook);
            setPiece(rookFrom, PieceConstants.NONE);
            setPiece(toIndex, piece);
            setPiece(fromIndex, PieceConstants.NONE);
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

        // ----------------------
        // Check for en passant
        // ----------------------
        Integer epTarget = getEnPassantTarget();
        boolean isEnPassant = false;
        if ((piece & 7) == PieceConstants.PAWN && epTarget != null && epTarget == toIndex) {
            isEnPassant = true;
        }

        // Execute normal move
        setPiece(toIndex, piece);
        setPiece(fromIndex, PieceConstants.NONE);

        // Handle en passant capture
        if (isEnPassant) {
            int capturedPawnIndex = isWhite ? (toIndex - 8) : (toIndex + 8);
            int epPawn = getPiece(capturedPawnIndex);
            lastCapturedPiece = epPawn;
            setPiece(capturedPawnIndex, PieceConstants.NONE);
            isCapture = true;
        }

        // If a normal capture occurred, store the captured piece
        if (!isEnPassant && isCapture) {
            lastCapturedPiece = targetPiece;
        }

        // Mark king/rook as having moved if applicable
        if ((piece & 7) == PieceConstants.KING) {
            if (isWhite) whiteKingMoved = true;
            else blackKingMoved = true;
        } else if ((piece & 7) == PieceConstants.ROOK) {
            switch (fromIndex) {
                case 7 -> whiteKingsideRookMoved = true;
                case 0 -> whiteQueensideRookMoved = true;
                case 63 -> blackKingsideRookMoved = true;
                case 56 -> blackQueensideRookMoved = true;
                default -> { }
            }
        }

        // Pawn promotion check (GUI will handle promotion popup)
        int promotionRank = isWhite ? 7 : 0;
        boolean isPromotion = ((piece & 7) == PieceConstants.PAWN) && ((toIndex / 8) == promotionRank);
        if (isPromotion) {
            // Do nothing here to let GUI handle promotion popup
        } else if (isCapture) {
            SoundManager.playCaptureSound();
        } else {
            SoundManager.playMoveSound();
        }

        // Update last move info and en passant target reset/increment
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

    // ---------------------------------------------------------
    // 6) Checkmate detection
    // ---------------------------------------------------------
    public boolean isCheckmate(boolean isWhite) {
        // If the king is not in check, it's not checkmate.
        if (!isKingInCheck(isWhite)) return false;

        // For every piece belonging to the side, if at least one legal move exists, it's not checkmate.
        for (int i = 0; i < 64; i++) {
            int p = getPiece(i);
            if (p == PieceConstants.NONE) continue;
            boolean pieceIsWhite = ((p & PieceConstants.WHITE) != 0);
            if (pieceIsWhite != isWhite) continue;

            List<Integer> moves = getValidMoves(i);
            if (!moves.isEmpty()) {
                return false;
            }
        }
        return true; // King is in check and no legal moves exist.
    }

    // ---------------------------------------------------------
    // 6b) Stalemate detection
    // ---------------------------------------------------------
    public boolean isStalemate(boolean isWhite) {
        // If the king is in check, then it's not stalemate.
        if (isKingInCheck(isWhite)) return false;

        // For every piece belonging to the side, if at least one legal move exists, it's not stalemate.
        for (int i = 0; i < 64; i++) {
            int p = getPiece(i);
            if (p == PieceConstants.NONE) continue;
            boolean pieceIsWhite = ((p & PieceConstants.WHITE) != 0);
            if (pieceIsWhite != isWhite) continue;

            List<Integer> moves = getValidMoves(i);
            if (!moves.isEmpty()) {
                return false;
            }
        }
        return true; // Not in check and no legal moves exist.
    }

    // ---------------------------------------------------------
    // 7) Methods for castling flags (for check detection and GUI)
    // ---------------------------------------------------------
    public boolean hasKingMoved(boolean isWhite) {
        return isWhite ? whiteKingMoved : blackKingMoved;
    }

    public boolean hasRookMoved(int rookIndex) {
        return switch (rookIndex) {
            case 0 -> whiteQueensideRookMoved;
            case 7 -> whiteKingsideRookMoved;
            case 56 -> blackQueensideRookMoved;
            case 63 -> blackKingsideRookMoved;
            default -> true;
        };
    }
}
