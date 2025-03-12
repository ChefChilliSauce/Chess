package com.ChilliSauce;

import javax.swing.*;
import java.awt.*;

public class HostGameFrame extends JFrame {
    private String hostCode;

    public HostGameFrame(String hostCode) {
        this.hostCode = hostCode;
        setTitle("Hosting Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel label = new JLabel("Your Host Code: " + hostCode, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 28));
        add(label, BorderLayout.CENTER);

        // NEW: Here you would also start your server socket thread (stubbed for now)
        // new Thread(() -> OnlineGameHost.startServer(hostCode)).start();

        setVisible(true);
    }
}
