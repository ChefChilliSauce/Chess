//package com.ChilliSauce;
//
//import javax.swing.*;
//import javax.swing.table.DefaultTableCellRenderer;
//import javax.swing.table.DefaultTableModel;
//import javax.swing.table.JTableHeader;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.geom.RoundRectangle2D;
//import java.util.ArrayList;
//import java.util.List;
//
//public class ChessGUI extends JFrame {
//    private static final int TILE_SIZE = 80;
//    private static final int BOARD_SIZE = TILE_SIZE * 8;
//
//    // ---------------------------------------------------------
//    // UI components
//    // ---------------------------------------------------------
//    private final JPanel chessBoardPanel;
//    private final JTable moveHistoryTable;
//    private final DefaultTableModel moveTableModel;
//    private JLabel topNameLabel;      // Display name of the player on top
//    private JLabel bottomNameLabel;   // Display name of the player on bottom
//
//    // ---------------------------------------------------------
//    // Chess logic references
//    // ---------------------------------------------------------
//    private final Board board;
//    private int selectedPieceIndex = -1;
//    private int draggedX, draggedY;
//    private boolean dragging = false;
//    private List<Integer> validMoves = new ArrayList<>();
//
//    // For highlighting last move
//    private int fromIndex = -1;
//    private int toIndex = -1;
//
//    // ---------------------------------------------------------
//    // Move notation tracking
//    // ---------------------------------------------------------
//    private int moveNumber = 1;
//    private boolean waitingForBlackMove = false;
//
//    // ---------------------------------------------------------
//    // Player info & board orientation
//    // ---------------------------------------------------------
//    private String playerOneName;   // e.g. "Alice"
//    private String playerTwoName;   // e.g. "Bob"
//    private int playerOneColor;     // PieceConstants.WHITE or BLACK
//    private int playerTwoColor;     // PieceConstants.WHITE or BLACK
//    private boolean boardFlipEnabled;
//    private boolean isWhiteAtBottom;  // If true, white is at bottom; otherwise black is at bottom
//    private boolean isFullScreen = false;
//    private Rectangle windowedBounds = null;
//    private JPanel factBubblePanel;
//    private JLabel factIconLabel;
//    private JLabel factTextLabel;
//    private Timer factTimer;
//    private List<String> facts = new ArrayList<>();
//    private int factIndex = 0;
//    private JLabel outsideIconLabel;
//
//    /**
//     * Constructor with all parameters needed for Pass n Play:
//     * - board: the chess logic
//     * - playerOneName, playerTwoName: the user-entered names
//     * - playerOneColor, playerTwoColor: which color each player has
//     * - boardFlipEnabled: whether we flip after each move
//     */
//    public ChessGUI(Board board,
//                    String playerOneName,
//                    String playerTwoName,
//                    int playerOneColor,
//                    int playerTwoColor,
//                    boolean boardFlipEnabled) {
//        this.board = board;
//        this.playerOneName = playerOneName;
//        this.playerTwoName = playerTwoName;
//        this.playerOneColor = playerOneColor;
//        this.playerTwoColor = playerTwoColor;
//        this.boardFlipEnabled = boardFlipEnabled;
//
//        // If flipping is disabled, White always at bottom.
//        // If flipping is enabled, check whose turn it is to decide initial orientation.
//        this.isWhiteAtBottom = boardFlipEnabled ? board.isWhiteTurn() : true;
//        getContentPane().setBackground(Color.decode("#363636"));
//
//        setTitle("Chess: " + playerOneName + " vs " + playerTwoName);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setExtendedState(JFrame.MAXIMIZED_BOTH);
//        setResizable(false);
//        setLayout(null);
//        addButtons();
//        addFactBubble();
//        setVisible(true);
//
//        // -----------------------------
//        // Chess Board Panel
//        // -----------------------------
//        chessBoardPanel = new JPanel() {
//            @Override
//            protected void paintComponent(Graphics g) {
//                super.paintComponent(g);
//                drawBoard(g);
//                drawPieces(g);
//            }
//        };
//        chessBoardPanel.setBounds(140, 100, BOARD_SIZE, BOARD_SIZE);
//        chessBoardPanel.setLayout(null);
//        chessBoardPanel.setBackground(Color.decode("#363636"));
//
//        // Mouse Listeners
//        chessBoardPanel.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
//                int file = e.getX() / TILE_SIZE;
//                int rank = e.getY() / TILE_SIZE;
//
//                if (isWhiteAtBottom) {
//                    // White at bottom => top row in screen is board row 7
//                    selectedPieceIndex = (7 - rank) * 8 + file;
//                } else {
//                    // Black at bottom => top row in screen is board row 0
//                    selectedPieceIndex = (rank * 8) + (7 - file);
//                }
//
//                if (board.getPiece(selectedPieceIndex) != PieceConstants.NONE) {
//                    dragging = true;
//                    draggedX = e.getX();
//                    draggedY = e.getY();
//                    validMoves = board.getValidMoves(selectedPieceIndex);
//                } else {
//                    validMoves.clear();
//                }
//                repaintBoard();
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                if (!dragging || selectedPieceIndex == -1) return;
//
//                int file = e.getX() / TILE_SIZE;
//                int rank = e.getY() / TILE_SIZE;
//                int targetIndex;
//
//                if (isWhiteAtBottom) {
//                    targetIndex = (7 - rank) * 8 + file;
//                } else {
//                    targetIndex = (rank * 8) + (7 - file);
//                }
//
//                int piece = board.getPiece(selectedPieceIndex);
//                if (piece == PieceConstants.NONE) return;
//
//                boolean moveSuccessful = board.makeMove(selectedPieceIndex, targetIndex, ChessGUI.this);
//                if (moveSuccessful) {
//                    fromIndex = selectedPieceIndex;
//                    toIndex = targetIndex;
//                    recordMove(piece, fromIndex, toIndex);
//
//                    // If it was a pawn promotion, show popup (already handled in board, but let's confirm)
//                    if ((piece & 7) == PieceConstants.PAWN) {
//                        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);
//                        int lastRank = isWhite ? 7 : 0;
//                        if (targetIndex / 8 == lastRank) {
//                            showPromotionPopup(targetIndex, isWhite);
//                        }
//                    }
//
//                    // Flip board if enabled
//                    if (boardFlipEnabled) {
//                        isWhiteAtBottom = board.isWhiteTurn();
//                    } else {
//                        isWhiteAtBottom = true; // Always keep white at bottom if flipping is off
//                    }
//                    // Update name labels so top/bottom swap if needed
//                    updateNameLabels();
//                    repaintBoard();
//                }
//
//                validMoves.clear();
//                selectedPieceIndex = -1;
//                dragging = false;
//                repaintBoard();
//            }
//        });
//
//        chessBoardPanel.addMouseMotionListener(new MouseAdapter() {
//            @Override
//            public void mouseDragged(MouseEvent e) {
//                if (dragging) {
//                    draggedX = e.getX();
//                    draggedY = e.getY();
//                    chessBoardPanel.repaint();
//                }
//            }
//        });
//
//        // -----------------------------
//// Move History Table
//// -----------------------------
//
//// 1) Create the table model & table
//        moveTableModel = new DefaultTableModel(new String[]{"Move No.", "White", "Black"}, 0);
//        moveHistoryTable = new JTable(moveTableModel);
//        moveHistoryTable.setRowHeight(30);
//
//// 2) Hide the header
//        JTableHeader tableHeader = moveHistoryTable.getTableHeader();
//        tableHeader.setVisible(false);
//        tableHeader.setPreferredSize(new Dimension(0, 0));
//        tableHeader.setBorder(null);
//
//// 3) Remove grid lines & spacing
//        moveHistoryTable.setShowGrid(false);
//        moveHistoryTable.setIntercellSpacing(new Dimension(0, 0));
//
//// 4) Remove table border & focus outlines
//        moveHistoryTable.setBorder(null);
//        moveHistoryTable.setFocusable(false);
//        UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder());
//
//
//// 5) Make the table fill the viewport’s background
//        moveHistoryTable.setFillsViewportHeight(true);
//
//// 6) Set a dark background & make it opaque
//        moveHistoryTable.setBackground(Color.decode("#363636"));
//        moveHistoryTable.setOpaque(true);
//        moveHistoryTable.setUI(new javax.swing.plaf.basic.BasicTableUI());
//
//
//
//// 7) Custom cell renderer for a dark background
//        moveHistoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
//            @Override
//            public Component getTableCellRendererComponent(JTable table,
//                                                           Object value,
//                                                           boolean isSelected,
//                                                           boolean hasFocus,
//                                                           int row,
//                                                           int column) {
//                JLabel label = (JLabel) super.getTableCellRendererComponent(
//                        table, value, isSelected, hasFocus, row, column
//                );
//                label.setOpaque(true);
//
//                // Dark background
//                if (!isSelected) {
//                    label.setBackground(Color.decode("#363636"));
//                    label.setForeground(Color.WHITE);
//                } else {
//                    label.setBackground(Color.decode("#505050"));
//                    label.setForeground(Color.WHITE);
//                }
//
//                // If it's our custom MoveCellData, display the icon & text
//                if (value instanceof MoveCellData) {
//                    MoveCellData data = (MoveCellData) value;
//                    label.setIcon(data.icon);
//                    label.setText(" " + data.text);
//                } else {
//                    // If it's just a string (like "1."), show that text
//                    label.setIcon(null);
//                    label.setText(value == null ? "" : value.toString());
//                }
//                return label;
//            }
//        });
//
//
//// 8) Create the scroll pane & position it
//        JScrollPane moveScrollPane = new JScrollPane(moveHistoryTable);
//        moveScrollPane.setBounds(950, 420, 350, 240);
//
//// 9) Hide scrollbars entirely (mouse wheel still works)
//        moveScrollPane.setUI(new javax.swing.plaf.basic.BasicScrollPaneUI());
//        moveScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
//        moveScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//        moveScrollPane.setUI(new javax.swing.plaf.basic.BasicScrollPaneUI());
//
//// 10) Remove all borders from the scroll pane
//        moveScrollPane.setBorder(null);
//        moveScrollPane.setViewportBorder(null);
//        moveScrollPane.getViewport().setBorder(null);
//        moveScrollPane.setFocusable(false);
//
//// 11) Make the scroll pane & viewport dark
//        moveScrollPane.setBackground(Color.decode("#363636"));
//        moveScrollPane.setOpaque(true);
//        moveScrollPane.getViewport().setBackground(Color.decode("#363636"));
//        moveScrollPane.getViewport().setOpaque(true);
//
//// 12) Force the first column to be small
//        moveHistoryTable.getColumnModel().getColumn(0).setMinWidth(40);
//        moveHistoryTable.getColumnModel().getColumn(0).setMaxWidth(40);
//        moveHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(40);
//
//// 13) Finally, add it to the frame
//        getContentPane().add(moveScrollPane);
//
//
//
//        // -----------------------------
//        // Name Labels (top & bottom)
//        // -----------------------------
//        topNameLabel = new JLabel("", SwingConstants.CENTER);
//        bottomNameLabel = new JLabel("", SwingConstants.CENTER);
//
//        topNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
//        bottomNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
//
//        // Position them around the board
//        //  is at (50,50), size 640 => let's place top label at y=20, bottom at y=700
//        topNameLabel.setBounds(50, 20, 640, 30);
//        bottomNameLabel.setBounds(50, 50 + BOARD_SIZE + 10, 640, 30);
//
//        // Add them
//        getContentPane().add(chessBoardPanel);
//        getContentPane().add(moveScrollPane);
//        getContentPane().add(topNameLabel);
//        getContentPane().add(bottomNameLabel);
//
//        // Do an initial label update
//        updateNameLabels();
//
//        setVisible(true);
//    }
//
//    // ---------------------------------------------------------
//    // Update the name labels based on which color is at bottom
//    // ---------------------------------------------------------
//    private void updateNameLabels() {
//        // Figure out which player is White, which is Black
//        String whitePlayer = (playerOneColor == PieceConstants.WHITE) ? playerOneName : playerTwoName;
//        String blackPlayer = (playerOneColor == PieceConstants.BLACK) ? playerOneName : playerTwoName;
//
//        if (isWhiteAtBottom) {
//            bottomNameLabel.setText(whitePlayer);
//            topNameLabel.setText(blackPlayer);
//        } else {
//            bottomNameLabel.setText(blackPlayer);
//            topNameLabel.setText(whitePlayer);
//        }
//    }
//
//    // Repaint the board
//    public void repaintBoard() {
//        SwingUtilities.invokeLater(chessBoardPanel::repaint);
//    }
//
//    // ---------------------------------------------------------
//    // Show Promotion Popup
//    // ---------------------------------------------------------
//    private void showPromotionPopup(int index, boolean isWhite) {
//        JDialog promotionDialog = new JDialog(this, "Choose Promotion Piece", true);
//        promotionDialog.setLayout(new GridLayout(1, 4));
//        promotionDialog.setUndecorated(true);
//
//        String colorPrefix = isWhite ? "w" : "b";
//        String[] pieceNames = {"q", "r", "b", "n"};
//        int[] pieceTypes = {PieceConstants.QUEEN, PieceConstants.ROOK, PieceConstants.BISHOP, PieceConstants.KNIGHT};
//
//        for (int i = 0; i < 4; i++) {
//            String imagePath = "/assets/" + colorPrefix + pieceNames[i] + ".png";
//            java.net.URL imgURL = getClass().getResource(imagePath);
//            if (imgURL == null) {
//                System.err.println("⚠ ERROR: Image not found: " + imagePath);
//                continue;
//            }
//            ImageIcon originalIcon = new ImageIcon(imgURL);
//            Image scaledImage = originalIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
//            ImageIcon resizedIcon = new ImageIcon(scaledImage);
//
//            JButton button = new JButton(resizedIcon);
//            button.setPreferredSize(new Dimension(80, 80));
//
//            final int selectedPiece = pieceTypes[i] | (isWhite ? PieceConstants.WHITE : PieceConstants.BLACK);
//            button.addActionListener(e -> {
//                board.setPiece(index, selectedPiece);
//                promotionDialog.dispose();
//                SoundManager.playPromotionSound();
//                repaintBoard();
//            });
//            promotionDialog.add(button);
//        }
//
//        promotionDialog.pack();
//        promotionDialog.setLocationRelativeTo(this);
//        promotionDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
//        promotionDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
//        promotionDialog.setVisible(true);
//    }
//
//    // ---------------------------------------------------------
//    // Drawing the Board (tiles, highlights)
//    // ---------------------------------------------------------
//    private void drawBoard(Graphics g) {
//        Graphics2D g2d = (Graphics2D) g;
//        for (int screenRow = 0; screenRow < 8; screenRow++) {
//            for (int screenCol = 0; screenCol < 8; screenCol++) {
//                int boardRow, boardCol;
//                if (isWhiteAtBottom) {
//                    boardRow = 7 - screenRow;
//                    boardCol = screenCol;
//                } else {
//                    boardRow = screenRow;
//                    boardCol = 7 - screenCol;
//                }
//
//                int index = boardRow * 8 + boardCol;
//                boolean isLastMove = (index == fromIndex || index == toIndex);
//                boolean isValidMove = validMoves.contains(index);
//
//                int x = screenCol * TILE_SIZE;
//                int y = screenRow * TILE_SIZE;
//
//                // Normal squares
//                g2d.setColor(((screenRow + screenCol) % 2 == 0)
//                        ? Color.decode("#FAFAFA")
//                        : Color.decode("#037A88"));
//                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
//
//                // Highlight last move
//                if (isLastMove) {
//                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
//                    g2d.setColor(Color.YELLOW);
//                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
//                }
//
//                // Highlight valid moves
//                if (isValidMove) {
//                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
//                    g2d.setColor(Color.BLUE);
//                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
//                }
//
//                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
//            }
//        }
//    }
//
//    // ---------------------------------------------------------
//    // Drawing the Pieces
//    // ---------------------------------------------------------
//    private void drawPieces(Graphics g) {
//        Graphics2D g2d = (Graphics2D) g;
//        for (int index = 0; index < 64; index++) {
//            int piece = board.getPiece(index);
//            if (piece == PieceConstants.NONE) continue;
//
//            int boardRow = index / 8;
//            int boardCol = index % 8;
//            int screenRow, screenCol;
//
//            if (isWhiteAtBottom) {
//                screenRow = 7 - boardRow;
//                screenCol = boardCol;
//            } else {
//                screenRow = boardRow;
//                screenCol = 7 - boardCol;
//            }
//
//            int x = screenCol * TILE_SIZE;
//            int y = screenRow * TILE_SIZE;
//
//            Image pieceImage = PieceConstants.getPieceImage(piece);
//            if (pieceImage != null) {
//                g2d.drawImage(pieceImage, x, y, TILE_SIZE, TILE_SIZE, this);
//            }
//        }
//    }
//
//    // ---------------------------------------------------------
//    // Recording a move in the move history table
//    // ---------------------------------------------------------
//    private void recordMove(int piece, int fromIndex, int toIndex) {
//        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);
//
//        int capturedPiece = board.getLastMoveCapturedPiece();
//        boolean isCapture = (capturedPiece != PieceConstants.NONE);
//
//        boolean nextTurnIsWhite = !isWhite;
//        boolean isCheck = isKingInCheck(board, nextTurnIsWhite);
//
//        String notation = buildAlgebraicNotation(piece, fromIndex, toIndex, isCapture, isCheck);
//
//        // Create a scaled icon
//        Image pieceImg = PieceConstants.getPieceImage(piece);
//        Image scaledImg = pieceImg.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
//        ImageIcon pieceIcon = new ImageIcon(scaledImg);
//
//        MoveCellData cellData = new MoveCellData(pieceIcon, notation);
//
//        if (isWhite) {
//            Object[] rowData = { moveNumber + ".", cellData, "" };
//            moveTableModel.addRow(rowData);
//            waitingForBlackMove = true;
//        } else {
//            if (waitingForBlackMove) {
//                int rowIndex = moveTableModel.getRowCount() - 1;
//                moveTableModel.setValueAt(cellData, rowIndex, 2);
//                waitingForBlackMove = false;
//            } else {
//                Object[] rowData = { moveNumber + ".", "", cellData };
//                moveTableModel.addRow(rowData);
//            }
//            moveNumber++;
//        }
//    }
//
//    private String buildAlgebraicNotation(int piece, int fromIndex, int toIndex,
//                                          boolean isCapture, boolean isCheck) {
//        int type = piece & 7;
//        // Castling
//        if (type == PieceConstants.KING && Math.abs(fromIndex - toIndex) == 2) {
//            String castleNotation = (toIndex > fromIndex) ? "O-O" : "O-O-O";
//            if (isCheck) castleNotation += "+";
//            return castleNotation;
//        }
//
//        String pieceLetter = switch (type) {
//            case PieceConstants.KING   -> "K";
//            case PieceConstants.QUEEN  -> "Q";
//            case PieceConstants.ROOK   -> "R";
//            case PieceConstants.BISHOP -> "B";
//            case PieceConstants.KNIGHT -> "N";
//            default -> ""; // pawn
//        };
//
//        String fromFile = String.valueOf((char) ('a' + (fromIndex % 8)));
//        String toFile   = String.valueOf((char) ('a' + (toIndex % 8)));
//        int toRankNum   = (toIndex / 8) + 1;
//        String toRank   = String.valueOf(toRankNum);
//
//        // Pawns capturing
//        if (type == PieceConstants.PAWN && isCapture) {
//            pieceLetter = fromFile;
//        }
//
//        String notation = isCapture
//                ? pieceLetter + "x" + toFile + toRank
//                : pieceLetter + toFile + toRank;
//
//        if (isCheck) notation += "+";
//        return notation;
//    }
//
//    private boolean isKingInCheck(Board board, boolean sideIsWhite) {
//        int kingPiece = (sideIsWhite ? PieceConstants.WHITE : PieceConstants.BLACK) | PieceConstants.KING;
//        int kingIndex = -1;
//        for (int i = 0; i < 64; i++) {
//            if (board.getPiece(i) == kingPiece) {
//                kingIndex = i;
//                break;
//            }
//        }
//        if (kingIndex == -1) return false;
//        return board.isSquareUnderAttack(kingIndex, !sideIsWhite);
//    }
//
//    // ---------------------------------------------------------
//        // Data structure & renderer for table cells
//        // ---------------------------------------------------------
//        private record MoveCellData(ImageIcon icon, String text) {
//    }
//
//    // ---------------------------------------------------------
//    // (Optional) main method for local testing
//    // ---------------------------------------------------------
//    public static void main(String[] args) {
//        Board board = new Board();
//            try {
//                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//            } catch (Exception e) {
//                // Ignore or print stack trace
//            }
//
//        // Example: PlayerOne is White, PlayerTwo is Black, board flip is ON
//        SwingUtilities.invokeLater(() -> new ChessGUI(board,
//                "Alice",
//                "Bob",
//                PieceConstants.WHITE,
//                PieceConstants.BLACK,
//                true));
//    }
//
//    /**
//     * Creates three buttons:
//     *   1) Button at (950, 700), width=110, no functionality yet.
//     *   2) Button at (1070, 700), width=110, no functionality yet.
//     *   3) Button at (1190, 700), width=110, toggles true fullscreen on click.
//     */
//    private void addButtons() {
//        // 1) First button (no functionality)
//        JButton button1 = new JButton("Button 1");
//        button1.setBounds(950, 680, 83, 40);
//        getContentPane().add(button1);
//
//        // 2) Second button (no functionality)
//        JButton button2 = new JButton("Button 2");
//        button2.setBounds(1039, 680, 83, 40);
//        getContentPane().add(button2);
//
//        // 3) Fullscreen toggle button
//        JButton toggleFsButton = new JButton("Toggle FS");
//        toggleFsButton.setBounds(1128, 680, 83, 40);
//        toggleFsButton.addActionListener(e -> toggleFullScreen());
//        getContentPane().add(toggleFsButton);
//
//        // 4) Second button (no functionality)
//        JButton button4 = new JButton("Button 4");
//        button4.setBounds(1217, 680, 83, 40);
//        getContentPane().add(button4);
//    }
//
//    /**
//     * Toggles between true fullscreen (using GraphicsDevice.setFullScreenWindow)
//     * and returning to the previous windowed size/position.
//     */
//    private void toggleFullScreen() {
//        GraphicsDevice device =
//                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//
//        if (!isFullScreen) {
//            // Store the current window bounds so we can restore later
//            windowedBounds = getBounds();
//
//            // Dispose so we can change undecorated status
//            dispose();
//            setUndecorated(true);
//            setVisible(true);
//
//            // Request the device to use this frame as fullscreen
//            device.setFullScreenWindow(this);
//
//            isFullScreen = true;
//        } else {
//            // Exit fullscreen
//            device.setFullScreenWindow(null);
//
//            // Return to windowed mode
//            dispose();
//            setUndecorated(false);
//            // Restore previous window bounds
//            if (windowedBounds != null) {
//                setBounds(windowedBounds);
//            }
//            setVisible(true);
//
//            isFullScreen = false;
//        }
//    }
//    /**
//     * Creates the bubble panel at (950,100), places a queen icon + text label,
//     * loads a list of facts, and starts rotating them every 5 seconds.
//     */
//
//
//    /**
//     * Creates a rounded bubble panel at (980,100) sized 320x280,
//     * loads facts, and starts rotating them every 7 seconds.
//     */
//    private void addFactBubble() {
//        // 1) Create the bubble panel
//        factBubblePanel = new ChatBubblePanel();
//        factBubblePanel.setBounds(950, 200, 350, 120);
//        getContentPane().add(factBubblePanel);
//
//        // 2) Add only a text label (no icon)
//        factTextLabel = new JLabel();
//        factTextLabel.setBounds(20, 20, 280, 240);
//        factTextLabel.setForeground(Color.BLACK);
//        factTextLabel.setFont(new Font("Arial", Font.PLAIN, 16));
//        factTextLabel.setVerticalAlignment(SwingConstants.TOP);
//        factBubblePanel.add(factTextLabel);
//
//        // 3) Load facts & start rotation
//        loadFacts();
//        startFactRotation(7000); // rotate every 7 seconds
//    }
//
//
//
//    public class ChatBubblePanel extends JPanel {
//        private static final int ARC = 20;
//
//        public ChatBubblePanel() {
//            setOpaque(false);
//            setLayout(null);
//        }
//
//        @Override
//        protected void paintComponent(Graphics g) {
//            Graphics2D g2 = (Graphics2D) g.create();
//            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//            int width = getWidth();
//            int height = getHeight();
//
//            // Just a simple rounded rectangle that fills the entire panel
//            RoundRectangle2D bubble = new RoundRectangle2D.Float(
//                    0, 0,
//                    width, height,
//                    ARC, ARC
//            );
//
//            g2.setColor(Color.WHITE);
//            g2.fill(bubble);
//
//            g2.dispose();
//            super.paintComponent(g);
//        }
//    }
//
//
//
//    /**
//     * Hard-coded list of 40 chess facts (one per line).
//     * If you prefer loading from a file, see the note below.
//     */
//    private void loadFacts() {
//        facts = new ArrayList<>();
//        facts.add("The number of possible unique chess games is greater than the number of electrons in the universe.");
//        facts.add("The longest chess game theoretically possible is 5,949 moves.");
//        facts.add("The longest time for a castling move was recorded in a match in 1966.");
//        facts.add("As late as 1561, castling was two moves.");
//        facts.add("The word 'Checkmate' in chess comes from the Persian phrase 'Shah Mat' meaning 'the King is dead.'");
//        facts.add("Blathy, Otto is credited for creating the longest chess problem, mate in 290 moves.");
//        facts.add("The police once raided a chess tournament in Cleveland on charges related to gambling.");
//        facts.add("The number of possibilities in a knight's tour is over 122 million.");
//        facts.add("The longest official chess game lasted 269 moves and ended in a draw.");
//        facts.add("From the starting position, there are 8 ways to mate in two moves and 355 ways to mate in three moves.");
//        facts.add("The new pawn move of advancing two squares was introduced in Spain in 1280.");
//        facts.add("Emanuel Lasker held the World Chess Champion title longer than anyone else: 27 years.");
//        facts.add("In 1985, Garry Kasparov became the youngest World Chess Champion at age 22.");
//        facts.add("The first chessboard with alternating squares appeared in Europe in 1090.");
//        facts.add("During World War II, some top chess players were also code breakers.");
//        facts.add("The first mechanical chess clock was invented in 1883.");
//        facts.add("The folding chess board was invented in 1125 by a chess-playing priest.");
//        facts.add("The worst performance by a player was recorded in 1889 at the New York double-round robin.");
//        facts.add("Frank Marshall was the first American to defeat a Soviet player in an international tournament in 1924.");
//        facts.add("In 1985, Eric Knoppert played 500 games of 10-minute chess in 68 hours.");
//        facts.add("Albert Einstein was a friend of Emanuel Lasker and took up chess in his later years.");
//        facts.add("There were 72 consecutive queen moves in an 1882 chess game in London.");
//        facts.add("A record of 100 moves without a capture was set in a 1992 match.");
//        facts.add("Rookies in chess are sometimes humorously named after the rook piece.");
//        facts.add("A computer program named Deep Thought beat an international grandmaster for the first time in 1988.");
//        facts.add("Blindfold chess is impressive; one record was 52 simultaneous blindfold games.");
//        facts.add("There are well over 1,000 different openings in chess.");
//        facts.add("Chess is often cited as an effective way to improve memory and problem-solving skills.");
//        facts.add("FIDE stands for Fédération Internationale des Échecs, the World Chess Federation.");
//        facts.add("The second book ever printed in English was about chess!");
//        facts.add("The first computer chess program was developed in 1951 by Alan Turing.");
//        facts.add("The oldest recorded chess game is from the 900s, between a historian and his student.");
//        facts.add("The oldest surviving complete chess sets were found on the Isle of Lewis, dating to the 12th century.");
//        facts.add("About 600 million people worldwide know how to play chess.");
//        facts.add("In German and Spanish, the pawn is called a peasant or farmer.");
//        facts.add("Before reaching Europe, chess passed through the Islamic world, which avoided figurative pieces.");
//        facts.add("Chess began in India during the Gupta Empire, then spread to Persia and beyond.");
//        facts.add("Initially, the queen could only move one square diagonally until her movement was expanded in Spain.");
//        facts.add("In Shatranj, the predecessor to chess, the queen was a minister or vizier.");
//        facts.add("'Checkmate' means the king is under immediate threat of capture with no escape!");
//    }
//
//    /**
//     * Creates a Swing Timer that updates the fact label every `delayMs` milliseconds.
//     */
//    private void startFactRotation(int delayMs) {
//        factTimer = new Timer(delayMs, new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // Move to the next fact
//                factIndex = (factIndex + 1) % facts.size();
//                // Wrap text in HTML so it can line-wrap
//                String currentFact = "<html>" + facts.get(factIndex) + "</html>";
//                factTextLabel.setText(currentFact);
//            }
//        });
//        factTimer.start();
//
//        // Show the first fact immediately
//        if (!facts.isEmpty()) {
//            factTextLabel.setText("<html>" + facts.get(0) + "</html>");
//        }
//    }
//}
