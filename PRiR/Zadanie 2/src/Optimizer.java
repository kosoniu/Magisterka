import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Optimizer implements OptimizerInterface {

    private PawnMover[][] currentPositions;
    private PawnMover[][] nextPositions;
    private int meetingPointCol;
    private int meetingPointRow;
    private List<PawnMover> movers;
    private final Object lock = new Object();
    private BoardInterface board;

    @Override
    public void setBoard(BoardInterface board) {
        int boardSize = board.getSize();
        currentPositions = new PawnMover[boardSize][boardSize];
        nextPositions = new PawnMover[boardSize][boardSize];
        meetingPointCol = board.getMeetingPointCol();
        meetingPointRow = board.getMeetingPointRow();
        movers = new ArrayList<>();
        this.board = board;

        for(int i = boardSize - 1; i >= 0; i--) {
            for(int j = 0; j < boardSize; j++) {
                if(board.get(j, i).isPresent()) {
                    PawnInterface pawn = board.get(j, i).get();
                    PawnMover mover = new PawnMover(j, i, pawn);
                    mover.setName("Watek " + j + "" + i);
                    pawn.registerThread(mover);
                    currentPositions[j][i] = mover;
                    movers.add(mover);
                } else {
                    currentPositions[j][i] = null;
                }
            }
        }

        board.optimizationStart();
        movers.forEach(Thread::start);
    }

    @Override
    public void suspend() {
        movers.forEach(mover -> mover.suspended.set(true));

        boolean allStopped = false;

        while (!allStopped) {
            for(PawnMover mover: movers) {
                if(mover.getState() == Thread.State.WAITING || mover.getState() == Thread.State.TERMINATED) {
                    allStopped = true;
                } else {
                    allStopped = false;
                    break;
                }
            }
        }
    }

    @Override
    public void resume() {
        movers.forEach(mover -> {
            mover.suspended.set(false);
            synchronized (mover) {
                mover.notify();
            }
        });
    }

    private class PawnMover extends Thread {
        boolean finished;
        int currentColPosition;
        int currentRowPosition;
        PawnInterface pawn;
        Direction firstDirection;
        Direction secondDirection;
        private AtomicBoolean suspended;

        public PawnMover(int currentPositionCol, int currentPositionRow, PawnInterface pawn) {
            this.currentColPosition = currentPositionCol;
            this.currentRowPosition = currentPositionRow;
            this.pawn = pawn;
            this.suspended = new AtomicBoolean(false);

            synchronized (lock) {
                currentPositions[currentPositionCol][currentPositionRow] = this;
            }
        }

        @Override
        public void run() {
            outerLoop: while (true) {
                if(currentColPosition == meetingPointCol && currentRowPosition == meetingPointRow) {
                    finished = true;
                    break;
                }

                checkIfSuspended();

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

                        if(checkIfMovementIsFinished(nextFirstMoveCol, nextFirstMoveRow, nextSecondMoveCol, nextSecondMoveRow)) {
                            finished = true;
                            lock.notifyAll();
                            break outerLoop;
                        }

                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                checkIfSuspended();
                move(nextMove);
                checkIfSuspended();

                synchronized (lock) {
                    nextPositions[nextPositionCol][nextPositionRow] = null;
                    currentPositions[nextPositionCol][nextPositionRow] = this;
                    currentPositions[currentColPosition][currentRowPosition] = null;
                    lock.notifyAll();
                }
            }

            boolean finished = false;

            for(PawnMover mover: movers) {
                if(mover.finished)
                    finished = true;
                else {
                    finished = false;
                    break;
                }
            }

            if(finished) {
                board.optimizationDone();
            }
        }

        private void checkIfSuspended() {
            while(suspended.get()) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private boolean checkIfMovementIsFinished(int nextFirstMoveCol, int nextFirstMoveRow, int nextSecondMoveCol, int nextSecondMoveRow) {
            boolean finished = false;

            if(secondDirection != null && currentPositions[nextFirstMoveCol][nextFirstMoveRow] != null && currentPositions[nextSecondMoveCol][nextSecondMoveRow] != null) {
                if(currentPositions[nextFirstMoveCol][nextFirstMoveRow].finished && currentPositions[nextSecondMoveCol][nextSecondMoveRow].finished) {
                    finished = true;
                }
            } else if(currentPositions[nextFirstMoveCol][nextFirstMoveRow] != null){
                if(currentPositions[nextFirstMoveCol][nextFirstMoveRow].finished) {
                    finished = true;
                }
            }

            return finished;
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


