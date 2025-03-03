package com.ChilliSauce;

import java.util.Scanner;

public class ValidMoves {

    public static boolean isValidMove(Piece piece, int startX, int startY, int endX, int endY, Board board) {
        if (piece == null) return false;
        return switch (piece) {
            case Pawn pawn -> isValidPawnMove(pawn, startX, startY, endX, endY, board);
            case Rook rook -> isValidRookMove(rook, startX, startY, endX, endY, board);
            case Knight knight -> isValidKnightMove(knight, startX, startY, endX, endY);
            case Bishop bishop -> isValidBishopMove(bishop, startX, startY, endX, endY, board);
            case Queen queen -> isValidQueenMove(queen, startX, startY, endX, endY, board);
            case King king -> isValidKingMove(king, startX, startY, endX, endY, board);
            default -> false;
        };

    }

    private static boolean isValidPawnMove(Pawn pawn, int startX, int startY, int endX, int endY, Board board) {
        int direction = pawn.getColor().equals("white") ? -1 : 1;

        char startFile = (char) ('a' + startY);
        int startRank = 8 - startX;
        char endFile = (char) ('a' + endY);
        int endRank = 8 - endX;

        // Standard move (1 square forward)
        if (endX == startX + direction && endY == startY && board.getPiece(endFile, endRank) == null) {
            return true;
        }

        // First move (2 squares forward)
        if ((startRank == 2 && pawn.getColor().equals("white")) || (startRank == 7 && pawn.getColor().equals("black"))) {
            if (endX == startX + 2 * direction && endY == startY && board.getPiece(endFile, endRank) == null) {
                // Store en passant possibility
                board.setEnPassantTarget(endFile, endRank - direction); // Mark the square behind the pawn
                return true;
            }
        }

        // Capture (Diagonal)
        if (endX == startX + direction && Math.abs(endY - startY) == 1) {
            Piece target = board.getPiece(endFile, endRank);
            if (target != null && !target.getColor().equals(pawn.getColor())) {
                return true; // Regular capture
            }

            // En Passant Check
            if (board.getEnPassantFile() == endFile && board.getEnPassantRank() == endRank) {
                board.setPiece(endFile, endRank, pawn); // Move pawn
                board.setPiece(endFile, startRank, null); // Remove captured pawn
                board.resetEnPassant(); // Clear En Passant after capture
                return true;
            }

        }

        // Check for promotion
        if ((endRank == 8 && pawn.getColor().equals("white")) || (endRank == 1 && pawn.getColor().equals("black"))) {
            promotePawn(pawn, endFile, endRank, board);
            return true;
        }

        return false;
    }

    private static void promotePawn(Pawn pawn, char file, int rank, Board board) {
        System.out.println("Pawn Promotion! Choose a piece (Q, R, B, N):");
        Scanner scanner = new Scanner(System.in);
        char choice = scanner.next().toUpperCase().charAt(0);

        Piece newPiece = switch (choice) {
            case 'Q' -> new Queen(pawn.getColor());
            case 'R' -> new Rook(pawn.getColor());
            case 'B' -> new Bishop(pawn.getColor());
            case 'N' -> new Knight(pawn.getColor());
            default -> new Queen(pawn.getColor()); // Default to Queen
        };

        board.setPiece(file, rank, newPiece);
        System.out.println("Pawn promoted to " + newPiece.getClass().getSimpleName());
    }



    private static boolean isValidKnightMove(Knight knight, int startX, int startY, int endX, int endY) {
        int dx = Math.abs(startX - endX);
        int dy = Math.abs(startY - endY);
        return (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
    }

    private static boolean isValidRookMove(Rook rook, int startX, int startY, int endX, int endY, Board board) {
        return (startX == endX || startY == endY); // Rook moves in straight lines
    }

    private static boolean isValidBishopMove(Bishop bishop, int startX, int startY, int endX, int endY, Board board) {
        return Math.abs(startX - endX) == Math.abs(startY - endY); // Diagonal movement
    }

    private static boolean isValidQueenMove(Queen queen, int startX, int startY, int endX, int endY, Board board) {
        return isValidRookMove(null, startX, startY, endX, endY, board) || isValidBishopMove(null, startX, startY, endX, endY, board);
    }

    private static boolean isValidKingMove(King king, int startX, int startY, int endX, int endY, Board board) {
        return Math.abs(startX - endX) <= 1 && Math.abs(startY - endY) <= 1;
    }
}
