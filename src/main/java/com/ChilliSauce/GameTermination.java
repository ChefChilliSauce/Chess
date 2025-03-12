package com.ChilliSauce;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all possible game termination scenarios (win/lose/draw).
 */
public class GameTermination {

    /**
     * High-level game outcome.
     */
    public enum GameState {
        ONGOING,
        WHITE_WINS,
        BLACK_WINS,
        DRAW
    }

    /**
     * Optional detail for draws (why was it a draw?).
     */
    public enum DrawReason {
        NONE,              // Not a draw
        STALEMATE,
        INSUFFICIENT_MATERIAL,
        FIFTY_MOVE_RULE,
        THREEFOLD_REPETITION,
        AGREEMENT
    }

    /**
     * Simple container for the result + an optional draw reason.
     */
    public static class GameResult {
        public final GameState state;
        public final DrawReason drawReason;

        public GameResult(GameState state, DrawReason reason) {
            this.state = state;
            this.drawReason = reason;
        }

        @Override
        public String toString() {
            if (state == GameState.DRAW) {
                return "Draw (" + drawReason + ")";
            }
            return state.toString();
        }
    }

    // ----------------------------------------------------------------
    // Tracking extra data needed for certain draw/termination rules
    // ----------------------------------------------------------------

    // Example: halfmove clock for the 50-move rule
    private int halfmoveClock = 0;

    // Example: store positions to detect 3-fold repetition
    // The key could be a FEN string or something that identifies the position + side to move
    private final Map<String, Integer> positionCount = new HashMap<>();

    // If either side resigns or agrees to a draw, store it here:
    private boolean whiteResigned = false;
    private boolean blackResigned = false;
    private boolean drawOfferedAndAccepted = false;

    // Example placeholder for time control
    private boolean whiteTimeout = false;
    private boolean blackTimeout = false;

    // ----------------------------------------------------------------
    // Public “setter” methods to update states like resignation, draw
    // ----------------------------------------------------------------

    public void setWhiteResigned(boolean resigned) {
        this.whiteResigned = resigned;
    }

    public void setBlackResigned(boolean resigned) {
        this.blackResigned = resigned;
    }

    public void setDrawAgreed(boolean agreed) {
        this.drawOfferedAndAccepted = agreed;
    }

    public void setWhiteTimeout(boolean timeout) {
        this.whiteTimeout = timeout;
    }

    public void setBlackTimeout(boolean timeout) {
        this.blackTimeout = timeout;
    }

    /**
     * Call this whenever a new move is made:
     * - If the move was a capture or a pawn move, reset halfmove clock.
     * - Otherwise, increment it.
     * - Update the repetition map with the new position.
     */
    public void onMoveMade(Board board, boolean wasCaptureOrPawnMove) {
        if (wasCaptureOrPawnMove) {
            halfmoveClock = 0;
        } else {
            halfmoveClock++;
        }

        // Build a “position key” (like a FEN) that includes side to move, castling rights, etc.
        String positionKey = buildPositionKey(board);

        positionCount.put(positionKey, positionCount.getOrDefault(positionKey, 0) + 1);
    }

