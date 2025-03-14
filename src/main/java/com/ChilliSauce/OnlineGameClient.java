//package com.ChilliSauce;
//
//import java.io.*;
//import java.net.*;
//
//public class OnlineGameClient {
//    private Socket socket;
//    private PrintWriter out;
//    private BufferedReader in;
//    private AlternateChessGUI gui; // Reference to the GUI to update moves
//
//    public OnlineGameClient(Socket socket, AlternateChessGUI gui) throws IOException {
//        this.socket = socket;
//        this.gui = gui;
//        out = new PrintWriter(socket.getOutputStream(), true);
//        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//        // Start a thread to listen for incoming messages
//        new Thread(() -> {
//            try {
//                String line;
//                while ((line = in.readLine()) != null) {
//                    processMessage(line);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
//
//    // Send a move in the format "MOVE fromIndex toIndex"
//    public void sendMove(int fromIndex, int toIndex) {
//        out.println("MOVE " + fromIndex + " " + toIndex);
//    }
//
//    // Send a resignation message
//    public void sendResign() {
//        out.println("RESIGN");
//    }
//
//    private void processMessage(String message) {
//        // Basic protocol: "MOVE from to" or "RESIGN"
//        String[] parts = message.split(" ");
//        if (parts.length > 0) {
//            switch (parts[0]) {
//                case "MOVE":
//                    if (parts.length == 3) {
//                        try {
//                            int from = Integer.parseInt(parts[1]);
//                            int to = Integer.parseInt(parts[2]);
//                            gui.receiveRemoteMove(from, to);
//                        } catch (NumberFormatException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    break;
//                case "RESIGN":
//                    gui.receiveRemoteResign();
//                    break;
//                // Add more message types as needed.
//            }
//        }
//    }
//}