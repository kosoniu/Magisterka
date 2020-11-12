package test;

import main.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;

public class TestBoard {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    @Test
    public void testBoard() {
        PawnInterface[][] pawnsBoard = new Pawn[25][25];
        Random random = new Random();

//        pawnsBoard[0][0] = new Pawn( pawnsBoard, 0,0);
//        pawnsBoard[0][1] = new Pawn( pawnsBoard, 0,1);
//        pawnsBoard[0][2] = new Pawn( pawnsBoard, 0,2);
//        pawnsBoard[1][2] = new Pawn( pawnsBoard, 1,2);
//        pawnsBoard[3][0] = new Pawn( pawnsBoard, 3,0);

        int counter = 100;

        for(int i = 0; i < counter; i++) {
            int x = random.nextInt(25);
            int y = random.nextInt(25);
            pawnsBoard[x][y] = new Pawn( pawnsBoard, x, y);
        }
//

//        pawnsBoard[0][0] = new Pawn( pawnsBoard, 0,0);
//        pawnsBoard[0][9] = new Pawn( pawnsBoard, 0,9);
//        pawnsBoard[9][0] = new Pawn( pawnsBoard, 9,0);
//        pawnsBoard[9][9] = new Pawn( pawnsBoard, 9,9);
//        pawnsBoard[0][5] = new Pawn( pawnsBoard, 0,5);
//        pawnsBoard[9][5] = new Pawn( pawnsBoard, 9,5);

//        pawnsBoard[4][9] = new Pawn( pawnsBoard, 4,9);
//        pawnsBoard[8][7] = new Pawn( pawnsBoard, 8,7);
//        pawnsBoard[0][4] = new Pawn( pawnsBoard, 0,4);
//        pawnsBoard[9][4] = new Pawn( pawnsBoard, 9,4);
//        pawnsBoard[1][4] = new Pawn( pawnsBoard, 1,4);
//        pawnsBoard[2][4] = new Pawn( pawnsBoard, 2,4);
//        pawnsBoard[2][5] = new Pawn( pawnsBoard, 2,5);
//        pawnsBoard[4][3] = new Pawn( pawnsBoard, 4,3);
//        pawnsBoard[9][5] = new Pawn( pawnsBoard, 9,5);
//        pawnsBoard[2][3] = new Pawn( pawnsBoard, 2,3);
//        pawnsBoard[1][3] = new Pawn( pawnsBoard, 1,3);
//        pawnsBoard[1][2] = new Pawn( pawnsBoard, 1,2);

        int meetingPointX = 5;
        int meetingPointY = 5;

        BoardInterface board = new Board(pawnsBoard, meetingPointX, meetingPointY);
        printBoard(board, meetingPointX, meetingPointY);

        OptimizerInterface optimizer = new Optimizer();
        optimizer.setBoard(board);


        try {
//            Thread.sleep(100);
//            optimizer.suspend();
//            optimizer.resume();
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        printBoard(board, meetingPointX, meetingPointY);
//        printBoard(board, meetingPointY, meetingPointX);
    }

    private void printBoard(BoardInterface board, int meetingPointX, int meetingPointY) {
        for(int i = 0; i < board.getSize(); i++) {
            for( int j = 0; j < board.getSize(); j++) {
                Optional<PawnInterface> pawn = board.get(j, i);

                if(pawn.isPresent()) {
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
