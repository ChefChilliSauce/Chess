package com.ChilliSauce;

import javax.swing.*;
import java.awt.*;

public class PassNPlaySetupDialog extends JDialog {
    private JTextField playerOneField;
    private JTextField playerTwoField;
    private JComboBox<String> startingColorCombo;
    private JCheckBox boardFlipCheck;

    public PassNPlaySetupDialog(JFrame parent) {
        super(parent, "Pass n Play Setup", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label1 = new JLabel("Player 1 Name:");
        playerOneField = new JTextField(15);
        JLabel label2 = new JLabel("Player 2 Name:");
        playerTwoField = new JTextField(15);
        JLabel label3 = new JLabel("Starting Color:");
        String[] options = {"White", "Black", "Random"};
        startingColorCombo = new JComboBox<>(options);
        JLabel label4 = new JLabel("Enable Board Flip:");
        boardFlipCheck = new JCheckBox();
        boardFlipCheck.setSelected(true);

        gbc.gridx = 0; gbc.gridy = 0;
        add(label1, gbc);
        gbc.gridx = 1;
        add(playerOneField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(label2, gbc);
        gbc.gridx = 1;
        add(playerTwoField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(label3, gbc);
        gbc.gridx = 1;
        add(startingColorCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(label4, gbc);
        gbc.gridx = 1;
        add(boardFlipCheck, gbc);

        JButton startButton = new JButton("Start Game");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        add(startButton, gbc);

        startButton.addActionListener(e -> {
            String player1 = playerOneField.getText().trim();
            String player2 = playerTwoField.getText().trim();
            if(player1.isEmpty() || player2.isEmpty()){
                JOptionPane.showMessageDialog(PassNPlaySetupDialog.this, "Please enter names for both players.");
                return;
            }
            String startingOption = (String) startingColorCombo.getSelectedItem();
            int playerOneColor, playerTwoColor;
            if("White".equals(startingOption)){
                playerOneColor = PieceConstants.WHITE;
                playerTwoColor = PieceConstants.BLACK;
            } else if("Black".equals(startingOption)){
                playerOneColor = PieceConstants.BLACK;
                playerTwoColor = PieceConstants.WHITE;
            } else { // Random
                double rand = Math.random() * 10;
                if(rand < 6){
                    playerOneColor = PieceConstants.WHITE;
                    playerTwoColor = PieceConstants.BLACK;
                } else {
                    playerOneColor = PieceConstants.BLACK;
                    playerTwoColor = PieceConstants.WHITE;
                }
            }
            boolean boardFlipEnabled = boardFlipCheck.isSelected();
            SwingUtilities.invokeLater(() -> new AlternateChessGUI(new Board(), player1, player2, playerOneColor, playerTwoColor, boardFlipEnabled));
            dispose();
        });

        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}
