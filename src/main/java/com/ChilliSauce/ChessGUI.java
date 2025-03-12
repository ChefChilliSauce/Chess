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

    // ---------------------------------------------------------
    // UI components
    // ---------------------------------------------------------
    private final JPanel chessBoardPanel;
    private final JTable moveHistoryTable;
    private final DefaultTableModel moveTableModel;
    private JLabel topNameLabel;      // Display name of the player on top
    private JLabel bottomNameLabel;   // Display name of the player on bottom

    // ---------------------------------------------------------
    // Chess logic references
    // ---------------------------------------------------------
    private final Board board;
    private int selectedPieceIndex = -1;
    private int draggedX, draggedY;
    private boolean dragging = false;
    private List<Integer> validMoves = new ArrayList<>();

    // For highlighting last move
    private int fromIndex = -1;
    private int toIndex = -1;

    // ---------------------------------------------------------
    // Move notation tracking
    // ---------------------------------------------------------
    private int moveNumber = 1;
    private boolean waitingForBlackMove = false;

    // ---------------------------------------------------------
    // Player info & board orientation
    // ---------------------------------------------------------
    private String playerOneName;   // e.g. "Alice"
    private String playerTwoName;   // e.g. "Bob"
    private int playerOneColor;     // PieceConstants.WHITE or BLACK
    private int playerTwoColor;     // PieceConstants.WHITE or BLACK
    private boolean boardFlipEnabled;
    private boolean isWhiteAtBottom;  // If true, white is at bottom; otherwise black is at bottom

    /**
     * Constructor with all parameters needed for Pass n Play:
     * - board: the chess logic
     * - playerOneName, playerTwoName: the user-entered names
     * - playerOneColor, playerTwoColor: which color each player has
     * - boardFlipEnabled: whether we flip after each move
     */
    public ChessGUI(Board board,
                    String playerOneName,
                    String playerTwoName,
                    int playerOneColor,
                    int playerTwoColor,
                    boolean boardFlipEnabled) {
        this.board = board;
        this.playerOneName = playerOneName;
        this.playerTwoName = playerTwoName;
        this.playerOneColor = playerOneColor;
        this.playerTwoColor = playerTwoColor;
        this.boardFlipEnabled = boardFlipEnabled;

        // If flipping is disabled, White always at bottom.
        // If flipping is enabled, check whose turn it is to decide initial orientation.
        this.isWhiteAtBottom = boardFlipEnabled ? board.isWhiteTurn() : true;

        setTitle("Chess: " + playerOneName + " vs " + playerTwoName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setLayout(null);

        // -----------------------------
        // Chess Board Panel
        // -----------------------------
        chessBoardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawPieces(g);
            }
        };
        chessBoardPanel.setBounds(50, 50, BOARD_SIZE, BOARD_SIZE);
        chessBoardPanel.setLayout(null);
        chessBoardPanel.setBackground(Color.LIGHT_GRAY);

        // Mouse Listeners
        chessBoardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int file = e.getX() / TILE_SIZE;
                int rank = e.getY() / TILE_SIZE;

                if (isWhiteAtBottom) {
                    // White at bottom => top row in screen is board row 7
                    selectedPieceIndex = (7 - rank) * 8 + file;
                } else {
                    // Black at bottom => top row in screen is board row 0
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
                    recordMove(piece, fromIndex, toIndex);

                    // If it was a pawn promotion, show popup (already handled in board, but let's confirm)
                    if ((piece & 7) == PieceConstants.PAWN) {
                        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);
                        int lastRank = isWhite ? 7 : 0;
                        if (targetIndex / 8 == lastRank) {
                            showPromotionPopup(targetIndex, isWhite);
                        }
                    }

                    // Flip board if enabled
                    if (boardFlipEnabled) {
                        isWhiteAtBottom = board.isWhiteTurn();
                    } else {
                        isWhiteAtBottom = true; // Always keep white at bottom if flipping is off
                    }
                    // Update name labels so top/bottom swap if needed
                    updateNameLabels();
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

        // -----------------------------
        // Move History Table
        // -----------------------------
        moveTableModel = new DefaultTableModel(new String[]{"Move No.", "White", "Black"}, 0);
        moveHistoryTable = new JTable(moveTableModel);
        moveHistoryTable.setBounds(750, 50, 400, 500);
        moveHistoryTable.setRowHeight(30);

        // Hide table header
        JTableHeader tableHeader = moveHistoryTable.getTableHeader();
        tableHeader.setVisible(false);
        tableHeader.setPreferredSize(new Dimension(0, 0));
        tableHeader.setBorder(null);

        moveHistoryTable.setShowGrid(false);
        moveHistoryTable.setIntercellSpacing(new Dimension(0, 0));
        moveHistoryTable.setBorder(null);
        moveHistoryTable.setDefaultRenderer(Object.class, new MoveCellRenderer());

        JScrollPane moveScrollPane = new JScrollPane(moveHistoryTable);
        moveScrollPane.setBounds(750, 50, 400, 500);
        moveScrollPane.setBorder(null);

        // Force the first column to be small
        moveHistoryTable.getColumnModel().getColumn(0).setMinWidth(40);
        moveHistoryTable.getColumnModel().getColumn(0).setMaxWidth(40);
        moveHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(40);

        // -----------------------------
        // Name Labels (top & bottom)
        // -----------------------------
        topNameLabel = new JLabel("", SwingConstants.CENTER);
        bottomNameLabel = new JLabel("", SwingConstants.CENTER);

        topNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        bottomNameLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Position them around the board
        // Board is at (50,50), size 640 => let's place top label at y=20, bottom at y=700
        topNameLabel.setBounds(50, 20, 640, 30);
        bottomNameLabel.setBounds(50, 50 + BOARD_SIZE + 10, 640, 30);

        // Add them
        getContentPane().add(chessBoardPanel);
        getContentPane().add(moveScrollPane);
        getContentPane().add(topNameLabel);
        getContentPane().add(bottomNameLabel);

        // Do an initial label update
        updateNameLabels();

        setVisible(true);
    }

    // ---------------------------------------------------------
    // Update the name labels based on which color is at bottom
    // ---------------------------------------------------------
    private void updateNameLabels() {
        // Figure out which player is White, which is Black
        String whitePlayer = (playerOneColor == PieceConstants.WHITE) ? playerOneName : playerTwoName;
        String blackPlayer = (playerOneColor == PieceConstants.BLACK) ? playerOneName : playerTwoName;

        if (isWhiteAtBottom) {
            bottomNameLabel.setText(whitePlayer);
            topNameLabel.setText(blackPlayer);
        } else {
            bottomNameLabel.setText(blackPlayer);
            topNameLabel.setText(whitePlayer);
        }
    }

    // Repaint the board
    public void repaintBoard() {
        SwingUtilities.invokeLater(chessBoardPanel::repaint);
    }

    // ---------------------------------------------------------
    // Show Promotion Popup
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // Drawing the Board (tiles, highlights)
    // ---------------------------------------------------------
    private void drawBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (int screenRow = 0; screenRow < 8; screenRow++) {
            for (int screenCol = 0; screenCol < 8; screenCol++) {
                int boardRow, boardCol;
                if (isWhiteAtBottom) {
                    boardRow = 7 - screenRow;
                    boardCol = screenCol;
                } else {
                    boardRow = screenRow;
                    boardCol = 7 - screenCol;
                }

                int index = boardRow * 8 + boardCol;
                boolean isLastMove = (index == fromIndex || index == toIndex);
                boolean isValidMove = validMoves.contains(index);

                int x = screenCol * TILE_SIZE;
                int y = screenRow * TILE_SIZE;

                // Normal squares
                g2d.setColor(((screenRow + screenCol) % 2 == 0)
                        ? Color.decode("#ebecd0")
                        : Color.decode("#6e9552"));
                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                // Highlight last move
                if (isLastMove) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }

                // Highlight valid moves
                if (isValidMove) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
    }

    // ---------------------------------------------------------
    // Drawing the Pieces
    // ---------------------------------------------------------
    private void drawPieces(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (int index = 0; index < 64; index++) {
            int piece = board.getPiece(index);
            if (piece == PieceConstants.NONE) continue;

            int boardRow = index / 8;
            int boardCol = index % 8;
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

    // ---------------------------------------------------------
    // Recording a move in the move history table
    // ---------------------------------------------------------
    private void recordMove(int piece, int fromIndex, int toIndex) {
        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);

        int capturedPiece = board.getLastMoveCapturedPiece();
        boolean isCapture = (capturedPiece != PieceConstants.NONE);

        boolean nextTurnIsWhite = !isWhite;
        boolean isCheck = isKingInCheck(board, nextTurnIsWhite);

        String notation = buildAlgebraicNotation(piece, fromIndex, toIndex, isCapture, isCheck);

        // Create a scaled icon
        Image pieceImg = PieceConstants.getPieceImage(piece);
        Image scaledImg = pieceImg.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        ImageIcon pieceIcon = new ImageIcon(scaledImg);

        MoveCellData cellData = new MoveCellData(pieceIcon, notation);

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

    private String buildAlgebraicNotation(int piece, int fromIndex, int toIndex,
                                          boolean isCapture, boolean isCheck) {
        int type = piece & 7;
        // Castling
        if (type == PieceConstants.KING && Math.abs(fromIndex - toIndex) == 2) {
            String castleNotation = (toIndex > fromIndex) ? "O-O" : "O-O-O";
            if (isCheck) castleNotation += "+";
            return castleNotation;
        }

        String pieceLetter = switch (type) {
            case PieceConstants.KING   -> "K";
            case PieceConstants.QUEEN  -> "Q";
            case PieceConstants.ROOK   -> "R";
            case PieceConstants.BISHOP -> "B";
            case PieceConstants.KNIGHT -> "N";
            default -> ""; // pawn
        };

        String fromFile = String.valueOf((char) ('a' + (fromIndex % 8)));
        String toFile   = String.valueOf((char) ('a' + (toIndex % 8)));
        int toRankNum   = (toIndex / 8) + 1;
        String toRank   = String.valueOf(toRankNum);

        // Pawns capturing
        if (type == PieceConstants.PAWN && isCapture) {
            pieceLetter = fromFile;
        }

        String notation = isCapture
                ? pieceLetter + "x" + toFile + toRank
                : pieceLetter + toFile + toRank;

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

    // ---------------------------------------------------------
    // Data structure & renderer for table cells
    // ---------------------------------------------------------
    private static class MoveCellData {
        final ImageIcon icon;
        final String text;

        public MoveCellData(ImageIcon icon, String text) {
            this.icon = icon;
            this.text = text;
        }
    }

    private static class MoveCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column)
        {
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

    // ---------------------------------------------------------
    // (Optional) main method for local testing
    // ---------------------------------------------------------
    public static void main(String[] args) {
        Board board = new Board();
        // Example: PlayerOne is White, PlayerTwo is Black, board flip is ON
        SwingUtilities.invokeLater(() -> new ChessGUI(board,
                "Alice",
                "Bob",
                PieceConstants.WHITE,
                PieceConstants.BLACK,
                true));
    }
}
