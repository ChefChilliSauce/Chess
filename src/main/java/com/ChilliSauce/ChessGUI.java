package com.ChilliSauce;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChessGUI extends JFrame {
    private final int TILE_SIZE = 80;
    private final Board board;
    private char selectedFile = '-';
    private int selectedRank = -1;

    public ChessGUI(Board board) {
        this.board = board;
        setTitle("Chess Game");
        setSize(TILE_SIZE * 8 + 16, TILE_SIZE * 8 + 39); // Adjusted for window borders
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = e.getY() / TILE_SIZE;
                int col = e.getX() / TILE_SIZE;
                handleClick(row, col);
            }
        });
    }

    private void handleClick(int row, int col) {
        char file = (char) ('a' + col);
        int rank = 8 - row;
        Piece clickedPiece = board.getPiece(file, rank);

        if (selectedFile == '-' && clickedPiece != null) {
            selectedFile = file;
            selectedRank = rank;
        } else if (selectedFile != '-' && selectedRank != -1) {
            board.setPiece(file, rank, board.getPiece(selectedFile, selectedRank));
            board.setPiece(selectedFile, selectedRank, null);
            selectedFile = '-';
            selectedRank = -1;
            repaint(); // 🔹 Ensure the board updates visually
        }
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        drawBoard(g);
        drawPieces(g);
    }

    private void drawBoard(Graphics g) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 0) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.GRAY);
                }
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawPieces(Graphics g) {
        g.setFont(new Font("SansSerif", Font.BOLD, TILE_SIZE - 20));
        FontMetrics metrics = g.getFontMetrics();

        for (char file = 'a'; file <= 'h'; file++) {
            for (int rank = 1; rank <= 8; rank++) {
                Piece piece = board.getPiece(file, rank);
                if (piece != null) {
                    int x = (file - 'a') * TILE_SIZE + (TILE_SIZE - metrics.stringWidth(piece.getSymbol())) / 2;
                    int y = (8 - rank) * TILE_SIZE + (TILE_SIZE + metrics.getAscent()) / 2 - 5;
                    g.setColor(piece.getColor().equals("white") ? Color.BLACK : Color.RED);
                    g.drawString(piece.getSymbol(), x, y);
                }
            }
        }
    }


    public static void main(String[] args) {
        Board board = new Board();
        ChessGUI gui = new ChessGUI(board);
        gui.setVisible(true);
    }
}
