package com.ChilliSauce;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import com.ChilliSauce.GameTermination;


public class AlternateChessGUI extends JFrame {
    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = TILE_SIZE * 8;

    // Chess board panel and chess logic
    private final JPanel chessBoardPanel;
    private final Board board;

    // Piece-dragging variables
    private int selectedPieceIndex = -1;
    private int draggedX, draggedY;
    private boolean dragging = false;
    private List<Integer> validMoves = new ArrayList<>();

    // Move highlights
    private int fromIndex = -1;
    private int toIndex = -1;

    // Board orientation (force white at bottom)
    private boolean isWhiteAtBottom;

    private GameTermination termination = new GameTermination();

    // ---------------------------
    // CLOCK VARIABLES (10+5 with idle wait)
    // ---------------------------
    private Timer whiteTimer, blackTimer;
    private Timer idleTimer;   // 20-second idle timer before starting White's clock
    private int whiteTime = 600; // 10 minutes (600 seconds)
    private int blackTime = 600;
    private boolean isWhiteTurn = true;
    private boolean firstMoveDone = false; // Clock doesn't start until first move or idle expires
    private JLabel whiteTimeLabel, blackTimeLabel;

    // Fullscreen support
    private boolean isFullScreen = false;
    private Rectangle windowedBounds = null;

