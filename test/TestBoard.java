package test;

import main.*;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class TestBoard {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    @Test
    public void testBoard() {
        PawnInterface pawnsBoard[][] = new Pawn[10][10];
        pawnsBoard[0][0] = new Pawn( pawnsBoard, 0,0);
        pawnsBoard[4][9] = new Pawn( pawnsBoard, 4,9);
        pawnsBoard[8][7] = new Pawn( pawnsBoard, 8,7);
        pawnsBoard[0][4] = new Pawn( pawnsBoard, 0,4);
        pawnsBoard[9][4] = new Pawn( pawnsBoard, 9,4);
        pawnsBoard[2][6] = new Pawn( pawnsBoard, 2,6);
        pawnsBoard[4][3] = new Pawn( pawnsBoard, 4,3);
        pawnsBoard[9][3] = new Pawn( pawnsBoard, 9,3);

        int meetingPointX = 3;
        int meetingPointY = 4;

        BoardInterface board = new Board(pawnsBoard, meetingPointY, meetingPointX);
        printBoard(board, meetingPointX, meetingPointY);

        OptimizerInterface optimizer = new Optimizer();
        optimizer.setBoard(board);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        printBoard(board, meetingPointX, meetingPointY);
    }

    private void printBoard(BoardInterface board, int meetingPointX, int meetingPointY) {
        for(int i = 0; i < 10; i++) {
            for( int j = 0; j < 10; j++) {
                Optional<PawnInterface> pawn = board.get(i,j);

                if(pawn.isPresent()) {
                    System.out.print(ANSI_RED + pawn.get().getID() + ANSI_RESET + " | ");
                } else if(meetingPointX == i && meetingPointY == j) {
                    System.out.print(ANSI_RED + "x" + ANSI_RESET + " | ");
                } else {
                    System.out.print( "o | ");
                }
            }
            System.out.println();
        }

        System.out.println();
    }
}
