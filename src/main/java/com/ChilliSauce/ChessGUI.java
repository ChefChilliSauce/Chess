package com.ChilliSauce;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.util.ArrayList;
import java.util.List;

public class ChessGUI extends JFrame {
    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = TILE_SIZE * 8;
    private final JTable moveHistoryTable;
    private DefaultTableModel moveTableModel;
    private final Board board;
    private int selectedPieceIndex = -1;
    private int draggedX, draggedY;
    private boolean dragging = false;

    // Track last move for highlighting
    private int fromIndex = -1;
    private int toIndex = -1;

    // Store valid moves for highlighting
    private List<Integer> validMoves = new ArrayList<>();

    public ChessGUI(Board board) {
        this.board = board;
        setTitle("Chess Board UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawPieces(g);
            }
        };

        panel.setBounds(100, 100, BOARD_SIZE, BOARD_SIZE);
        panel.setLayout(null);
        panel.setBackground(Color.LIGHT_GRAY);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = e.getX() / TILE_SIZE;
                int row = 7 - (e.getY() / TILE_SIZE);
                selectedPieceIndex = row * 8 + col;

                System.out.println("Selected index: " + selectedPieceIndex);

                if (board.getPiece(selectedPieceIndex) != PieceConstants.NONE) {
                    dragging = true;
                    draggedX = e.getX();
                    draggedY = e.getY();

                    // Get valid moves for highlighting
                    validMoves = board.getValidMoves(selectedPieceIndex);
                } else {
                    validMoves.clear(); // Clear highlights if no piece is selected
                }
                panel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!dragging || selectedPieceIndex == -1) return;

                int targetCol = e.getX() / TILE_SIZE;
                int targetRow = 7 - (e.getY() / TILE_SIZE);
                int targetIndex = targetRow * 8 + targetCol;

                System.out.println("Target index: " + targetIndex);

                // ✅ Let Board handle move logic
                boolean moveSuccessful = board.makeMove(selectedPieceIndex, targetIndex);

                if (moveSuccessful) {
                    fromIndex = selectedPieceIndex;
                    toIndex = targetIndex;
                    System.out.println("Move registered: " + fromIndex + " -> " + toIndex);
                } else {
                    System.out.println("❌ Invalid move! Check turn and rules.");
                }

                validMoves.clear();
                selectedPieceIndex = -1;
                dragging = false;
                panel.repaint();
            }

        });

        moveTableModel = new DefaultTableModel(new String[]{"Move No.", "White", "Black"}, 0);
        moveHistoryTable = new JTable(moveTableModel);
        moveHistoryTable.setBounds(1000, 120, 400, 500);
        moveHistoryTable.setShowGrid(false);
        moveHistoryTable.setRowHeight(30);

        JTableHeader tableHeader = moveHistoryTable.getTableHeader();
        tableHeader.setVisible(false);
        tableHeader.setPreferredSize(new Dimension(0, 0));

        moveHistoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane moveScrollPane = new JScrollPane(moveHistoryTable);
        moveScrollPane.setBounds(1000, 120, 400, 500);

        getContentPane().add(panel);
        getContentPane().add(moveScrollPane);

        setVisible(true);
    }

    private void drawBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g; // Convert to Graphics2D for transparency

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int index = (7 - row) * 8 + col;
                boolean isLastMove = (index == fromIndex || index == toIndex);
                boolean isValidMove = validMoves.contains(index);

                // Step 1: Draw the base tile color (light/dark squares)
                g2d.setColor((row + col) % 2 == 0 ? Color.decode("#ebecd0") : Color.decode("#6e9552"));
                g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                // Step 2: Apply translucent yellow for the last move
                if (isLastMove) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // 50% transparency
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

                // Step 3: Apply translucent blue for valid moves (if also last move, it'll blend with yellow)
                if (isValidMove) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f)); // 40% transparency
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

                // Step 4: Reset transparency back to default
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
    }



    private void drawPieces(Graphics g) {
        for (int index = 0; index < 64; index++) {
            int piece = board.getPiece(index);
            if (piece != PieceConstants.NONE) {
                Image pieceImage = PieceConstants.getPieceImage(piece);
                if (pieceImage != null) {
                    int x = (index % 8) * TILE_SIZE;
                    int y = (7 - (index / 8)) * TILE_SIZE;

                    if (dragging && index == selectedPieceIndex) {
                        g.drawImage(pieceImage, draggedX - TILE_SIZE / 2, draggedY - TILE_SIZE / 2, TILE_SIZE, TILE_SIZE, this);
                    } else {
                        g.drawImage(pieceImage, x, y, TILE_SIZE, TILE_SIZE, this);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Board board = new Board();
        SwingUtilities.invokeLater(() -> new ChessGUI(board));
    }
}
