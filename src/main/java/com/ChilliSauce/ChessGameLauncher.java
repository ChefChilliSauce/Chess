//package com.ChilliSauce;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//public class ChessGameLauncher extends JFrame {
//    public ChessGameLauncher() {
//        setTitle("Chess Game Launcher");
//        setSize(400, 300);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLocationRelativeTo(null);
//        setLayout(new GridLayout(4, 1));
//
//        JLabel titleLabel = new JLabel("Select Game Mode", SwingConstants.CENTER);
//        add(titleLabel);
//
//        JButton passPlayButton = new JButton("Pass n Play");
//        JButton createGameButton = new JButton("Create Online Game");
//        JButton joinGameButton = new JButton("Join Online Game");
//
//        passPlayButton.addActionListener(e -> showPassPlayDialog());
//        createGameButton.addActionListener(e -> showCreateGameDialog());
//        joinGameButton.addActionListener(e -> showJoinGameDialog());
//
//        add(passPlayButton);
//        add(createGameButton);
//        add(joinGameButton);
//    }
//
//    private void showPassPlayDialog() {
//        String whitePlayer = JOptionPane.showInputDialog(this, "Enter White Player Name:");
//        String blackPlayer = JOptionPane.showInputDialog(this, "Enter Black Player Name:");
//
//        if (whitePlayer != null && blackPlayer != null) {
//            new ChessGUI(whitePlayer, blackPlayer);
//        }
//    }
//
//    private void showCreateGameDialog() {
//        String hostName = JOptionPane.showInputDialog(this, "Enter Host Name:");
//        if (hostName == null || hostName.trim().isEmpty()) return;
//
//        String[] options = {"White", "Black"};
//        String choice = (String) JOptionPane.showInputDialog(this, "Choose your color:", "Create Game",
//                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
//
//        if (choice != null) {
//            new ChessServer(hostName, choice.toLowerCase());
//        }
//    }
//
//    private void showJoinGameDialog() {
//        String playerName = JOptionPane.showInputDialog(this, "Enter Display Name:");
//        String gameCode = JOptionPane.showInputDialog(this, "Enter Game Code:");
//
//        if (playerName != null && gameCode != null) {
//            new ChessClient(playerName, gameCode);
//        }
//    }
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            ChessGameLauncher launcher = new ChessGameLauncher();
//            launcher.setVisible(true);
//        });
//    }
//
//    // Placeholder classes for networking logic
//    class ChessServer {
//        public ChessServer(String hostName, String color) {
//            String gameCode = generateGameCode();
//            JOptionPane.showMessageDialog(null, "Game created by " + hostName + ". Code: " + gameCode + "\nHost is playing as " + color);
//            // Implement actual socket communication logic
//        }
//
//        private String generateGameCode() {
//            int code = (int) (Math.random() * 900000) + 100000;
//            return String.valueOf(code);
//        }
//    }
//
//    class ChessClient {
//        public ChessClient(String playerName, String gameCode) {
//            JOptionPane.showMessageDialog(null, playerName + " is joining game with code: " + gameCode);
//            //  Implement client-side socket connection
//        }
//    }
//}