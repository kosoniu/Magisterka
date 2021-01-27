
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;

public class BoardTest {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_MAGNETA = "\u001b[34m";

    @Test
    public void testBoard() throws InterruptedException {
        PawnInterface[][] pawnsBoard = new Pawn[9][9];

        pawnsBoard[2][1] = new Pawn( pawnsBoard, 2, 1);
        pawnsBoard[1][2] = new Pawn( pawnsBoard, 1, 2);
        pawnsBoard[6][1] = new Pawn( pawnsBoard, 6, 1);
        pawnsBoard[7][2] = new Pawn( pawnsBoard, 7, 2);

        pawnsBoard[4][3] = new Pawn( pawnsBoard, 4, 3);
        pawnsBoard[3][4] = new Pawn( pawnsBoard, 3, 4);
        pawnsBoard[5][4] = new Pawn( pawnsBoard, 5, 4);
        pawnsBoard[4][5] = new Pawn( pawnsBoard, 4, 5);

        pawnsBoard[1][6] = new Pawn( pawnsBoard, 1, 6);
        pawnsBoard[2][7] = new Pawn( pawnsBoard, 2, 7);
        pawnsBoard[7][6] = new Pawn( pawnsBoard, 7, 6);
        pawnsBoard[6][7] = new Pawn( pawnsBoard, 6, 7);

        int meetingPointX = 4;
        int meetingPointY = 4;

        Board board = new Board(pawnsBoard, meetingPointX, meetingPointY);
        printBoard(board, meetingPointX, meetingPointY);

        OptimizerInterface optimizer = new Optimizer();
        optimizer.setBoard(board);

//        boolean ended = board.optimizationEnded;

//        while(!ended) {
            Thread.sleep(5000);
//            ended = board.optimizationEnded;
//        }

        printBoard(board, meetingPointX, meetingPointY);
    }


    private void printBoard(Board board, int meetingPointX, int meetingPointY) {
        int counter = 0;

        for(int i = board.getSize() - 1; i >= 0 ; i--) {
            for( int j = 0; j < board.getSize() ; j++) {
                Optional<PawnInterface> pawn = board.get(j, i);

                if(pawn.isPresent()) {
                    counter++;
                    if(meetingPointX == j && meetingPointY == i)
                        System.out.print(ANSI_MAGNETA + String.format("%4d", pawn.get().getID()) + ANSI_RESET + " |");
                    else
                        System.out.print(ANSI_RED + String.format("%4d", pawn.get().getID()) + ANSI_RESET + " |");
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
