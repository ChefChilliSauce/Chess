package com.ChilliSauce;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader; // This will help remove headers



public class ChessGUI extends JFrame {
    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = TILE_SIZE * 8;
    private final Board board;
    private final TurnManager turnManager;
    private char selectedFile = '-';
    private int selectedRank = -1;
    private final List<Point> legalMoves;
    private boolean flipped = false;
    private JLabel playerTurnLabel;
    private JPanel capturedWhitePanel;
    private JPanel capturedBlackPanel;
    private JTable moveHistoryTable;
    private DefaultTableModel moveTableModel;
    private String whitePlayer;
    private String blackPlayer;

    public ChessGUI(String whitePlayer, String blackPlayer) {
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        board = new Board();
        turnManager = new TurnManager();
        legalMoves = new ArrayList<>();

        // Set layout to null for manual positioning
        setLayout(null);

        // Chess Board Panel
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawPieces(g);
                drawLegalMoves(g);
            }
        };
        panel.setBounds(100, 100, BOARD_SIZE, BOARD_SIZE); // Position (X, Y) and size
        panel.setLayout(null);
        panel.setBackground(Color.LIGHT_GRAY);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
                repaint();
            }
        });

        // Player Turn Label
        playerTurnLabel = new JLabel("Current Turn: " + whitePlayer, SwingConstants.CENTER);
        playerTurnLabel.setBounds(600, 50, 200, 30); // Position label

// Move History Table
        moveTableModel = new DefaultTableModel(new String[]{"Move No.", "White", "Black"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent editing of move history
            }
        };

        moveHistoryTable = new JTable(moveTableModel);
        moveHistoryTable.setBounds(1000, 120, 400, 500); // Set position manually
        moveHistoryTable.setShowGrid(false); // Hide grid lines
        moveHistoryTable.setRowHeight(30); // Adjust row height for better readability

// Hide the column headers
        JTableHeader tableHeader = moveHistoryTable.getTableHeader();
        tableHeader.setVisible(false);
        tableHeader.setPreferredSize(new Dimension(0, 0));

// Apply custom renderer for alternating row colors
        moveHistoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.LIGHT_GRAY : Color.DARK_GRAY); // Alternate colors
                    c.setForeground(Color.BLACK); // Set text color
                }
                return c;
            }
        });

        JScrollPane moveScrollPane = new JScrollPane(moveHistoryTable);
        moveScrollPane.setBounds(1000, 120, 400, 500); // Set position manually


// Add to GUI
        getContentPane().add(moveScrollPane);


        // Captured Pieces Panels
        capturedWhitePanel = new JPanel();
        capturedBlackPanel = new JPanel();
        capturedWhitePanel.setBounds(600, 420, 200, 80);
        capturedBlackPanel.setBounds(600, 520, 200, 80);

        JLabel capturedBlackLabel = new JLabel("Captured by Black:");
        capturedBlackLabel.setBounds(600, 400, 200, 20);

        JLabel capturedWhiteLabel = new JLabel("Captured by White:");
        capturedWhiteLabel.setBounds(600, 500, 200, 20);

        // Add elements manually
        getContentPane().add(panel);
        getContentPane().add(playerTurnLabel);
        getContentPane().add(moveScrollPane);
        getContentPane().add(capturedBlackLabel);
        getContentPane().add(capturedWhiteLabel);
        getContentPane().add(capturedWhitePanel);
        getContentPane().add(capturedBlackPanel);

        setVisible(true);
    }

    private void drawBoard(Graphics g) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int drawRow = flipped ? 7 - row : row;
                int drawCol = flipped ? 7 - col : col;
                g.setColor((drawRow + drawCol) % 2 == 0 ? Color.WHITE : Color.DARK_GRAY);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawPieces(Graphics g) {
        for (char file = 'a'; file <= 'h'; file++) {
            for (int rank = 1; rank <= 8; rank++) {
                Piece piece = board.getPiece(file, rank);
                if (piece != null) {
                    int col = Board.fileToIndex(file);
                    int row = Board.rankToIndex(rank);
                    if (flipped) {
                        col = 7 - col;
                        row = 7 - row;
                    }
                    Image pieceImage = piece.getImage();
                    g.drawImage(pieceImage, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }
    }

    private void drawLegalMoves(Graphics g) {
        g.setColor(new Color(50, 200, 50, 180));
        for (Point move : legalMoves) {
            int x = move.x * TILE_SIZE + TILE_SIZE / 2 - 10;
            int y = move.y * TILE_SIZE + TILE_SIZE / 2 - 10;
            if (flipped) {
                x = (7 - move.x) * TILE_SIZE + TILE_SIZE / 2 - 10;
                y = (7 - move.y) * TILE_SIZE + TILE_SIZE / 2 - 10;
            }
            g.fillOval(x, y, 20, 20);
        }
    }

    private void handleMouseClick(MouseEvent e) {
        int col = e.getX() / TILE_SIZE;
        int row = e.getY() / TILE_SIZE;
        if (flipped) {
            col = 7 - col;
            row = 7 - row;
        }

        char file = (char) ('a' + col);
        int rank = 8 - row;
        Piece piece = board.getPiece(file, rank);

        if (selectedFile == '-' && selectedRank == -1) {
            if (piece != null && turnManager.isPlayerTurn(piece.getColor())) {
                selectedFile = file;
                selectedRank = rank;
                MoveValidator validator = board.getMoveValidator(piece);
                if (validator != null) {
                    legalMoves.clear();
                    legalMoves.addAll(MoveHighlighter.getLegalMoves(board, file, rank));
                }
            }
        } else {
            Piece capturedPiece = board.getPiece(file, rank); // ✅ Get captured piece BEFORE moving

            if (board.movePiece(selectedFile, selectedRank, file, rank, turnManager)) {
                String moveNotation = formatMove(selectedFile, selectedRank, file, rank, capturedPiece);
                updateMoveHistory(moveNotation); // ✅ Add move to the table

                if (capturedPiece != null) {
                    System.out.println("Captured Piece: " + capturedPiece.getClass().getSimpleName() + " at " + file + rank);
                    SoundManager.playCaptureSound();  // ✅ Play only if a piece was actually captured
                } else {
                    System.out.println("Regular Move: No piece captured at " + file + rank);
                    SoundManager.playMoveSound();  // ✅ Play normal move sound otherwise
                }

                flipBoard();
                updateTurnDisplay();
            }

            selectedFile = '-';
            selectedRank = -1;
            legalMoves.clear();
            repaint();
        }
    }

    private int moveNumber = 1; // Track the move number

    private void updateMoveHistory(String moveNotation) {
        if (turnManager.getCurrentPlayerColor().equals("black")) {
            // White's move - Create a new row with the current move number
            moveTableModel.addRow(new Object[]{moveNumber + ".", moveNotation, ""});
        } else {
            // Black's move - Update the last row with Black's move
            moveTableModel.setValueAt(moveNotation, moveTableModel.getRowCount() - 1, 2);
            moveNumber++; // Increment the move number after both White and Black have moved
        }
    }


    private String formatMove(char fromFile, int fromRank, char toFile, int toRank, Piece capturedPiece) {
        String move = "" + toFile + toRank; // Only show final square
        if (capturedPiece != null) {
            move = move + "+"; // Add 'x' if a piece was captured
        }
        return move;
    }



    private void flipBoard() {
        flipped = !flipped;
    }

    private void updateTurnDisplay() {
        playerTurnLabel.setText("Current Turn: " + (turnManager.getCurrentPlayerColor().equals("white") ? whitePlayer : blackPlayer));
    }
}
