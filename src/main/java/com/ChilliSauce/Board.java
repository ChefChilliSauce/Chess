package com.ChilliSauce;

public class Board {
    private Piece[][] board;
    private char enPassantFile = '-'; // Default: No en passant
    private int enPassantRank = -1;  // Default: No en passant

    public Board() {
        board = new Piece[8][8];
        setupBoard();
    }

    private void setupBoard() {
        // Place pawns
        for (char file = 'a'; file <= 'h'; file++) {
            board[rankToIndex(2)][fileToIndex(file)] = new Pawn("white"); // White Pawns a2-h2
            board[rankToIndex(7)][fileToIndex(file)] = new Pawn("black"); // Black Pawns a7-h7
        }
        // Place rooks
        board[rankToIndex(1)][fileToIndex('a')] = new Rook("white"); //white rook a1
        board[rankToIndex(1)][fileToIndex('h')] = new Rook("white"); //white rook h1
        board[rankToIndex(8)][fileToIndex('a')] = new Rook("black"); //black rook a8
        board[rankToIndex(8)][fileToIndex('h')] = new Rook("black"); //black rook h8

        //Place Knight
        board[rankToIndex(1)][fileToIndex('b')] = new Knight("white"); //white Knight b1
        board[rankToIndex(1)][fileToIndex('g')] = new Knight("white"); //white Knight g1
        board[rankToIndex(8)][fileToIndex('b')] = new Knight("black"); //black Knight b8
        board[rankToIndex(8)][fileToIndex('g')] = new Knight("black"); //black Knight g8


        //Place Bishop
        board[rankToIndex(1)][fileToIndex('c')] = new Bishop("white"); //white Bishop c1
        board[rankToIndex(1)][fileToIndex('f')] = new Bishop("white"); //white Bishop f1
        board[rankToIndex(8)][fileToIndex('c')] = new Bishop("black"); //black Bishop c8
        board[rankToIndex(8)][fileToIndex('f')] = new Bishop("black"); //black Bishop f8


        //Place Queen
        board[rankToIndex(1)][fileToIndex('d')] = new Queen("white"); //white Queen d1
        board[rankToIndex(8)][fileToIndex('d')] = new Queen("black"); //black Queen d8

        //Place Kind
        board[rankToIndex(1)][fileToIndex('e')] = new King("white"); //white King e1
        board[rankToIndex(8)][fileToIndex('e')] = new King("black"); //black King e8
    }

    public Piece getPiece(char file, int rank) {
        int x = rankToIndex(rank);
        int y = fileToIndex(file);

        if (x >= 0 && x < 8 && y >= 0 && y < 8) {
            return board[x][y];
        }
        return null; // Out of bounds
    }

    public void setPiece(char file, int rank, Piece piece) {
        int x = rankToIndex(rank);
        int y = fileToIndex(file);

        if (x >= 0 && x < 8 && y >= 0 && y < 8) {
            board[x][y] = piece;
        }
    }

    public void setEnPassantTarget(char file, int rank) {
        enPassantFile = file;
        enPassantRank = rank;
    }

    public char getEnPassantFile() {
        return enPassantFile;
    }

    public int getEnPassantRank() {
        return enPassantRank;
    }

    public void resetEnPassant() {
        enPassantFile = '-';
        enPassantRank = -1;
    }

    public void printBoard() {
        System.out.println("  a b c d e f g h");
        System.out.println("  ----------------");
        for (int rank = 8; rank >= 1; rank--) {
            System.out.print(rank + "| ");
            for (char file = 'a'; file <= 'h'; file++) {
                Piece piece = getPiece(file, rank);
                System.out.print((piece != null ? piece.getSymbol() : ".") + " ");
            }
            System.out.println("|" + rank);
        }
        System.out.println("  ----------------");
        System.out.println("  a b c d e f g h");
    }


    public static int fileToIndex(char file) {
        return file - 'a';  // Converts 'a'-'h' to 0-7
    }

    public static int rankToIndex(int rank) {
        return 8 - rank;  // Converts chess rank 1-8 to array index 7-0
    }

}
