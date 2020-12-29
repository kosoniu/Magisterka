import java.util.concurrent.ThreadLocalRandom;

public class Pawn implements PawnInterface {

    private int id;
    private int rowPosition;
    private int colPosition;
    private PawnInterface board[][];
    public Thread worker;

    public Pawn(PawnInterface board[][], int colPosition, int rowPosition) {
        this.rowPosition = rowPosition;
        this.colPosition = colPosition;
        this.board = board;
        this.id = Integer.parseInt(String.valueOf(colPosition) + rowPosition);
    }

    @Override
    public int getID() {
        return this.id;
    }

    @Override
    public int moveLeft() {
        try {
//            Thread.sleep(ThreadLocalRandom.current().nextInt(100,300));
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(board[colPosition - 1][rowPosition] != null) {
            System.out.println("BLEDNE PRZESUNIECIE W LEWO!");
        }

        board[colPosition - 1][rowPosition] = this;
        board[colPosition][rowPosition] = null;
        colPosition -= 1;

        return colPosition;
    }

    @Override
    public int moveRight() {
        try {
//            Thread.sleep(ThreadLocalRandom.current().nextInt(100,300));
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(board[colPosition + 1][rowPosition] != null) {
            System.out.println("BLEDNE PRZESUNIECIE W PRAWO!");
        }

        board[colPosition + 1][rowPosition] = this;
        board[colPosition][rowPosition] = null;

        colPosition += 1;

        return colPosition;
    }

    @Override
    public int moveUp() {
        try {
//            Thread.sleep(ThreadLocalRandom.current().nextInt(100,300));
            Thread.sleep(200);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(board[colPosition][rowPosition - 1] != null) {
            System.out.println("BLEDNE PRZESUNIECIE DO GORY!");
        }

        board[colPosition][rowPosition - 1] = this;
        board[colPosition][rowPosition] = null;
        rowPosition -= 1;

        return rowPosition;
    }

    @Override
    public int moveDown() {
        try {
//            Thread.sleep(ThreadLocalRandom.current().nextInt(100,300));
            Thread.sleep(200);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(board[colPosition][rowPosition + 1] != null) {
            System.out.println("BLEDNE PRZESUNIECIE DO GORY!");
        }

        board[colPosition][rowPosition + 1] = this;
        board[colPosition][rowPosition] = null;
        rowPosition += 1;

        return rowPosition;
    }

    @Override
    public void registerThread(Thread thread) {
        this.worker = thread;
    }
}