    /**
     * Constructor.
     * Although the parameters for player names, colors, etc. are provided,
     * they will not be used in this minimal AlternateChessGUI.
     */
    public AlternateChessGUI(Board board,
                             String playerOneName,
                             String playerTwoName,
                             int playerOneColor,
                             int playerTwoColor,
                             boolean boardFlipEnabled) {
        this.board = board;
        // Force white to always be at the bottom.
        this.isWhiteAtBottom = true;

        // Basic window setup
        getContentPane().setBackground(Color.decode("#1f1f1f"));
        setTitle("Chess: " + playerOneName + " vs " + playerTwoName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setLayout(null);

        // -----------------------------
        // 1) Create Clock Panel
        // -----------------------------
        JPanel clockPanel = new JPanel();
        clockPanel.setLayout(new GridBagLayout());
        clockPanel.setBackground(Color.decode("#1f1f1f"));
        // Position the clock panel (adjust as needed)
        clockPanel.setBounds(BOARD_SIZE + 600, 335, 150, 200);

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

        // -----------------------------
        // 2) Chess Board Panel (Centered)
        // -----------------------------
        chessBoardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawPieces(g);
            }
        };
        chessBoardPanel.setLayout(null);
        chessBoardPanel.setBackground(Color.decode("#1f1f1f"));
        // Center the board dynamically on window resize
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int centerX = (getWidth() - BOARD_SIZE) / 2;
                int centerY = (getHeight() - BOARD_SIZE) / 2;
                chessBoardPanel.setBounds(centerX, centerY, BOARD_SIZE, BOARD_SIZE);
                repaintBoard();
            }
        });
        // Initial centering
        int centerX = (getWidth() - BOARD_SIZE) / 2;
        int centerY = (getHeight() - BOARD_SIZE) / 2;
        chessBoardPanel.setBounds(centerX, centerY, BOARD_SIZE, BOARD_SIZE);
        getContentPane().add(chessBoardPanel);

        // -----------------------------
        // 3) Add Buttons (at specified positions)
        // -----------------------------
        addButtons();

        // -----------------------------
        // 4) Mouse Listeners for Piece Dragging
        // -----------------------------
        chessBoardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int file = e.getX() / TILE_SIZE;
                int rank = e.getY() / TILE_SIZE;
                selectedPieceIndex = (7 - rank) * 8 + file; // white always at bottom

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
                int targetIndex = (7 - rank) * 8 + file;

                int piece = board.getPiece(selectedPieceIndex);
                if (piece == PieceConstants.NONE) return;

                boolean moveSuccessful = board.makeMove(selectedPieceIndex, targetIndex, AlternateChessGUI.this);
                if (moveSuccessful) {
                    fromIndex = selectedPieceIndex;
                    toIndex = targetIndex;

                    // Pawn promotion check
                    if ((piece & 7) == PieceConstants.PAWN) {
                        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);
                        int lastRank = isWhite ? 7 : 0;
                        if (targetIndex / 8 == lastRank) {
                            showPromotionPopup(targetIndex, isWhite);
                        }
                    }

                    // Force white to remain at bottom
                    isWhiteAtBottom = true;

                    // ---------------------------
                    // Clock Logic:
                    // If this is the first move, stop the idle timer and start White's clock.
                    // Otherwise, switch clocks with a +5 seconds increment.
                    // ---------------------------
                    if (!firstMoveDone) {
                        firstMoveDone = true;
                        idleTimer.stop();
                        whiteTimer.start();
                    }
                    if (isWhiteTurn) {
                        whiteTimer.stop();
                        whiteTime += 5;  // Add 5 seconds increment
                        blackTimer.start();
                    } else {
                        blackTimer.stop();
                        blackTime += 5;
                        whiteTimer.start();
                    }
                    isWhiteTurn = !isWhiteTurn;

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
        // 5) Set up Timers (Clock)
        // -----------------------------
        whiteTimer = new Timer(1000, e -> {
            if (whiteTime > 0) {
                whiteTime--;
                updateClockDisplay();
            }
        });
        blackTimer = new Timer(1000, e -> {
            if (blackTime > 0) {
                blackTime--;
                updateClockDisplay();
            }
        });

        // Idle timer: if no move is made in 20 seconds, start White's clock automatically.
        idleTimer = new Timer(20_000, e -> {
            if (!firstMoveDone) {
                firstMoveDone = true;
                whiteTimer.start();
            }
        });
        idleTimer.setRepeats(false);
        idleTimer.start();

        updateClockDisplay();
        setVisible(true);
    }

    // Repaint the board panel
    public void repaintBoard() {
        SwingUtilities.invokeLater(chessBoardPanel::repaint);
    }

    // Show Promotion Popup
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

    // Draw the Board (tiles and highlights)
    private void drawBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (int screenRow = 0; screenRow < 8; screenRow++) {
            for (int screenCol = 0; screenCol < 8; screenCol++) {
                int x = screenCol * TILE_SIZE;
                int y = screenRow * TILE_SIZE;

                // Alternate square colors
                if ((screenRow + screenCol) % 2 == 0) {
                    g2d.setColor(Color.decode("#e0c8b0"));
                } else {
                    g2d.setColor(Color.decode("#a16f5a"));
                }
                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                // Highlight last move
                int boardRow = 7 - screenRow;
                int boardCol = screenCol;
                int index = boardRow * 8 + boardCol;
                boolean isLastMove = (index == fromIndex || index == toIndex);
                if (isLastMove) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }

                // Highlight valid moves
                if (validMoves.contains(index)) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
            }
        }
    }

    // Draw the Pieces (with dragging visualization)
    private void drawPieces(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Draw all non-dragged pieces
        for (int index = 0; index < 64; index++) {
            if (dragging && index == selectedPieceIndex) continue;
            int piece = board.getPiece(index);
            if (piece == PieceConstants.NONE) continue;

            int row = index / 8;
            int col = index % 8;
            int x = col * TILE_SIZE;
            int y = (7 - row) * TILE_SIZE;

            Image pieceImage = PieceConstants.getPieceImage(piece);
            if (pieceImage != null) {
                g2d.drawImage(pieceImage, x, y, TILE_SIZE, TILE_SIZE, this);
            }
        }

        // Draw the dragged piece at the mouse position
        if (dragging && selectedPieceIndex != -1) {
            int piece = board.getPiece(selectedPieceIndex);
            if (piece != PieceConstants.NONE) {
                Image pieceImage = PieceConstants.getPieceImage(piece);
                if (pieceImage != null) {
                    int offset = TILE_SIZE / 2;
                    g2d.drawImage(pieceImage, draggedX - offset, draggedY - offset, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }
    }

    // Clock Utility: Update clock display
    private void updateClockDisplay() {
        whiteTimeLabel.setText(formatTime(whiteTime));
        blackTimeLabel.setText(formatTime(blackTime));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Fullscreen Toggle Functionality
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
     * Creates three buttons:
     *   1) Button at (950, 700), width=110, no functionality.
     *   2) Button at (1070, 700), width=110, no functionality.
     *   3) Button at (1190, 700), width=110, toggles true fullscreen on click.
     */
    private void addButtons() {
        java.net.URL maximize = getClass().getResource("/assets/maximize.png");
        java.net.URL resign = getClass().getResource("/assets/resign.png");


        if (resign != null) {
            ImageIcon icon = new ImageIcon(resign);
            Image scaled = icon.getImage().getScaledInstance(80, 40, Image.SCALE_AREA_AVERAGING);
            ImageIcon scaledIcon = new ImageIcon(scaled);
            JButton resignButton = new JButton(scaledIcon);
            resignButton.setToolTipText("Resign");
            resignButton.setBorderPainted(false);
            resignButton.setOpaque(true);
            resignButton.setBackground(Color.decode("#353535"));
            resignButton.setBounds(115, 425, 80, 40);
            resignButton.addActionListener(e -> handleResignation());
            getContentPane().add(resignButton);
        }
        else {
            System.err.println("Icon not found!");
        }

        JButton button1 = new JButton("Button 1");
        button1.setBounds(115, 375, 80, 40);
        button1.setBackground(Color.decode("#353535"));
        button1.setForeground(Color.WHITE);
        button1.setOpaque(true);
        button1.setBorderPainted(false);
        getContentPane().add(button1);

        JButton button2 = new JButton("Button 2");
        button2.setBounds(200, 375, 80, 40);
        button2.setBackground(Color.decode("#353535"));
        button2.setForeground(Color.WHITE);
        button2.setOpaque(true);
        button2.setBorderPainted(false);
        getContentPane().add(button2);

        if (maximize != null) {
            ImageIcon icon = new ImageIcon(maximize);
            Image scaled = icon.getImage().getScaledInstance(80, 40, Image.SCALE_AREA_AVERAGING);
            ImageIcon scaledIcon = new ImageIcon(scaled);
            JButton maxMinButton = new JButton(scaledIcon);
            maxMinButton.setToolTipText("Fullscreen");
            maxMinButton.setBorderPainted(false);
            maxMinButton.setOpaque(true);
            maxMinButton.setBackground(Color.decode("#353535"));
            maxMinButton.setBounds(115, 425, 80, 40);
            maxMinButton.addActionListener(e -> toggleFullScreen());
            getContentPane().add(maxMinButton);
        }
        else {
            System.err.println("Icon not found!");
        }

        JButton button4 = new JButton("Button 4");
        button4.setBounds(200, 425, 80, 40);
        button4.setBackground(Color.decode("#353535"));
        button4.setForeground(Color.WHITE);
        button4.setOpaque(true);
        button4.setBorderPainted(false);
        getContentPane().add(button4);
    }

    private void handleResignation() {
        // Mark resignation based on whose turn it is.
        if (isWhiteTurn) {
            termination.setWhiteResigned(true);
        } else {
            termination.setBlackResigned(true);
        }

        // Get the final game result from GameTermination.
        GameTermination.GameResult result = termination.checkGameState(board);

        // Stop the clocks (if running)
        whiteTimer.stop();
        blackTimer.stop();

        // Display the game result in a popup.
        JOptionPane.showMessageDialog(
                this,
                "Game Over: " + result.toString() + "\nThank you for playing!",
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE
        );
    }



    // Main method for local testing
    public static void main(String[] args) {
        Board board = new Board();
        SwingUtilities.invokeLater(() -> new AlternateChessGUI(board,
                "Alice",
                "Bob",
                PieceConstants.WHITE,
                PieceConstants.BLACK,
                true));
    }
}
