
import org.junit.jupiter.api.Test;

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

//        boolean ended = board.optimizationEnded;

//        while(!ended) {
            Thread.sleep(3000);
//            ended = board.optimizationEnded;
//        }

        printBoard(board, meetingPointX, meetingPointY);
    }

    @Test
    public void testCollision() throws InterruptedException {
        PawnInterface[][] pawnsBoard = new Pawn[10][10];

        pawnsBoard[0][0] = new Pawn( pawnsBoard, 0, 0);
        pawnsBoard[0][1] = new Pawn( pawnsBoard, 0, 1);
        pawnsBoard[1][0] = new Pawn( pawnsBoard, 1, 0);
        pawnsBoard[1][1] = new Pawn( pawnsBoard, 1, 1);
        pawnsBoard[0][2] = new Pawn( pawnsBoard, 0, 2);
        pawnsBoard[2][0] = new Pawn( pawnsBoard, 2, 0);
        pawnsBoard[2][1] = new Pawn( pawnsBoard, 2, 1);
        pawnsBoard[2][2] = new Pawn( pawnsBoard, 2, 2);
        pawnsBoard[1][2] = new Pawn( pawnsBoard, 1, 2);
        pawnsBoard[3][0] = new Pawn( pawnsBoard, 3, 0);
        pawnsBoard[3][1] = new Pawn( pawnsBoard, 3, 1);
        pawnsBoard[3][2] = new Pawn( pawnsBoard, 3, 2);
        pawnsBoard[3][3] = new Pawn( pawnsBoard, 3, 3);
        pawnsBoard[0][3] = new Pawn( pawnsBoard, 0, 3);
        pawnsBoard[1][3] = new Pawn( pawnsBoard, 1, 3);
        pawnsBoard[2][3] = new Pawn( pawnsBoard, 2, 3);
        pawnsBoard[5][0] = new Pawn( pawnsBoard, 5, 0);
        pawnsBoard[5][1] = new Pawn( pawnsBoard, 5, 1);
        pawnsBoard[5][3] = new Pawn( pawnsBoard, 5, 3);

        int meetingPointX = 8;
        int meetingPointY = 6;

        Board board = new Board(pawnsBoard, meetingPointX, meetingPointY);
        printBoard(board, meetingPointX, meetingPointY);

        OptimizerInterface optimizer = new Optimizer();
        optimizer.setBoard(board);

//        boolean ended = board.optimizationEnded;
//
//        while(!ended) {
            Thread.sleep(3000);
//            ended = board.optimizationEnded;
//        }

        printBoard(board, meetingPointX, meetingPointY);
    }

    @Test
    public void testOneLineBoard() throws InterruptedException {
        PawnInterface[][] pawnsBoard = new Pawn[10][10];

//        pawnsBoard[4][0] = new Pawn( pawnsBoard, 4, 0);
//        pawnsBoard[4][1] = new Pawn( pawnsBoard, 4, 1);
//        pawnsBoard[4][2] = new Pawn( pawnsBoard, 4, 2);
        pawnsBoard[4][9] = new Pawn( pawnsBoard, 4, 9);
        pawnsBoard[4][8] = new Pawn( pawnsBoard, 4, 8);
        pawnsBoard[4][7] = new Pawn( pawnsBoard, 4, 7);
        pawnsBoard[0][4] = new Pawn( pawnsBoard, 0, 4);
        pawnsBoard[1][4] = new Pawn( pawnsBoard, 1, 4);
        pawnsBoard[2][4] = new Pawn( pawnsBoard, 2, 4);
        pawnsBoard[9][4] = new Pawn( pawnsBoard, 9, 4);
        pawnsBoard[8][4] = new Pawn( pawnsBoard, 8, 4);
        pawnsBoard[7][4] = new Pawn( pawnsBoard, 7, 4);

        int meetingPointCol = 4;
        int meetingPointRow = 4;

        Board board = new Board(pawnsBoard, meetingPointCol, meetingPointRow);
        printBoard(board, meetingPointCol, meetingPointRow);

        OptimizerInterface optimizer = new Optimizer();
        optimizer.setBoard(board);

//        boolean ended = board.optimizationEnded;
//
//        while(!ended) {
            Thread.sleep(2000);
//            ended = board.optimizationEnded;
//        }

        printBoard(board, meetingPointCol, meetingPointRow);
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
    public void testVertical() throws InterruptedException {
        PawnInterface[][] pawnsBoard = new Pawn[20][20];

        pawnsBoard[10][0] = new Pawn( pawnsBoard, 10, 0);
        pawnsBoard[10][1] = new Pawn( pawnsBoard, 10, 1);
        pawnsBoard[10][2] = new Pawn( pawnsBoard, 10, 2);
        pawnsBoard[10][3] = new Pawn( pawnsBoard, 10, 3);
        pawnsBoard[10][4] = new Pawn( pawnsBoard, 10, 4);
        pawnsBoard[10][15] = new Pawn( pawnsBoard, 10, 15);
        pawnsBoard[10][16] = new Pawn( pawnsBoard, 10, 16);
        pawnsBoard[10][17] = new Pawn( pawnsBoard, 10, 17);
        pawnsBoard[10][18] = new Pawn( pawnsBoard, 10, 18);
        pawnsBoard[10][19] = new Pawn( pawnsBoard, 10, 19);

        int meetingPointCol = 10;
        int meetingPointRow = 10;

        Board board = new Board(pawnsBoard, meetingPointCol, meetingPointRow);
        printBoard(board, meetingPointCol, meetingPointRow);

        OptimizerInterface optimizer = new Optimizer();
        optimizer.setBoard(board);

//        boolean ended = board.optimizationEnded;
//
//        while(!ended) {
        Thread.sleep(2000);
//            ended = board.optimizationEnded;
//        }

        printBoard(board, meetingPointCol, meetingPointRow);
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
