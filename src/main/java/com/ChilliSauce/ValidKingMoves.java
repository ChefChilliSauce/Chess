package com.ChilliSauce;

class ValidKingMoves extends MoveValidator {
    @Override
    public boolean isValidMove(Board board, char fromFile, int fromRank, char toFile, int toRank) {
        Piece piece = board.getPiece(fromFile, fromRank);
        if (!(piece instanceof King)) return false;

        int fileDiff = Math.abs(toFile - fromFile);
        int rankDiff = Math.abs(toRank - fromRank);
        Piece target = board.getPiece(toFile, toRank);

        // Normal King movement
        return fileDiff <= 1 && rankDiff <= 1 && (target == null || !target.getColor().equals(piece.getColor()));
    }
}

class GameLogic {
    public static boolean isKingInCheck(Board board, String color) {
        char kingFile = '-';
        int kingRank = -1;

        for (char file = 'a'; file <= 'h'; file++) {
            for (int rank = 1; rank <= 8; rank++) {
                Piece piece = board.getPiece(file, rank);
                if (piece instanceof King && piece.getColor().equals(color)) {
                    kingFile = file;
                    kingRank = rank;
                    break;
                }
            }
        }

        if (kingFile == '-' || kingRank == -1) return false;

        for (char file = 'a'; file <= 'h'; file++) {
            for (int rank = 1; rank <= 8; rank++) {
                Piece piece = board.getPiece(file, rank);
                if (piece != null && !piece.getColor().equals(color)) {
                    MoveValidator validator = board.getMoveValidator(piece);
                    if (validator != null && validator.isValidMove(board, file, rank, kingFile, kingRank)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isCheckmate(Board board, String color) {
        if (!isKingInCheck(board, color)) return false;

        for (char file = 'a'; file <= 'h'; file++) {
            for (int rank = 1; rank <= 8; rank++) {
                Piece piece = board.getPiece(file, rank);
                if (piece != null && piece.getColor().equals(color)) {
                    for (char toFile = 'a'; toFile <= 'h'; toFile++) {
                        for (int toRank = 1; toRank <= 8; toRank++) {
                            MoveValidator validator = board.getMoveValidator(piece);
                            if (validator != null && validator.isValidMove(board, file, rank, toFile, toRank)) {
                                Piece capturedPiece = board.getPiece(toFile, toRank);
                                board.setPiece(toFile, toRank, piece);
                                board.setPiece(file, rank, null);
                                boolean stillInCheck = isKingInCheck(board, color);
                                board.setPiece(file, rank, piece);
                                board.setPiece(toFile, toRank, capturedPiece);
                                if (!stillInCheck) return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
