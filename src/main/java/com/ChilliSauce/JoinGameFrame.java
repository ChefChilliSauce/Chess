package com.ChilliSauce;

import javax.swing.*;
import java.awt.*;

public class JoinGameFrame extends JFrame {
    private String hostCode;

    public JoinGameFrame(String hostCode) {
        this.hostCode = hostCode;
        setTitle("Join Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel label = new JLabel("Attempting to join game with code: " + hostCode, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        add(label, BorderLayout.CENTER);

        // NEW: Here you would also start a client thread to connect to the host
        // new Thread(() -> OnlineGameClient.joinGame(hostCode)).start();

        setVisible(true);
    }
}
