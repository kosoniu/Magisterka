import java.util.*;

public class Optimizer implements OptimizerInterface {

    private PawnMover currentPositions[][];
    private PawnMover nextPositions[][];
    private int meetingPointCol;
    private int meetingPointRow;
    private List<PawnMover> movers;
    private final Object lock = new Object();

    @Override
    public void setBoard(BoardInterface board) {
        int boardSize = board.getSize();
        currentPositions = new PawnMover[boardSize][boardSize];
        nextPositions = new PawnMover[boardSize][boardSize];
        meetingPointCol = board.getMeetingPointCol();
        meetingPointRow = board.getMeetingPointRow();
        movers = new ArrayList<>();

        for(int i = boardSize - 1; i >= 0; i--) {
            for(int j = 0; j < boardSize; j++) {
                if(board.get(j, i).isPresent()) {
//                    System.out.println("Pionek obecny na : " + j + " " + i);
                    PawnMover mover = new PawnMover(j, i, board.get(j, i).get());
                    mover.setName("Watek " + j + "" + i);
                    currentPositions[j][i] = mover;
                    movers.add(mover);
                } else {
                    currentPositions[j][i] = null;
                }
            }
        }

        movers.forEach(Thread::start);
    }

    @Override
    public void suspend() {

    }

    @Override
    public void resume() {

    }

    private class PawnMover extends Thread {
        boolean finished;
        int currentColPosition;
        int currentRowPosition;
        PawnInterface pawn;
        Direction firstDirection;
        Direction secondDirection;

        public PawnMover(int currentPositionCol, int currentPositionRow, PawnInterface pawn) {
            this.currentColPosition = currentPositionCol;
            this.currentRowPosition = currentPositionRow;
            this.pawn = pawn;

            synchronized (lock) {
                currentPositions[currentPositionCol][currentPositionRow] = this;
            }
        }

        @Override
        public void run() {
            while (true) {
                if(currentColPosition == meetingPointCol && currentRowPosition == meetingPointRow) {
                    finished = true;
                    return;
                }

                updatePossibleDirections();

                int currentColPosition = this.currentColPosition;
                int currentRowPosition = this.currentRowPosition;

                int nextFirstMoveCol = firstDirection.getNextColPosition(currentColPosition);
                int nextFirstMoveRow = firstDirection.getNextRowPosition(currentRowPosition);

                int nextSecondMoveCol = -1;
                int nextSecondMoveRow = -1;

                if(secondDirection != null) {
                    nextSecondMoveCol = secondDirection.getNextColPosition(currentColPosition);
                    nextSecondMoveRow = secondDirection.getNextRowPosition(currentRowPosition);
                }

                Direction nextMove;
                int nextPositionCol;
                int nextPositionRow;

                synchronized (lock) {
                    while(true) {
                        if(nextPositions[nextFirstMoveCol][nextFirstMoveRow] == null && currentPositions[nextFirstMoveCol][nextFirstMoveRow] == null) {
                            nextPositionCol = nextFirstMoveCol;
                            nextPositionRow = nextFirstMoveRow;
                            nextMove = firstDirection;
                            nextPositions[nextPositionCol][nextPositionRow] = this;
                            break;
                        } else if(nextSecondMoveCol >= 0 && nextSecondMoveRow >= 0) {
                            if(nextPositions[nextSecondMoveCol][nextSecondMoveRow] == null && currentPositions[nextSecondMoveCol][nextSecondMoveRow] == null) {
                                nextPositionCol = nextSecondMoveCol;
                                nextPositionRow = nextSecondMoveRow;
                                nextMove = secondDirection;
                                nextPositions[nextPositionCol][nextPositionRow] = this;
                                break;
                            }
                        }

                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                move(nextMove);

                synchronized (lock) {
                    nextPositions[nextPositionCol][nextPositionRow] = null;
                    currentPositions[nextPositionCol][nextPositionRow] = this;
                    currentPositions[currentColPosition][currentRowPosition] = null;
                    lock.notifyAll();
                }
            }

        }

        private void move(Direction direction) {
            switch (direction){
                case DOWN:
                    currentRowPosition = pawn.moveDown();
                    break;
                case UP:
                    currentRowPosition = pawn.moveUp();
                    break;
                case LEFT:
                    currentColPosition = pawn.moveLeft();
                    break;
                case RIGHT:
                    currentColPosition = pawn.moveRight();
                    break;
            }
        }

        private void updatePossibleDirections() {
            if (this.currentColPosition < meetingPointCol) {
                if (this.currentRowPosition < meetingPointRow) {
                    firstDirection = Direction.RIGHT;
                    secondDirection = Direction.UP;
                } else if (this.currentRowPosition > meetingPointRow) {
                    firstDirection = Direction.RIGHT;
                    secondDirection = Direction.DOWN;
                } else {
                    firstDirection = Direction.RIGHT;
                    secondDirection = null;
                }
            } else if (this.currentColPosition > meetingPointCol) {
                if (this.currentRowPosition < meetingPointRow) {
                    firstDirection = Direction.LEFT;
                    secondDirection = Direction.UP;
                } else if (this.currentRowPosition > meetingPointRow) {
                    firstDirection = Direction.LEFT;
                    secondDirection = Direction.DOWN;
                } else {
                    firstDirection = Direction.LEFT;
                    secondDirection = null;
                }
            } else {
                if (this.currentRowPosition < meetingPointRow) {
                    firstDirection = Direction.UP;
                } else {
                    firstDirection = Direction.DOWN;
                }
                secondDirection = null;
            }
        }
    }

    enum Direction {
        LEFT(-1, 0),
        RIGHT(+1, 0),
        UP(0, +1),
        DOWN(0, -1);

        final int deltaCol;
        final int deltaRow;

        Direction(int deltaCol, int deltaRow) {
            this.deltaCol = deltaCol;
            this.deltaRow = deltaRow;
        }

        public int getNextColPosition(int colPosition) {
            return colPosition + deltaCol;
        }

        public int getNextRowPosition(int rowPosition) {
            return rowPosition + deltaRow;
        }
    }




}


