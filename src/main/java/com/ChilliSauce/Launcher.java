package com.ChilliSauce;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Launcher extends JFrame {

    public Launcher() {
        setTitle("Chess Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);  // Center on screen
        setLayout(new GridBagLayout());

        // Create buttons for the three modes
        JButton passNPlayButton = new JButton("Pass n Play");
        JButton hostButton = new JButton("Host");
        JButton joinGameButton = new JButton("Join Game");

        // Use GridBagLayout for vertical arrangement
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(passNPlayButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(hostButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(joinGameButton, gbc);

        // When "Pass n Play" is clicked, ask for the names and start AlternateChessGUI.
        passNPlayButton.addActionListener((ActionEvent e) -> {
            String whiteName = JOptionPane.showInputDialog(this, "Enter White's name:");
            if (whiteName == null || whiteName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "White's name is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String blackName = JOptionPane.showInputDialog(this, "Enter Black's name:");
            if (blackName == null || blackName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Black's name is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Launch AlternateChessGUI with the entered names.
            SwingUtilities.invokeLater(() -> new AlternateChessGUI(
                    new Board(),
                    whiteName.trim(),
                    blackName.trim(),
                    PieceConstants.WHITE,
                    PieceConstants.BLACK,
                    true));
            dispose();
        });

        // Host and Join (stubbed for now)
        hostButton.addActionListener(e -> {
            String hostCode = String.valueOf(100000 + (int)(Math.random()*900000));
            SwingUtilities.invokeLater(() -> new HostGameFrame(hostCode));
            dispose();
        });

        joinGameButton.addActionListener(e -> {
            String code = JOptionPane.showInputDialog(this, "Enter Host Code:");
            if (code != null && !code.trim().isEmpty()) {
                SwingUtilities.invokeLater(() -> new JoinGameFrame(code.trim()));
                dispose();
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Launcher::new);
    }
}
