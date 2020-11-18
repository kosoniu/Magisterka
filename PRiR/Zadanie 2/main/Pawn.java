package main;

public class Pawn implements PawnInterface {

    private int id;
    private int xPosition;
    private int yPosition;
    private PawnInterface board[][];
    public Thread worker;

    public Pawn(PawnInterface board[][], int xPosition, int yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.board = board;
        this.id = Integer.parseInt(xPosition + String.valueOf(yPosition));
    }

    @Override
    public int getID() {
        return this.id;
    }

    @Override
    public int moveLeft() {
        board[xPosition - 1][yPosition] = this;
        board[xPosition][yPosition] = null;
        xPosition -= 1;
        return xPosition;
    }

    @Override
    public int moveRight() {
        board[xPosition + 1][yPosition] = this;
        board[xPosition][yPosition] = null;
        xPosition += 1;
        return xPosition;
    }

    @Override
    public int moveUp() {
        board[xPosition][yPosition - 1] = this;
        board[xPosition][yPosition] = null;
        yPosition -= 1;
        return yPosition;
    }

    @Override
    public int moveDown() {
        board[xPosition][yPosition + 1] = this;
        board[xPosition][yPosition] = null;
        yPosition += 1;
        return yPosition;
    }

    @Override
    public void registerThread(Thread thread) {
        this.worker = thread;
    }

}
