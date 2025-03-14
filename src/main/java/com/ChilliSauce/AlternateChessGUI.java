package com.ChilliSauce;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class AlternateChessGUI extends JFrame {
    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = TILE_SIZE * 8;

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

    // Force White at bottom
    private boolean isWhiteAtBottom = true;

    // Game Termination
    private GameTermination termination = new GameTermination();

    // Clock Variables (10+5)
    private Timer whiteTimer, blackTimer;
    private Timer idleTimer;
    private int whiteTime = 600; // 10 min
    private int blackTime = 600; // 10 min
    private boolean isWhiteTurn = true;
    private boolean firstMoveDone = false;
    private JLabel whiteTimeLabel, blackTimeLabel;

    // Fullscreen toggle
    private boolean isFullScreen = false;
    private Rectangle windowedBounds = null;

    // (A) Captured Pieces UI
    private JLabel blackPlayerLabel, whitePlayerLabel;
    private JPanel blackCapturedPanel, whiteCapturedPanel; // flow layout for icons
    private JLabel blackScoreLabel, whiteScoreLabel;       // show the numeric score

    // Track captured pieces + scores
    private int blackScore = 0;
    private int whiteScore = 0;

    public AlternateChessGUI(Board board,
                             String playerOneName,
                             String playerTwoName,
                             int playerOneColor,
                             int playerTwoColor,
                             boolean boardFlipEnabled) {
        this.board = board;

        // Basic window setup
        getContentPane().setBackground(Color.decode("#1f1f1f"));
        setTitle("Chess: " + playerOneName + " vs " + playerTwoName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setLayout(null);

        // 1) Clock Panel
        JPanel clockPanel = new JPanel(new GridBagLayout());
        clockPanel.setBackground(Color.decode("#1f1f1f"));
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

        // 2) Chess Board Panel
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
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                repositionComponents();
            }
        });
        int centerX = (getWidth() - BOARD_SIZE) / 2;
        int centerY = (getHeight() - BOARD_SIZE) / 2;
        chessBoardPanel.setBounds(centerX, centerY, BOARD_SIZE, BOARD_SIZE);
        getContentPane().add(chessBoardPanel);

        // 3) Buttons
        addButtons();

        // 4) Mouse Listeners
        chessBoardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int file = e.getX() / TILE_SIZE;
                int rank = e.getY() / TILE_SIZE;
                selectedPieceIndex = (7 - rank) * 8 + file;

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

                    // Pawn promotion
                    if ((piece & 7) == PieceConstants.PAWN) {
                        boolean isWhite = ((piece & PieceConstants.WHITE) != 0);
                        int lastRank = isWhite ? 7 : 0;
                        if (targetIndex / 8 == lastRank) {
                            showPromotionPopup(targetIndex, isWhite);
                        }
                    }

                    // Force white at bottom
                    isWhiteAtBottom = true;

                    // Clock logic
                    if (!firstMoveDone) {
                        firstMoveDone = true;
                        idleTimer.stop();
                        whiteTimer.start();
                    }
                    if (isWhiteTurn) {
                        whiteTimer.stop();
                        whiteTime += 5;
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

        // 5) Timers
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
        idleTimer = new Timer(20_000, e -> {
            if (!firstMoveDone) {
                firstMoveDone = true;
                whiteTimer.start();
            }
        });
        idleTimer.setRepeats(false);
        idleTimer.start();
        updateClockDisplay();

        // (A) Player/Captured UI
        blackPlayerLabel = new JLabel(playerTwoName, SwingConstants.CENTER);
        blackPlayerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        blackPlayerLabel.setForeground(Color.WHITE);
        getContentPane().add(blackPlayerLabel);

        blackCapturedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        blackCapturedPanel.setOpaque(false);
        getContentPane().add(blackCapturedPanel);

        blackScoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        blackScoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        blackScoreLabel.setForeground(Color.GRAY);
        getContentPane().add(blackScoreLabel);

        whiteCapturedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        whiteCapturedPanel.setOpaque(false);
        getContentPane().add(whiteCapturedPanel);

        whiteScoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        whiteScoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        whiteScoreLabel.setForeground(Color.GRAY);
        getContentPane().add(whiteScoreLabel);

        whitePlayerLabel = new JLabel(playerOneName, SwingConstants.CENTER);
        whitePlayerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        whitePlayerLabel.setForeground(Color.WHITE);
        getContentPane().add(whitePlayerLabel);

        repositionComponents();
        setVisible(true);
    }

    // (B) Called by Board when a piece is captured
    public void onPieceCaptured(int capturedPiece, boolean capturedByWhite) {
        // Increase the capturing side's score
        int value = getPieceValue(capturedPiece);
        if (capturedByWhite) {
            whiteScore += value;
            // Add small icon to whiteCapturedPanel
            whiteCapturedPanel.add(new JLabel(getCapturedIcon(capturedPiece)));
        } else {
            blackScore += value;
            blackCapturedPanel.add(new JLabel(getCapturedIcon(capturedPiece)));
        }
        // Update the score labels
        blackScoreLabel.setText("Score: " + blackScore);
        whiteScoreLabel.setText("Score: " + whiteScore);

        // Refresh UI
        whiteCapturedPanel.revalidate();
        whiteCapturedPanel.repaint();
        blackCapturedPanel.revalidate();
        blackCapturedPanel.repaint();
    }

    private int getPieceValue(int piece) {
        switch (piece & 7) {
            case PieceConstants.QUEEN:  return 9;
            case PieceConstants.ROOK:   return 5;
            case PieceConstants.BISHOP: return 3;
            case PieceConstants.KNIGHT: return 3;
            case PieceConstants.PAWN:   return 1;
            default:                    return 0;
        }
    }

    // Returns a small icon (32x32) for the captured piece
    private Icon getCapturedIcon(int piece) {
        // Example naming: /assets/cbbp.png or /assets/cbq.png etc.
        // Let's pick a naming scheme: c + color + piece letter, e.g. cwbp, cbbp, etc.
        // color = (piece & PieceConstants.WHITE) != 0 ? 'w' : 'b'
        // letter = 'p', 'r', 'n', 'b', 'q', 'k'
        char colorChar = ((piece & PieceConstants.WHITE) != 0) ? 'w' : 'b';
        char letter;
        switch (piece & 7) {
            case PieceConstants.PAWN:   letter = 'p'; break;
            case PieceConstants.ROOK:   letter = 'r'; break;
            case PieceConstants.KNIGHT: letter = 'n'; break;
            case PieceConstants.BISHOP: letter = 'b'; break;
            case PieceConstants.QUEEN:  letter = 'q'; break;
            case PieceConstants.KING:   letter = 'k'; break;
            default:                    letter = '?'; break;
        }
        String imageName = "/assets/c" + colorChar + letter + ".png"; // e.g. "/assets/cwp.png"
        java.net.URL imgURL = getClass().getResource(imageName);
        if (imgURL == null) {
            // fallback
            return new ImageIcon(); // empty icon
        }
        ImageIcon icon = new ImageIcon(imgURL);
        // scale down to 32x32
        Image scaled = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    // Re-center the board and label/panels
    private void repositionComponents() {
        int centerX = (getWidth() - BOARD_SIZE) / 2;
        int centerY = (getHeight() - BOARD_SIZE) / 2;

        chessBoardPanel.setBounds(centerX, centerY, BOARD_SIZE, BOARD_SIZE);

        // Place black (top) stuff
        blackPlayerLabel.setBounds(centerX - 300  , centerY - 69, BOARD_SIZE, 25);
        blackCapturedPanel.setBounds(centerX - 300, centerY - 40, BOARD_SIZE, 30);
        blackScoreLabel.setBounds(centerX, centerY - 10, BOARD_SIZE, 20);

        // Place white (bottom) stuff
        whitePlayerLabel.setBounds(centerX - 298  , centerY + BOARD_SIZE + 5, BOARD_SIZE, 25);
        whiteCapturedPanel.setBounds(centerX - 298 , centerY + BOARD_SIZE, BOARD_SIZE, 30);
        whiteScoreLabel.setBounds(centerX, centerY + BOARD_SIZE + 30, BOARD_SIZE, 20);


        repaintBoard();
    }

    public void repaintBoard() {
        SwingUtilities.invokeLater(chessBoardPanel::repaint);
    }

    // Promotion Popup
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

    // Draw the Board
    private void drawBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (int screenRow = 0; screenRow < 8; screenRow++) {
            for (int screenCol = 0; screenCol < 8; screenCol++) {
                int x = screenCol * TILE_SIZE;
                int y = screenRow * TILE_SIZE;

                // Checker pattern
                if ((screenRow + screenCol) % 2 == 0) {
                    g2d.setColor(Color.decode("#e0c8b0")); // Light
                } else {
                    g2d.setColor(Color.decode("#a16f5a")); // Dark
                }
                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                int boardRow = 7 - screenRow;
                int boardCol = screenCol;
                int index = boardRow * 8 + boardCol;

                // Last move highlight
                boolean isLastMove = (index == fromIndex || index == toIndex);
                if (isLastMove) {
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

    // Draw the Pieces
    private void drawPieces(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Non-dragged pieces
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

        // The dragged piece
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

    // Update clock labels
    private void updateClockDisplay() {
        whiteTimeLabel.setText(formatTime(whiteTime));
        blackTimeLabel.setText(formatTime(blackTime));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Fullscreen
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

    // Buttons (resign, etc.)
    private void addButtons() {
        java.net.URL maximize = getClass().getResource("/assets/maximize.png");
        java.net.URL resign = getClass().getResource("/assets/resign.png");

        // Example
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

        // Fullscreen
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
        } else {
            System.err.println("maximize.png icon not found!");
        }

        // Resign
        if (resign != null) {
            ImageIcon icon = new ImageIcon(resign);
            Image scaled = icon.getImage().getScaledInstance(80, 40, Image.SCALE_AREA_AVERAGING);
            ImageIcon scaledIcon = new ImageIcon(scaled);

            JButton resignButton = new JButton(scaledIcon);
            resignButton.setToolTipText("Resign");
            resignButton.setBorderPainted(false);
            resignButton.setOpaque(true);
            resignButton.setBackground(Color.decode("#353535"));
            resignButton.setBounds(200, 425, 80, 40);
            resignButton.addActionListener(e -> handleResignation());
            getContentPane().add(resignButton);
        } else {
            System.err.println("resign.png icon not found!");
        }
    }

    private void handleResignation() {
        if (isWhiteTurn) {
            termination.setWhiteResigned(true);
        } else {
            termination.setBlackResigned(true);
        }
        GameTermination.GameResult result = termination.checkGameState(board);

        whiteTimer.stop();
        blackTimer.stop();

        JOptionPane.showMessageDialog(
                this,
                "Game Over: " + result.toString() + "\nThank you for playing!",
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // Main
    public static void main(String[] args) {
        Board board = new Board();
        SwingUtilities.invokeLater(() -> new AlternateChessGUI(
                board,
                "Alice",
                "Bob",
                PieceConstants.WHITE,
                PieceConstants.BLACK,
                true
        ));
    }
}
