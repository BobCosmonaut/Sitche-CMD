package com.company;

import java.awt.*;
import java.util.Scanner;

public class Board {

    static RuleBook ruleBook;
    static Opponent Opponent;

    static Position currentPosition;

    static boolean whitesTurn = true;

    static Scanner scanner;

    public static void main(String[] args) {
        ruleBook = new RuleBook();


        scanner = new Scanner(System.in);

        System.out.println("Welcome to Sitche Command Line!\n");

        boolean multithreading;
        while (true) {
            System.out.print("Do you want to use multithreading? (Y/N): ");
            String response = scanner.nextLine();
            if (Character.toLowerCase(response.charAt(0)) == 'y' || Character.toLowerCase(response.charAt(0)) == 'n') {
                multithreading = Character.toLowerCase(response.charAt(0)) == 'y';
                break;
            } else {
                System.out.println("Invalid!");
            }
        }
        boolean whitePlayer;
        while (true) {
            System.out.print("Do you want to play as white? (Y/N): ");
            String response = scanner.nextLine();
            if (Character.toLowerCase(response.charAt(0)) == 'y' || Character.toLowerCase(response.charAt(0)) == 'n') {
                whitePlayer = Character.toLowerCase(response.charAt(0)) == 'y';
                break;
            } else {
                System.out.println("Invalid");
            }
        }
        Opponent = new Opponent(!whitePlayer, multithreading);


        while (true) {
            int depthValue = 0;
            System.out.print("How many moves do you want the opponent to search ahead? [1-5]: ");
            try {
                depthValue = Integer.parseInt(scanner.nextLine());
                if (depthValue < 6 && depthValue > 0) {
                    Position.maxSearchDepth = depthValue;
                    break;
                }
            } catch (Exception e) {

            }
            System.out.println("Invalid!");
        }


        System.out.println("Files (columns) are numbered A-G. Ranks (rows) are numbered 1-8");
        System.out.println("Similar to chess notation, input moves like this: \"A6 E3\"");
        System.out.println("Where the first set is the file and rank to move from");
        System.out.println("and the last set is the file and rank to move to.");

        System.out.println();

        System.out.println("Pieces are represented by their characters in chess notation. (The letter that they start with, except for knights, which are shown with \"N\" or \"n\")");
        System.out.println("White pieces are uppercase, black pieces are lowercase.");

        currentPosition = new Position();

        render();

        while (true)
            playGame();
    }

    private static void playGame() {
        currentPosition.whiteToMove = whitesTurn;

        if (Opponent.IAmWhite == whitesTurn) {
            currentPosition = Opponent.makeMove(currentPosition);
        } else {
            requestMove();
        }
        render();
        whitesTurn = !whitesTurn;
    }


    static final char border = '#';

    protected static void render() {
        printSide();

        for (int i = 0; i < 8; i++) {
            System.out.print(8 - i + " " + border + " ");
            for (int j = 0; j < 8; j++) {
                System.out.print(getSymbol(currentPosition.pieces[j][7 - i]) + "  ");

            }
            System.out.print(border + " ");
            System.out.println();
        }
        printSide();
        printFile();

        System.out.println();
    }

    private static void printSide() {
        System.out.print("  ");
        for (int i = 0; i < 9 * 3; i++) {
            System.out.print(border);
        }
        System.out.println();
    }

    private static void printFile() {
        System.out.print("    ");
        for (int i = 0; i < 8; i++) {
            System.out.print((char) ('A' + i) + "  ");
        }
    }

    private static char getSymbol(byte code) {
        switch (code) {
            case 0:
                return '-';

            case -1:
                return 'p';
            case -2:
                return 'r';

            case -3:
                return 'n';

            case -4:
                return 'b';

            case -5:
                return 'q';

            case -6:
                return 'k';


            case 1:
                return 'P';
            case 2:
                return 'R';

            case 3:
                return 'N';
            case 4:
                return 'B';
            case 5:
                return 'Q';
            case 6:
                return 'K';

            default:
                return 'X';
        }
    }

    static void requestMove() {
        while (true) {
            System.out.print("Enter your move: ");
            String input = scanner.nextLine();
            if (input.length() >= 5) {
                if (input.charAt(0) >= 'A' && input.charAt(0) <= 'H' &&
                        input.charAt(1) >= '1' && input.charAt(1) <= '8' &&
                        input.charAt(3) >= 'A' && input.charAt(3) <= 'H' &&
                        input.charAt(4) >= '1' && input.charAt(4) <= '8') {

                    int fromX = input.charAt(0) - 'A';
                    int fromY = input.charAt(1) - '1';

                    int toX = input.charAt(3) - 'A';
                    int toY = input.charAt(4) - '1';

                    if (ruleBook.isMoveLegal(currentPosition, new Point(fromX, fromY), new Point(toX, toY), false) && (currentPosition.pieces[fromX][fromY] > 0 == whitesTurn)) {

                        Position attemptedPosition = new Position(currentPosition, fromX, fromY, toX, toY);
                        if (!attemptedPosition.kingIsInCheck(!Opponent.IAmWhite)) {
                            currentPosition = attemptedPosition;
                            break;
                        }
                        System.out.println("Moving into check!");
                    }
                }
            }
            System.out.println("Invalid!");
        }
    }
}