package com.ChilliSauce;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
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

    // For tracking move number and alternating moves
    private int moveNumber = 1;
    private boolean waitingForBlackMove = false;

    // NEW: Track which side is at the bottom
    private boolean isWhiteAtBottom = true;

    public ChessGUI(Board board) {
        this.board = board;
        setTitle("Chess Board UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setLayout(null);

        // ---------------------------------
        // 1) Chess Board Panel
        // ---------------------------------
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

        // Mouse Listeners for piece dragging
        chessBoardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Instead of int row = 7 - (e.getY() / TILE_SIZE), we decide based on isWhiteAtBottom

                int file = e.getX() / TILE_SIZE;     // 0..7
                int rank = e.getY() / TILE_SIZE;     // 0..7

                // If White is at bottom, the "top row" in drawing is rank=0 => actual board row=7
                // If Black is at bottom, we invert it.
                if (isWhiteAtBottom) {
                    // row: 7 - rank
                    // col: file
                    selectedPieceIndex = (7 - rank) * 8 + file;
                } else {
                    // row: rank
                    // col: 7 - file
                    selectedPieceIndex = (rank * 8) + (7 - file);
                }

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

                int file = e.getX() / TILE_SIZE;
                int rank = e.getY() / TILE_SIZE;
                int targetIndex;

                if (isWhiteAtBottom) {
                    targetIndex = (7 - rank) * 8 + file;
                } else {
                    targetIndex = (rank * 8) + (7 - file);
                }

                int piece = board.getPiece(selectedPieceIndex);
                if (piece == PieceConstants.NONE) return;

                boolean moveSuccessful = board.makeMove(selectedPieceIndex, targetIndex, ChessGUI.this);
                if (moveSuccessful) {
                    fromIndex = selectedPieceIndex;
                    toIndex = targetIndex;

                    // Record the move
                    recordMove(piece, fromIndex, toIndex);

                    // If it's a pawn reaching the last rank, show promotion popup
                    if ((piece & 7) == PieceConstants.PAWN) {
                        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);
                        int lastRank = isWhite ? 7 : 0;
                        if (targetIndex / 8 == lastRank) {
                            showPromotionPopup(targetIndex, isWhite);
                        }
                    }

                    // NEW: Flip the board if it's now Black's turn, or flip back if White's turn
                    // So the side to move is always at the bottom
                    isWhiteAtBottom = board.isWhiteTurn();
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

        // ---------------------------------
        // 2) Move History Table
        // ---------------------------------
        moveTableModel = new DefaultTableModel(new String[]{"Move No.", "White", "Black"}, 0);
        moveHistoryTable = new JTable(moveTableModel);
        moveHistoryTable.setBounds(1000, 120, 400, 500);
        moveHistoryTable.setRowHeight(30);

        // Hide table header
        JTableHeader tableHeader = moveHistoryTable.getTableHeader();
        tableHeader.setVisible(false);
        tableHeader.setPreferredSize(new Dimension(0, 0));

        // Custom cell renderer to show piece icon + notation
        moveHistoryTable.setDefaultRenderer(Object.class, new MoveCellRenderer());

        JScrollPane moveScrollPane = new JScrollPane(moveHistoryTable);
        moveScrollPane.setBounds(1000, 120, 400, 500);
        moveHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        moveHistoryTable.setShowGrid(false);
        moveHistoryTable.setIntercellSpacing(new Dimension(0, 0));
        moveHistoryTable.setBorder(null);
        moveScrollPane.setBorder(null);

        // Add both board & table to the frame
        getContentPane().add(chessBoardPanel);
        getContentPane().add(moveScrollPane);

        setVisible(true);
    }

    public void repaintBoard() {
        SwingUtilities.invokeLater(chessBoardPanel::repaint);
    }

    /**
     * Show Promotion Popup
     */
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

    /**
     * Draw the Board
     */
    private void drawBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // We'll loop over "screen rows" 0..7, but map them to actual board squares
        for (int screenRow = 0; screenRow < 8; screenRow++) {
            for (int screenCol = 0; screenCol < 8; screenCol++) {
                // Decide which board row/col to get based on isWhiteAtBottom
                int boardRow, boardCol;
                if (isWhiteAtBottom) {
                    boardRow = 7 - screenRow;  // top row in screen => row 7 in board
                    boardCol = screenCol;      // left col in screen => col 0
                } else {
                    boardRow = screenRow;       // top row in screen => row 0 in board
                    boardCol = 7 - screenCol;   // left col in screen => col 7
                }

                int index = boardRow * 8 + boardCol;

                boolean isLastMove = (index == fromIndex || index == toIndex);
                boolean isValidMove = validMoves.contains(index);

                // Where do we draw it on screen?
                int x = screenCol * TILE_SIZE;
                int y = screenRow * TILE_SIZE;

                // Square color
                g2d.setColor(((screenRow + screenCol) % 2 == 0)
                        ? Color.decode("#ebecd0")
                        : Color.decode("#6e9552"));
                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                if (isLastMove) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }

                if (isValidMove) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
    }

    /**
     * Draw Pieces
     */
    private void drawPieces(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // We iterate over all squares 0..63, find the piece, then compute its on-screen position
        for (int index = 0; index < 64; index++) {
            int piece = board.getPiece(index);
            if (piece == PieceConstants.NONE) continue;

            // Convert index to boardRow, boardCol
            int boardRow = index / 8;
            int boardCol = index % 8;

            // Now map boardRow,boardCol to "screen" row,col
            int screenRow, screenCol;
            if (isWhiteAtBottom) {
                screenRow = 7 - boardRow;
                screenCol = boardCol;
            } else {
                screenRow = boardRow;
                screenCol = 7 - boardCol;
            }

            int x = screenCol * TILE_SIZE;
            int y = screenRow * TILE_SIZE;

            Image pieceImage = PieceConstants.getPieceImage(piece);
            if (pieceImage != null) {
                g2d.drawImage(pieceImage, x, y, TILE_SIZE, TILE_SIZE, this);
            }
        }
    }

    /**
     * Record a Move in the History Table
     */
    private void recordMove(int piece, int fromIndex, int toIndex) {
        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);

        int capturedPiece = board.getLastMoveCapturedPiece();
        boolean isCapture = (capturedPiece != PieceConstants.NONE);

        boolean nextTurnIsWhite = !isWhite;
        boolean isCheck = isKingInCheck(board, nextTurnIsWhite);

        String notation = buildAlgebraicNotation(piece, fromIndex, toIndex, isCapture, isCheck);

        // Create scaled icon
        Image pieceImg = PieceConstants.getPieceImage(piece);
        Image scaledImg = pieceImg.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        ImageIcon pieceIcon = new ImageIcon(scaledImg);

        MoveCellData cellData = new MoveCellData(pieceIcon, notation);

        // Insert into table
        if (isWhite) {
            Object[] rowData = { moveNumber + ".", cellData, "" };
            moveTableModel.addRow(rowData);
            waitingForBlackMove = true;
        } else {
            if (waitingForBlackMove) {
                int rowIndex = moveTableModel.getRowCount() - 1;
                moveTableModel.setValueAt(cellData, rowIndex, 2);
                waitingForBlackMove = false;
                moveNumber++;
            } else {
                Object[] rowData = { moveNumber + ".", "", cellData };
                moveTableModel.addRow(rowData);
                moveNumber++;
            }
        }
    }

    private String buildAlgebraicNotation(int piece, int fromIndex, int toIndex, boolean isCapture, boolean isCheck) {
        int type = piece & 7;
        // If king and move distance is 2, it's castling.
        if (type == PieceConstants.KING && Math.abs(fromIndex - toIndex) == 2) {
            String castleNotation = (toIndex > fromIndex) ? "O-O" : "O-O-O";
            if (isCheck) castleNotation += "+";
            return castleNotation;
        }

        // Otherwise normal notation
        String pieceLetter = switch (type) {
            case PieceConstants.KING -> "K";
            case PieceConstants.QUEEN -> "Q";
            case PieceConstants.ROOK -> "R";
            case PieceConstants.BISHOP -> "B";
            case PieceConstants.KNIGHT -> "N";
            default -> ""; // pawn => no letter
        };

        String fromFile = String.valueOf((char) ('a' + (fromIndex % 8)));
        String toFile   = String.valueOf((char) ('a' + (toIndex % 8)));
        int toRankNum   = (toIndex / 8) + 1;
        String toRank   = String.valueOf(toRankNum);

        if (type == PieceConstants.PAWN && isCapture) {
            pieceLetter = fromFile;
        }

        String notation;
        if (isCapture) notation = pieceLetter + "x" + toFile + toRank;
        else notation = pieceLetter + toFile + toRank;

        if (isCheck) notation += "+";
        return notation;
    }

    private boolean isKingInCheck(Board board, boolean sideIsWhite) {
        int kingPiece = (sideIsWhite ? PieceConstants.WHITE : PieceConstants.BLACK) | PieceConstants.KING;
        int kingIndex = -1;
        for (int i = 0; i < 64; i++) {
            if (board.getPiece(i) == kingPiece) {
                kingIndex = i;
                break;
            }
        }
        if (kingIndex == -1) return false;
        return board.isSquareUnderAttack(kingIndex, !sideIsWhite);
    }

    // Container for move cell data
    private static class MoveCellData {
        final ImageIcon icon;
        final String text;
        public MoveCellData(ImageIcon icon, String text) {
            this.icon = icon;
            this.text = text;
        }
    }

    // Custom cell renderer
    private static class MoveCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (value instanceof MoveCellData) {
                MoveCellData data = (MoveCellData) value;
                label.setIcon(data.icon);
                label.setText(" " + data.text);
            } else {
                label.setIcon(null);
                label.setText(value == null ? "" : value.toString());
            }
            label.setHorizontalAlignment(LEFT);
            return label;
        }
    }

    public static void main(String[] args) {
        Board board = new Board();
        SwingUtilities.invokeLater(() -> new ChessGUI(board));
    }
}
