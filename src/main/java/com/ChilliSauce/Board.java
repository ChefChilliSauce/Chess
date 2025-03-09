package com.ChilliSauce;

import java.util.HashMap;
import java.util.Map;

public class Board {
    private final Piece[][] board;
    private final Map<String, MoveValidator> moveValidators;

    public Board() {
        board = new Piece[8][8];
        moveValidators = new HashMap<>();
        initializeMoveValidators();
        setupBoard();
    }

    private void initializeMoveValidators() {
        moveValidators.put("Pawn", new ValidPawnMoves());
        moveValidators.put("Rook", new ValidRookMoves());
        moveValidators.put("Knight", new ValidKnightMoves());
        moveValidators.put("Bishop", new ValidBishopMoves());
        moveValidators.put("Queen", new ValidQueenMoves());
        moveValidators.put("King", new ValidKingMoves());
    }

    private void setupBoard() {
        // Place pawns
        for (char file = 'a'; file <= 'h'; file++) {
            board[rankToIndex(2)][fileToIndex(file)] = new Pawn("white");
            board[rankToIndex(7)][fileToIndex(file)] = new Pawn("black");
        }

        // Place rooks
        placePiece('a', 1, new Rook("white"));
        placePiece('h', 1, new Rook("white"));
        placePiece('a', 8, new Rook("black"));
        placePiece('h', 8, new Rook("black"));

        // Place knights
        placePiece('b', 1, new Knight("white"));
        placePiece('g', 1, new Knight("white"));
        placePiece('b', 8, new Knight("black"));
        placePiece('g', 8, new Knight("black"));

        // Place bishops
        placePiece('c', 1, new Bishop("white"));
        placePiece('f', 1, new Bishop("white"));
        placePiece('c', 8, new Bishop("black"));
        placePiece('f', 8, new Bishop("black"));

        // Place queens
        placePiece('d', 1, new Queen("white"));
        placePiece('d', 8, new Queen("black"));

        // Place kings
        placePiece('e', 1, new King("white"));
        placePiece('e', 8, new King("black"));
    }

    private void placePiece(char file, int rank, Piece piece) {
        board[rankToIndex(rank)][fileToIndex(file)] = piece;
    }

    public Piece getPiece(char file, int rank) {
        int x = rankToIndex(rank);
        int y = fileToIndex(file);
        return (x >= 0 && x < 8 && y >= 0 && y < 8) ? board[x][y] : null;
    }

    public void setPiece(char file, int rank, Piece piece) {
        int x = rankToIndex(rank);
        int y = fileToIndex(file);
        if (x >= 0 && x < 8 && y >= 0 && y < 8) {
            board[x][y] = piece;
        }
    }

    public boolean movePiece(char fromFile, int fromRank, char toFile, int toRank, TurnManager turnManager) {
        Piece piece = getPiece(fromFile, fromRank);
        if (piece == null) return false;

        MoveValidator validator = moveValidators.get(piece.getClass().getSimpleName());
        if (validator == null) return false;

        Piece capturedPiece = getPiece(toFile, toRank); // ✅ Check BEFORE moving

        if (validator.isValidMove(this, fromFile, fromRank, toFile, toRank)) {
            setPiece(toFile, toRank, piece);  // Move piece
            setPiece(fromFile, fromRank, null); // Clear old position

            if (isKingInCheck(piece.getColor())) {
                // Undo move if King is in check
                setPiece(fromFile, fromRank, piece);
                setPiece(toFile, toRank, capturedPiece);
                System.out.println("Move blocked: King would be in check!");
                return false;
            }

            turnManager.switchTurn();
            return true;
        }
        return false;
    }

    public void printBoard() {
        System.out.println("  a b c d e f g h");
        System.out.println("  ----------------");
        for (int rank = 8; rank >= 1; rank--) {
            System.out.print(rank + "| ");
            for (char file = 'a'; file <= 'h'; file++) {
                Piece piece = getPiece(file, rank);
                if (piece != null) {
                    System.out.print(piece.getColor().charAt(0) + " "); // Display first letter of color
                } else {
                    System.out.print(". "); // Empty square
                }
            }
            System.out.println("|" + rank);
        }
        System.out.println("  ----------------");
        System.out.println("  a b c d e f g h");
    }

    public static int fileToIndex(char file) {
        return file - 'a';
    }

    public static int rankToIndex(int rank) {
        return 8 - rank;
    }

    public MoveValidator getMoveValidator(Piece piece) {
        return moveValidators.get(piece.getClass().getSimpleName());
    }
    public boolean isKingInCheck(String color) {
        // 1. Find the King's position
        char kingFile = '-';
        int kingRank = -1;

        for (char file = 'a'; file <= 'h'; file++) {
            for (int rank = 1; rank <= 8; rank++) {
                Piece piece = getPiece(file, rank);
                if (piece instanceof King && piece.getColor().equals(color)) {
                    kingFile = file;
                    kingRank = rank;
                    break;
                }
            }
        }

        // If the King was not found, return false (should never happen)
        if (kingFile == '-' || kingRank == -1) return false;

        // 2. Check if any opponent piece can attack the King's position
        for (char file = 'a'; file <= 'h'; file++) {
            for (int rank = 1; rank <= 8; rank++) {
                Piece piece = getPiece(file, rank);
                if (piece != null && !piece.getColor().equals(color)) {
                    MoveValidator validator = getMoveValidator(piece);
                    if (validator != null && validator.isValidMove(this, file, rank, kingFile, kingRank)) {
                        return true; // King is in check
                    }
                }
            }
        }

        return false; // King is not in check
    }

}