package test;

import main.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class TestBoard {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_MAGNETA = "\u001b[34m";

    @Test
    public void testBoard() throws InterruptedException {
        PawnInterface[][] pawnsBoard = new Pawn[12][12];

        pawnsBoard[5][0] = new Pawn( pawnsBoard, 5, 0);
        pawnsBoard[5][2] = new Pawn( pawnsBoard, 5, 2);
        pawnsBoard[7][2] = new Pawn( pawnsBoard, 7, 2);
        pawnsBoard[10][1] = new Pawn( pawnsBoard, 10, 1);
        pawnsBoard[3][5] = new Pawn( pawnsBoard, 3, 5);
        pawnsBoard[5][0] = new Pawn( pawnsBoard, 5, 0);
        pawnsBoard[5][7] = new Pawn( pawnsBoard, 5, 7);
        pawnsBoard[5][9] = new Pawn( pawnsBoard, 5, 9);
        pawnsBoard[5][10] = new Pawn( pawnsBoard, 5, 10);

        int meetingPointX = 5;
        int meetingPointY = 5;

        Board board = new Board(pawnsBoard, meetingPointX, meetingPointY);
        printBoard(board, meetingPointX, meetingPointY);

        OptimizerInterface optimizer = new Optimizer();
        optimizer.setBoard(board);

        boolean ended = board.optimizationEnded;

        while(!ended) {
            Thread.sleep(1000);
            ended = board.optimizationEnded;
        }

        printBoard(board, meetingPointX, meetingPointY);
    }

    @Test
    public void testOneLineBoard() throws InterruptedException {
        PawnInterface[][] pawnsBoard = new Pawn[12][12];

        pawnsBoard[5][0] = new Pawn( pawnsBoard, 5, 0);
        pawnsBoard[5][1] = new Pawn( pawnsBoard, 5, 1);
        pawnsBoard[5][2] = new Pawn( pawnsBoard, 5, 2);
        pawnsBoard[0][5] = new Pawn( pawnsBoard, 0, 5);
        pawnsBoard[1][5] = new Pawn( pawnsBoard, 1, 5);
        pawnsBoard[2][5] = new Pawn( pawnsBoard, 2, 5);
        pawnsBoard[8][5] = new Pawn( pawnsBoard, 8, 5);
        pawnsBoard[9][5] = new Pawn( pawnsBoard, 9, 5);
        pawnsBoard[5][7] = new Pawn( pawnsBoard, 5, 7);
        pawnsBoard[5][8] = new Pawn( pawnsBoard, 5, 8);
        pawnsBoard[5][9] = new Pawn( pawnsBoard, 5, 9);
        pawnsBoard[5][10] = new Pawn( pawnsBoard, 5, 10);

        int meetingPointX = 5;
        int meetingPointY = 5;

        Board board = new Board(pawnsBoard, meetingPointX, meetingPointY);
        printBoard(board, meetingPointX, meetingPointY);

        OptimizerInterface optimizer = new Optimizer();
        optimizer.setBoard(board);

        boolean ended = board.optimizationEnded;

        while(!ended) {
            Thread.sleep(1000);
            ended = board.optimizationEnded;
        }

        printBoard(board, meetingPointX, meetingPointY);
    }

    @Test
    public void testBigBoard() throws InterruptedException {
        PawnInterface[][] pawnsBoard = new Pawn[20][20];
        Random random = new Random();

        int counter = 150;

        for(int i = 0; i < counter; i++) {
            int x = random.nextInt(20);
            int y = random.nextInt(20);
            pawnsBoard[x][y] = new Pawn( pawnsBoard, x, y);
        }

        int meetingPointX = 10;
        int meetingPointY = 10;

        Board board = new Board(pawnsBoard, meetingPointX, meetingPointY);
        printBoard(board, meetingPointX, meetingPointY);

        OptimizerInterface optimizer = new Optimizer();
        optimizer.setBoard(board);

        boolean ended = board.optimizationEnded;

        while(!ended) {
            Thread.sleep(1000);
            ended = board.optimizationEnded;
        }

        printBoard(board, meetingPointX, meetingPointY);
    }

    @Test
    public void testCorner() throws InterruptedException {
        PawnInterface[][] pawnsBoard = new Pawn[20][20];
        Random random = new Random();

        int counter = 200;

        for(int i = 0; i < counter; i++) {
            int x = random.nextInt(20);
            int y = random.nextInt(20);
            pawnsBoard[x][y] = new Pawn( pawnsBoard, x, y);
        }

        int meetingPointX = 0;
        int meetingPointY = 0;

        Board board = new Board(pawnsBoard, meetingPointX, meetingPointY);
        printBoard(board, meetingPointX, meetingPointY);

        OptimizerInterface optimizer = new Optimizer();
        optimizer.setBoard(board);

        boolean ended = board.optimizationEnded;

        while(!ended) {
            Thread.sleep(1000);
            ended = board.optimizationEnded;
        }

        printBoard(board, meetingPointX, meetingPointY);
    }

    @Test
    public void testSuspendResume() throws InterruptedException {
        PawnInterface[][] pawnsBoard = new Pawn[12][12];

//        pawnsBoard[5][0] = new Pawn( pawnsBoard, 5, 0);
//        pawnsBoard[5][2] = new Pawn( pawnsBoard, 5, 2);
//        pawnsBoard[7][2] = new Pawn( pawnsBoard, 7, 2);
//        pawnsBoard[10][1] = new Pawn( pawnsBoard, 10, 1);
//        pawnsBoard[3][5] = new Pawn( pawnsBoard, 3, 5);
//        pawnsBoard[5][0] = new Pawn( pawnsBoard, 5, 0);
//        pawnsBoard[5][7] = new Pawn( pawnsBoard, 5, 7);
//        pawnsBoard[5][9] = new Pawn( pawnsBoard, 5, 9);
//        pawnsBoard[5][10] = new Pawn( pawnsBoard, 5, 10);

        pawnsBoard[5][0] = new Pawn( pawnsBoard, 5, 0);
        pawnsBoard[5][1] = new Pawn( pawnsBoard, 5, 1);
        pawnsBoard[5][2] = new Pawn( pawnsBoard, 5, 2);
        pawnsBoard[0][5] = new Pawn( pawnsBoard, 0, 5);
        pawnsBoard[1][5] = new Pawn( pawnsBoard, 1, 5);
        pawnsBoard[2][5] = new Pawn( pawnsBoard, 2, 5);
        pawnsBoard[8][5] = new Pawn( pawnsBoard, 8, 5);
        pawnsBoard[9][5] = new Pawn( pawnsBoard, 9, 5);
        pawnsBoard[5][7] = new Pawn( pawnsBoard, 5, 7);
        pawnsBoard[5][8] = new Pawn( pawnsBoard, 5, 8);
        pawnsBoard[5][9] = new Pawn( pawnsBoard, 5, 9);
        pawnsBoard[5][10] = new Pawn( pawnsBoard, 5, 10);

        int meetingPointX = 5;
        int meetingPointY = 5;

        Board board = new Board(pawnsBoard, meetingPointX, meetingPointY);
        printBoard(board, meetingPointX, meetingPointY);

        OptimizerInterface optimizer = new Optimizer();
        optimizer.setBoard(board);

        Thread.sleep(200);

        optimizer.suspend();

        System.out.println("na 5s wstrzymuje prace pionkow");
        Thread.sleep(1000);
        printBoard(board, meetingPointX, meetingPointY);
        Thread.sleep(1000);
        printBoard(board, meetingPointX, meetingPointY);
        Thread.sleep(3000);
        System.out.println("wznamwiam prace");
        optimizer.resume();


        boolean ended = board.optimizationEnded;

        while(!ended) {
            Thread.sleep(1000);
            ended = board.optimizationEnded;
        }

        printBoard(board, meetingPointX, meetingPointY);
    }

    private void printBoard(Board board, int meetingPointX, int meetingPointY) {
        int counter = 0;

        for(int i = 0; i < board.getSize(); i++) {
            for( int j = 0; j < board.getSize(); j++) {
                Optional<PawnInterface> pawn = board.get(j, i);

                if(pawn.isPresent()) {
                    counter++;
                    if(meetingPointX == j && meetingPointY == i)
                        System.out.print(ANSI_MAGNETA + String.format("%04d", pawn.get().getID()) + ANSI_RESET + " |");
                    else
                        System.out.print(ANSI_RED + String.format("%04d", pawn.get().getID()) + ANSI_RESET + " |");
                } else if(meetingPointX == j && meetingPointY == i) {
                    System.out.print(ANSI_RED +  "  x " + ANSI_RESET + " |");
                } else {
                    System.out.print( "  o  |");
                }
            }
            System.out.println();
        }
        System.out.println("ilosc pionkow: " + counter);
    }
}
