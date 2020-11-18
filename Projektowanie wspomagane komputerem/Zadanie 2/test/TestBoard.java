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
    public void testBoard() {
        PawnInterface[][] pawnsBoard = new Pawn[20][20];
        Random random = new Random();

        int counter = 300;

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

        printBoard(board, meetingPointX, meetingPointY);
    }

    @Test
    public void testSuspendResume() {
        PawnInterface[][] pawnsBoard = new Pawn[10][10];
        Random random = new Random();
        OptimizerInterface optimizer = new Optimizer();
        List<Pawn> pawns = new ArrayList<>();

        int counter = 25;

        for(int i = 0; i < counter; i++) {
            int x = random.nextInt(10);
            int y = random.nextInt(10);
            Pawn pawn = new Pawn( pawnsBoard, x, y);
            pawnsBoard[x][y] = pawn;
            pawns.add(pawn);
        }

        int meetingPointX = 5;
        int meetingPointY = 5;

        Board board = new Board(pawnsBoard, meetingPointX, meetingPointY);
        printBoard(board, meetingPointX, meetingPointY);
        optimizer.setBoard(board);
        optimizer.suspend();

//        pawns.forEach(pawn -> System.out.println(pawn.worker.getName() + " " + pawn.worker.getState()));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

//        pawns.forEach(pawn -> System.out.println(pawn.worker.getName() + " " + pawn.worker.getState()));

        optimizer.resume();

        printBoard(board, meetingPointX, meetingPointY);

    }

    private void printBoard(BoardInterface board, int meetingPointX, int meetingPointY) {
        for(int i = 0; i < board.getSize(); i++) {
            for( int j = 0; j < board.getSize(); j++) {
                Optional<PawnInterface> pawn = board.get(j, i);

                if(pawn.isPresent()) {
                    if(meetingPointX == j && meetingPointY == i)
                        System.out.print(ANSI_MAGNETA + pawn.get().getID() + ANSI_RESET + " | ");
                    else
                        System.out.print(ANSI_RED + pawn.get().getID() + ANSI_RESET + " | ");
                } else if(meetingPointX == j && meetingPointY == i) {
                    System.out.print(ANSI_RED + " x" + ANSI_RESET + " | ");
                } else {
                    System.out.print( " o | ");
                }
            }
            System.out.println();
        }

        System.out.println();
    }
}
