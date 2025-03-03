package com.ChilliSauce;

import java.util.Scanner;

public class Game {
    private Board board;
    private Player player1, player2;
    private boolean isWhiteTurn;

    public Game() {
        board = new Board();
        player1 = new Player("Player 1", "white");
        player2 = new Player("Player 2", "black");
        isWhiteTurn = true;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            // Game loop logic
            board.printBoard();
            System.out.println("Enter move: ");
            String move = scanner.nextLine();
            // Process move logic
        }
    }
}

