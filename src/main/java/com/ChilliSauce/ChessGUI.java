package com.ChilliSauce;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ChessGUI extends JFrame {
    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = TILE_SIZE * 8;

    private final JTable moveHistoryTable;
    private final DefaultTableModel moveTableModel;
    private final Board board;
    private int selectedPieceIndex = -1;
    private int draggedX, draggedY;
    private boolean dragging = false;
    private final JPanel chessBoardPanel;
    private int fromIndex = -1;
    private int toIndex = -1;
    private List<Integer> validMoves = new ArrayList<>();

    public ChessGUI(Board board) {
        this.board = board;
        setTitle("Chess Board UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setLayout(null);

        chessBoardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawPieces(g);
            }
        };

        chessBoardPanel.setBounds(100, 100, BOARD_SIZE, BOARD_SIZE);
        chessBoardPanel.setLayout(null);
        chessBoardPanel.setBackground(Color.LIGHT_GRAY);

        chessBoardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = e.getX() / TILE_SIZE;
                int row = 7 - (e.getY() / TILE_SIZE);
                selectedPieceIndex = row * 8 + col;

                if (board.getPiece(selectedPieceIndex) != PieceConstants.NONE) {
                    dragging = true;
                    draggedX = e.getX();
                    draggedY = e.getY();
                    validMoves = board.getValidMoves(selectedPieceIndex);
                } else {
                    validMoves.clear();
                }
                repaintBoard();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!dragging || selectedPieceIndex == -1) return;

                int targetCol = e.getX() / TILE_SIZE;
                int targetRow = 7 - (e.getY() / TILE_SIZE);
                int targetIndex = targetRow * 8 + targetCol;

                int piece = board.getPiece(selectedPieceIndex);
                if (piece == PieceConstants.NONE) return;

                boolean moveSuccessful = board.makeMove(selectedPieceIndex, targetIndex, ChessGUI.this);

                if (moveSuccessful) {
                    fromIndex = selectedPieceIndex;
                    toIndex = targetIndex;

                    if ((piece & 7) == PieceConstants.PAWN) {
                        boolean isWhite = (piece & PieceConstants.WHITE) != 0;
                        int lastRank = isWhite ? 7 : 0;

                        if (targetIndex / 8 == lastRank) {
                            showPromotionPopup(targetIndex, isWhite);
                        }
                    }
                    repaintBoard();
                }

                validMoves.clear();
                selectedPieceIndex = -1;
                dragging = false;
                repaintBoard();
            }
        });

        chessBoardPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    draggedX = e.getX();
                    draggedY = e.getY();
                    chessBoardPanel.repaint();
                }
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
        getContentPane().add(chessBoardPanel);
        getContentPane().add(moveScrollPane);
        setVisible(true);
    }

    public void repaintBoard() {
        SwingUtilities.invokeLater(() -> chessBoardPanel.repaint());
    }

    private void showPromotionPopup(int index, boolean isWhite) {
        JDialog promotionDialog = new JDialog(this, "Choose Promotion Piece", true);
        promotionDialog.setLayout(new GridLayout(1, 4));
        promotionDialog.setUndecorated(true);

        String colorPrefix = isWhite ? "w" : "b";
        String[] pieceNames = {"q", "r", "b", "n"};
        int[] pieceTypes = {PieceConstants.QUEEN, PieceConstants.ROOK, PieceConstants.BISHOP, PieceConstants.KNIGHT};

        for (int i = 0; i < 4; i++) {
            String imagePath = "/assets/" + colorPrefix + pieceNames[i] + ".png";
            java.net.URL imgURL = getClass().getResource(imagePath);

            if (imgURL == null) {
                System.err.println("⚠ ERROR: Image not found: " + imagePath);
                continue;
            }

            ImageIcon originalIcon = new ImageIcon(imgURL);
            Image scaledImage = originalIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(scaledImage);

            JButton button = new JButton(resizedIcon);
            button.setPreferredSize(new Dimension(80, 80));

            final int selectedPiece = pieceTypes[i] | (isWhite ? PieceConstants.WHITE : PieceConstants.BLACK);

            button.addActionListener(e -> {
                board.setPiece(index, selectedPiece);
                promotionDialog.dispose();
                SoundManager.playPromotionSound();
                repaintBoard();
            });

            promotionDialog.add(button);
        }

        promotionDialog.pack();
        promotionDialog.setLocationRelativeTo(this);
        promotionDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        promotionDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        promotionDialog.setVisible(true);
    }

    private void drawBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int index = (7 - row) * 8 + col;
                boolean isLastMove = (index == fromIndex || index == toIndex);
                boolean isValidMove = validMoves.contains(index);

                g2d.setColor((row + col) % 2 == 0 ? Color.decode("#ebecd0") : Color.decode("#6e9552"));
                g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                if (isLastMove) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

                if (isValidMove) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

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
                    g.drawImage(pieceImage, x, y, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }
    }
public static void main(String[] args) {
        Board board = new Board();
        SwingUtilities.invokeLater(() -> new ChessGUI(board));
    }
}
