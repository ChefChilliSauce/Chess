package com.ChilliSauce;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class AlternateChessGUI extends JFrame {
    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = TILE_SIZE * 8;  // e.g. 640 for an 8×8 board

    // Chess board panel + logic
    private final JPanel chessBoardPanel;
    private final Board board;

    // Piece-dragging
    private int selectedPieceIndex = -1;
    private int draggedX, draggedY;
    private boolean dragging = false;
    private List<Integer> validMoves = new ArrayList<>();

    // Move highlights
    private int fromIndex = -1;
    private int toIndex = -1;


    // Game Termination
    private final GameTermination termination = new GameTermination();

    // Clock Variables (10+5)
    private final Timer whiteTimer, blackTimer, idleTimer;
    private int whiteTime = 600; // 10 min in seconds
    private int blackTime = 600; // 10 min in seconds
    private boolean isWhiteTurn = true;
    private boolean firstMoveDone = false;
    private final JLabel whiteTimeLabel, blackTimeLabel;

    // Fullscreen toggle
    private boolean isFullScreen = false;
    private Rectangle windowedBounds = null;

    // Score fields
    private final String blackPlayerName;
    private final String whitePlayerName;


    // Clock panel position & size (hard-coded for simplicity)
    private static final int CLOCK_X = 1240;
    private static final int CLOCK_Y = 335;
    private static final int CLOCK_W = 150;
    private static final int CLOCK_H = 200;

    public AlternateChessGUI(Board board,
                             String playerOneName,
                             String playerTwoName) {
        this.board = board;

        // Store player names for use in the labels
        this.whitePlayerName = playerOneName;
        this.blackPlayerName = playerTwoName;

        // Basic window setup
        getContentPane().setBackground(Color.decode("#123524"));
        setTitle("Chess: " + playerOneName + " vs " + playerTwoName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setLayout(null);

        // Optionally set a window icon from /assets/chess_logo.png
        java.net.URL logoURL = getClass().getResource("/assets/chess_logo.png");
        if (logoURL != null) {
            ImageIcon logoIcon = new ImageIcon(logoURL);
            setIconImage(logoIcon.getImage());
        } else {
            System.err.println("Chess logo not found at /assets/chess_logo.png");
        }
        setVisible(true);

        // 1) Clock Panel
        JPanel clockPanel = new JPanel(new GridBagLayout());
        clockPanel.setBackground(Color.decode("#123524"));
        clockPanel.setBounds(CLOCK_X, CLOCK_Y, CLOCK_W, CLOCK_H);

        whiteTimeLabel = new JLabel("10:00", SwingConstants.CENTER);
        blackTimeLabel = new JLabel("10:00", SwingConstants.CENTER);
        whiteTimeLabel.setFont(new Font("Arial", Font.BOLD, 45));
        blackTimeLabel.setFont(new Font("Arial", Font.BOLD, 45));
        whiteTimeLabel.setForeground(Color.WHITE);
        blackTimeLabel.setForeground(Color.LIGHT_GRAY);

        JLabel divider = new JLabel("———", SwingConstants.CENTER);
        divider.setFont(new Font("Arial", Font.BOLD, 20));
        divider.setForeground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        clockPanel.add(blackTimeLabel, gbc);
        gbc.gridy = 1;
        clockPanel.add(divider, gbc);
        gbc.gridy = 2;
        clockPanel.add(whiteTimeLabel, gbc);

        getContentPane().add(clockPanel);

        // 2) Chess Board Panel (centered)
        chessBoardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawPieces(g);
            }
        };
        chessBoardPanel.setLayout(null);
        chessBoardPanel.setBackground(Color.decode("#123524"));

        // Center the board once at startup
        int centerX = (getWidth() - BOARD_SIZE) / 2;
        int centerY = (getHeight() - BOARD_SIZE) / 2;
        chessBoardPanel.setBounds(centerX, centerY, BOARD_SIZE, BOARD_SIZE);
        getContentPane().add(chessBoardPanel);

        // Re-center the board if the window is resized
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                repositionChessBoard();
            }
        });

        // 3) Four Buttons on the left
        addButtons();

        // 4) Player labels – display only the names.
        // Black player's label is positioned just above the clock,
        // White player's label is positioned just below the clock.
        JLabel blackPlayerLabel = new JLabel(blackPlayerName, SwingConstants.CENTER);
        blackPlayerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        blackPlayerLabel.setForeground(Color.WHITE);
        blackPlayerLabel.setBounds(CLOCK_X, CLOCK_Y - 35, CLOCK_W, 30);
        getContentPane().add(blackPlayerLabel);

        JLabel whitePlayerLabel = new JLabel(whitePlayerName, SwingConstants.CENTER);
        whitePlayerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        whitePlayerLabel.setForeground(Color.WHITE);
        whitePlayerLabel.setBounds(CLOCK_X, CLOCK_Y + CLOCK_H + 5, CLOCK_W, 30);
        getContentPane().add(whitePlayerLabel);

        // 5) Mouse Listeners for Piece Dragging
        chessBoardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int file = e.getX() / TILE_SIZE;
                int rank = e.getY() / TILE_SIZE;
                selectedPieceIndex = (7 - rank) * 8 + file; // White always at bottom

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
                if (!dragging || selectedPieceIndex == -1)
                    return;

                int file = e.getX() / TILE_SIZE;
                int rank = e.getY() / TILE_SIZE;
                int targetIndex = (7 - rank) * 8 + file;

                int piece = board.getPiece(selectedPieceIndex);
                if (piece == PieceConstants.NONE)
                    return;

                boolean moveSuccessful = board.makeMove(selectedPieceIndex, targetIndex, AlternateChessGUI.this);
                if (moveSuccessful) {
                    fromIndex = selectedPieceIndex;
                    toIndex = targetIndex;

                    // Pawn promotion check
                    if ((piece & PieceConstants.PAWN) == PieceConstants.PAWN) {
                        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);
                        int lastRank = isWhite ? 7 : 0;
                        if (targetIndex / 8 == lastRank) {
                            showPromotionPopup(targetIndex, isWhite);
                        }
                    }

                    // Clock logic
                    if (!firstMoveDone) {
                        firstMoveDone = true;
                        idleTimer.stop();
                        whiteTimer.start();
                    }
                    if (isWhiteTurn) {
                        whiteTimer.stop();
                        whiteTime += 5;  // increment
                        blackTimer.start();
                    } else {
                        blackTimer.stop();
                        blackTime += 5;
                        whiteTimer.start();
                    }
                    isWhiteTurn = !isWhiteTurn;

                    repaintBoard();
                    // Check for checkmate and stalemate after a successful move.
                    checkForCheckmate();
                    checkForStalemate();
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

        // 6) Timers Setup
        whiteTimer = new Timer(1000, _ -> {
            if (whiteTime > 0) {
                whiteTime--;
                updateClockDisplay();
                if (whiteTime == 0) {
                    handleTimeLoss(true);
                }
            }
        });
        blackTimer = new Timer(1000, _ -> {
            if (blackTime > 0) {
                blackTime--;
                updateClockDisplay();
                if (blackTime == 0) {
                    handleTimeLoss(false);
                }
            }
        });
        idleTimer = new Timer(20_000, _ -> {
            if (!firstMoveDone) {
                firstMoveDone = true;
                whiteTimer.start();
            }
        });
        idleTimer.setRepeats(false);
        idleTimer.start();
        updateClockDisplay();

        // Show the frame fully
        setVisible(true);
    }

    // Only re-center the chessboard if the window is resized
    private void repositionChessBoard() {
        int frameWidth = getWidth();
        int frameHeight = getHeight();
        int centerX = (frameWidth - BOARD_SIZE) / 2;
        int centerY = (frameHeight - BOARD_SIZE) / 2;
        chessBoardPanel.setBounds(centerX, centerY, BOARD_SIZE, BOARD_SIZE);
        repaintBoard();
    }

    /**
     * Repaint the chessBoardPanel on the Swing event thread.
     */
    public void repaintBoard() {
        SwingUtilities.invokeLater(chessBoardPanel::repaint);
    }

    /**
     * Show the promotion popup for a pawn that reaches the last rank.
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

            button.addActionListener(_ -> {
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
     * Draw the chessboard squares + highlights.
     */
    private void drawBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (int screenRow = 0; screenRow < 8; screenRow++) {
            for (int screenCol = 0; screenCol < 8; screenCol++) {
                int x = screenCol * TILE_SIZE;
                int y = screenRow * TILE_SIZE;

                // Checker pattern
                if ((screenRow + screenCol) % 2 == 0) {
                    g2d.setColor(Color.decode("#e0c8b0")); // Light square
                } else {
                    g2d.setColor(Color.decode("#a16f5a")); // Dark square
                }
                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                int boardRow = 7 - screenRow;
                int index = boardRow * 8 + screenCol;

                // Last move highlight
                if (index == fromIndex || index == toIndex) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }

                // Valid moves highlight
                if (validMoves.contains(index)) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
            }
        }
    }

    /**
     * Draw the chess pieces. If one is being dragged, draw it under the mouse
     * and skip drawing it in its original square.
     */
    private void drawPieces(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // 1) Draw all non-dragged pieces
        for (int index = 0; index < 64; index++) {
            if (dragging && index == selectedPieceIndex)
                continue;

            int piece = board.getPiece(index);
            if (piece == PieceConstants.NONE)
                continue;

            int row = index / 8;
            int col = index % 8;
            int x = col * TILE_SIZE;
            int y = (7 - row) * TILE_SIZE;

            Image pieceImage = PieceConstants.getPieceImage(piece);
            if (pieceImage != null) {
                g2d.drawImage(pieceImage, x, y, TILE_SIZE, TILE_SIZE, this);
            }
        }

        // 2) If dragging, draw that piece at the mouse location
        if (dragging && selectedPieceIndex != -1) {
            int piece = board.getPiece(selectedPieceIndex);
            if (piece != PieceConstants.NONE) {
                Image pieceImage = PieceConstants.getPieceImage(piece);
                if (pieceImage != null) {
                    int offset = TILE_SIZE / 2;
                    g2d.drawImage(pieceImage, draggedX - offset, draggedY - offset,
                            TILE_SIZE, TILE_SIZE, this);
                }
            }
        }
    }



    /**
     * (Currently not used) Update the clock labels.
     */
    private void updateClockDisplay() {
        whiteTimeLabel.setText(formatTime(whiteTime));
        blackTimeLabel.setText(formatTime(blackTime));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Toggle Fullscreen on/off.
     */
    private void toggleFullScreen() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (!isFullScreen) {
            windowedBounds = getBounds();
            dispose();
            setUndecorated(true);
            setVisible(true);
            device.setFullScreenWindow(this);
            isFullScreen = true;
        } else {
            device.setFullScreenWindow(null);
            dispose();
            setUndecorated(false);
            if (windowedBounds != null) {
                setBounds(windowedBounds);
            }
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setVisible(true);
            isFullScreen = false;
        }
    }

    /**
     * Create the UI buttons (resign, fullscreen, plus 2 placeholders).
     */
    private void addButtons() {
        java.net.URL maximize = getClass().getResource("/assets/maximize.png");
        java.net.URL resign = getClass().getResource("/assets/resign.png");
        java.net.URL draw = getClass().getResource("/assets/draw.png");
        java.net.URL engine = getClass().getResource("/assets/engine.png");

        // Resign button
        if (resign != null) {
            ImageIcon icon = new ImageIcon(resign);
            Image scaled = icon.getImage().getScaledInstance(80, 40, Image.SCALE_AREA_AVERAGING);
            ImageIcon scaledIcon = new ImageIcon(scaled);

            JButton resignButton = new JButton(scaledIcon);
            resignButton.setToolTipText("Resign");
            styleButton(resignButton);
            resignButton.setBounds(100, 390, 80, 40);
            resignButton.addActionListener(_ -> handleResignation());
            getContentPane().add(resignButton);
        } else {
            System.err.println("resign.png icon not found!");
        }

        // Draw button
        if (draw != null) {
            ImageIcon icon = new ImageIcon(draw);
            Image scaled = icon.getImage().getScaledInstance(80, 40, Image.SCALE_AREA_AVERAGING);
            ImageIcon scaledIcon = new ImageIcon(scaled);
            JButton drawButton = new JButton(scaledIcon);
            drawButton.setToolTipText("Draw");
            styleButton(drawButton);
            drawButton.setBounds(200, 390, 80, 40);
            drawButton.addActionListener(_ -> handleDraw());
            getContentPane().add(drawButton);
        } else {
            System.err.println("draw.png icon not found!");
        }

        // Fullscreen button
        if (maximize != null) {
            ImageIcon icon = new ImageIcon(maximize);
            Image scaled = icon.getImage().getScaledInstance(80, 40, Image.SCALE_AREA_AVERAGING);
            ImageIcon scaledIcon = new ImageIcon(scaled);

            JButton maxMinButton = new JButton(scaledIcon);
            maxMinButton.setToolTipText("Fullscreen");
            styleButton(maxMinButton);
            maxMinButton.setBounds(100, 450, 80, 40);
            maxMinButton.addActionListener(_ -> toggleFullScreen());
            getContentPane().add(maxMinButton);
        } else {
            System.err.println("maximize.png icon not found!");
        }

        // Engine button (placeholder)
        if (engine != null) {
            ImageIcon icon = new ImageIcon(engine);
            Image scaled = icon.getImage().getScaledInstance(80, 40, Image.SCALE_AREA_AVERAGING);
            ImageIcon scaledIcon = new ImageIcon(scaled);
            JButton engineButton = new JButton(scaledIcon);
            engineButton.setToolTipText("Engine");
            styleButton(engineButton);
            engineButton.setBounds(200, 450, 80, 40);
            engineButton.addActionListener(_ -> JOptionPane.showMessageDialog(
                    this,
                    "Engine feature will be available soon",
                    "Engine Feature",
                    JOptionPane.INFORMATION_MESSAGE
            ));
            getContentPane().add(engineButton);
        } else {
            System.err.println("engine.png icon not found!");
        }
    }

    private void styleButton(JButton btn) {
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBackground(Color.decode("#353535"));
        btn.setForeground(Color.WHITE);
    }

    /**
     * Called when the user clicks the "Resign" button.
     */
    private void handleResignation() {
        if (isWhiteTurn) {
            termination.setWhiteResigned(true);
        } else {
            termination.setBlackResigned(true);
        }
        termination.checkGameState(board);

        // Stop the clocks
        whiteTimer.stop();
        blackTimer.stop();

        String resigningPlayer = isWhiteTurn ? whitePlayerName : blackPlayerName;
        String winningPlayer = isWhiteTurn ? blackPlayerName : whitePlayerName;
        String message = String.format("%s resigned, %s wins!\nThank you for playing!", resigningPlayer, winningPlayer);

        String[] options = { "Rematch", "Exit" };
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            System.exit(0);
        }
    }

    /**
     * Called when the user clicks the "Draw" button.
     */
    private void handleDraw() {
        termination.setDrawAgreed(true);
        termination.checkGameState(board);

        whiteTimer.stop();
        blackTimer.stop();

        String message = "The game is a draw!\nThank you for playing!";
        String[] options = { "Rematch", "Exit" };
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            System.exit(0);
        }
    }

    /**
     * Check for checkmate. If the side to move is in checkmate, show a popup
     * that indicates which player is checkmated and which player wins.
     * Then offer options for Rematch or Exit.
     */
    private void checkForCheckmate() {
        // isWhiteTurn now indicates the side to move.
        if (board.isCheckmate(isWhiteTurn)) {
            String losingPlayer = isWhiteTurn ? whitePlayerName : blackPlayerName;
            String winningPlayer = isWhiteTurn ? blackPlayerName : whitePlayerName;
            // Stop the clocks
            whiteTimer.stop();
            blackTimer.stop();
            String message = String.format("%s is checkmated. %s wins the game.\nThank you for playing!", losingPlayer, winningPlayer);
            String[] options = { "Rematch", "Exit" };
            int choice = JOptionPane.showOptionDialog(
                    this,
                    message,
                    "Game Over",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == JOptionPane.YES_OPTION) {
                startNewGame();
            } else {
                System.exit(0);
            }
        }
    }

    /**
     * Check for stalemate. If the side to move is not in check but has no legal moves,
     * then it is stalemate (draw).
     */
    private void checkForStalemate() {
        if (board.isStalemate(isWhiteTurn)) {
            // Stop the clocks
            whiteTimer.stop();
            blackTimer.stop();
            String message = "Stalemate! The game is a draw.\nThank you for playing!";
            String[] options = { "Rematch", "Exit" };
            int choice = JOptionPane.showOptionDialog(
                    this,
                    message,
                    "Game Over",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            if (choice == JOptionPane.YES_OPTION) {
                startNewGame();
            } else {
                System.exit(0);
            }
        }
    }

    /**
     * Start a new game (rematch) with the same player names and settings.
     */
    private void startNewGame() {
        dispose();
        Board newBoard = new Board();
        SwingUtilities.invokeLater(() -> new AlternateChessGUI(
                newBoard,
                whitePlayerName,
                blackPlayerName
        ));
    }

    /**
     * Handle time loss: if a player's clock reaches zero, show a popup indicating that
     * "<Player name> ran out of time. <Opponent> wins the game. Thank you for playing!"
     * with options to Rematch or Exit.
     *
     * @param whiteTimeExpired true if white's time has expired, false if black's time has expired.
     */
    private void handleTimeLoss(boolean whiteTimeExpired) {
        // Stop both clocks.
        whiteTimer.stop();
        blackTimer.stop();

        String losingPlayer, winningPlayer;
        if (whiteTimeExpired) {
            losingPlayer = whitePlayerName;
            winningPlayer = blackPlayerName;
        } else {
            losingPlayer = blackPlayerName;
            winningPlayer = whitePlayerName;
        }
        String message = String.format("%s ran out of time. %s wins the game.\nThank you for playing!", losingPlayer, winningPlayer);
        String[] options = { "Rematch", "Exit" };
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            System.exit(0);
        }
    }

    /**
     * Main method for local testing.
     */
    public static void main(String[] args) {
        Board board = new Board();
        SwingUtilities.invokeLater(() -> new AlternateChessGUI(
                board,
                "Snowy",
                "Chilli"
        ));
    }
}