    // ----------------------------------------------------------------
    // Main method to check if the game is over
    // ----------------------------------------------------------------
    public GameResult checkGameState(Board board) {
        boolean isWhiteToMove = board.isWhiteTurn();  // Whose turn is it?

        // 1) Check if either side resigned
        if (whiteResigned) {
            // White resigned => black wins
            return new GameResult(GameState.BLACK_WINS, DrawReason.NONE);
        }
        if (blackResigned) {
            // Black resigned => white wins
            return new GameResult(GameState.WHITE_WINS, DrawReason.NONE);
        }

        // 2) Check if time ran out (placeholder logic)
        if (whiteTimeout) {
            // White's clock hit zero => black wins
            return new GameResult(GameState.BLACK_WINS, DrawReason.NONE);
        }
        if (blackTimeout) {
            // Black's clock hit zero => white wins
            return new GameResult(GameState.WHITE_WINS, DrawReason.NONE);
        }

        // 3) Check for checkmate
        if (isCheckmate(board, isWhiteToMove)) {
            // If it's white's turn and white is checkmated => black wins, etc.
            return isWhiteToMove
                    ? new GameResult(GameState.BLACK_WINS, DrawReason.NONE)
                    : new GameResult(GameState.WHITE_WINS, DrawReason.NONE);
        }

        // 4) Check for stalemate
        if (isStalemate(board, isWhiteToMove)) {
            return new GameResult(GameState.DRAW, DrawReason.STALEMATE);
        }

        // 5) Check for insufficient material
        if (isInsufficientMaterial(board)) {
            return new GameResult(GameState.DRAW, DrawReason.INSUFFICIENT_MATERIAL);
        }

        // 6) Check 50-move rule
        if (halfmoveClock >= 50) {
            return new GameResult(GameState.DRAW, DrawReason.FIFTY_MOVE_RULE);
        }

        // 7) Check for threefold repetition
        if (isThreefoldRepetition()) {
            return new GameResult(GameState.DRAW, DrawReason.THREEFOLD_REPETITION);
        }

        // 8) Check if players have agreed to a draw
        if (drawOfferedAndAccepted) {
            return new GameResult(GameState.DRAW, DrawReason.AGREEMENT);
        }

        // Otherwise, the game continues
        return new GameResult(GameState.ONGOING, DrawReason.NONE);
    }

    // ----------------------------------------------------------------
    // Helper checks (PLACEHOLDERS) – fill in real logic yourself
    // ----------------------------------------------------------------

    private boolean isCheckmate(Board board, boolean whiteToMove) {
        // PSEUDOCODE:
        // 1. If the side to move is not in check => false (can't be checkmate if you're not in check).
        // 2. Generate all legal moves for that side. If none exist => checkmate, else not.
        boolean inCheck = isInCheck(board, whiteToMove);
        if (!inCheck) return false;
        return !hasAnyLegalMove(board, whiteToMove);
    }

    private boolean isStalemate(Board board, boolean whiteToMove) {
        // PSEUDOCODE:
        // 1. If the side to move is in check => false.
        // 2. If that side has no legal moves => stalemate, else not.
        boolean inCheck = isInCheck(board, whiteToMove);
        if (inCheck) return false;
        return !hasAnyLegalMove(board, whiteToMove);
    }

    private boolean isInsufficientMaterial(Board board) {
        // PSEUDOCODE:
        // Count material for each side. If only kings remain => true
        // If one side has only king + bishop or king + knight and the other side has only king => true
        // If both sides have only king + bishop (same color bishop) => also a known draw, etc.
        // ...
        return false; // placeholder
    }

    private boolean isThreefoldRepetition() {
        // If any position key in positionCount has occurred 3 or more times => true
        for (Integer count : positionCount.values()) {
            if (count >= 3) {
                return true;
            }
        }
        return false;
    }

    private boolean isInCheck(Board board, boolean whiteToMove) {
        // PSEUDOCODE:
        // 1. Find the king of the side to move
        // 2. Check if that king's square is under attack by the opponent
        // ...
        return false; // placeholder
    }

    private boolean hasAnyLegalMove(Board board, boolean whiteToMove) {
        // PSEUDOCODE:
        // 1. For each square that has a piece of the side to move
        // 2. Generate valid moves
        // 3. If at least one valid move => true
        // ...
        return false; // placeholder
    }

    /**
     * Build a unique string that identifies the current position (like FEN).
     * Must include side to move, castling rights, en passant square, etc.
     */
    private String buildPositionKey(Board board) {
        // For a fully robust approach, you'd create a FEN string or
        // something similar. For a placeholder:
        return "FEN_or_hash_of_position_" + (board.isWhiteTurn() ? "w" : "b");
    }

}
