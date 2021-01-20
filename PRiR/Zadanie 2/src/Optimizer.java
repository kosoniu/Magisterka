import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Optimizer implements OptimizerInterface {

    private BoardInterface board;
    private Set<PawnMover> movers;
    private int meetingPointCol;
    private int meetingPointRow;
    private final Object lock = new Object();
    private PawnMover[][] moversBoard;

    @Override
    public void setBoard(BoardInterface board) {
        this.board = board;

        this.meetingPointCol = board.getMeetingPointCol();
        this.meetingPointRow = board.getMeetingPointRow();
        this.movers = new HashSet<>();
        moversBoard = new PawnMover[board.getSize()][board.getSize()];

        int counter = 0;

        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                Optional<PawnInterface> pawn = this.board.get(j, i);
                if (pawn.isPresent()) {
                    PawnMover pawnMover = new PawnMover(pawn.get(), j, i, counter);
                    moversBoard[j][i] = pawnMover;
                    counter++;
                    pawnMover.setName("Watek: " + pawn.get().getID());
                    try {
                        pawnMover.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    movers.add(pawnMover);
                    pawn.get().registerThread(pawnMover);
                }
            }
        }

        board.optimizationStart();
        movers.forEach(PawnMover::start);
    }

    @Override
    public void suspend() {
        movers.forEach(PawnMover::suspendThread);
    }

    @Override
    public void resume() {
        movers.forEach(PawnMover::resumeThread);
    }

    private class PawnMover extends Thread {

        private final PawnInterface pawn;
        private int currentColPosition;
        private int currentRowPosition;
        private Boolean suspended;
        private boolean finished;
        private int id;
        private List<PawnMover> waitingMovers;

        PawnMover(PawnInterface pawn, int currentColPosition, int currentRowPosition, int id) {
            this.pawn = pawn;
            this.currentColPosition = currentColPosition;
            this.currentRowPosition = currentRowPosition;
            this.suspended = false;
            this.finished = false;
            this.id = id;
            this.waitingMovers = new ArrayList<>();
        }

        @Override
        public void run() {
            while (!finished) {
                int col = this.currentColPosition;
                int row = this.currentRowPosition;

                if (this.currentColPosition == meetingPointCol && this.currentRowPosition == meetingPointRow) {
                    finished = true;
                    break;
                }

                if (this.currentColPosition < meetingPointCol) {
                    if (this.currentRowPosition < meetingPointRow) {
                        checkIf2WayMovementIsPossible(col + 1, row, col, row + 1, "R", "D");
                    } else if (this.currentRowPosition > meetingPointRow) {
                        checkIf2WayMovementIsPossible(col + 1, row, col, row - 1, "R", "U");
                    } else {
                        checkIfMovementIsPossible(col + 1, row, "L");
                    }
                } else if (this.currentColPosition > meetingPointCol) {
                    if (this.currentRowPosition < meetingPointRow) {
                        checkIf2WayMovementIsPossible(col - 1, row, col, row + 1, "L", "D");
                    } else if (this.currentRowPosition > meetingPointRow) {
                        checkIf2WayMovementIsPossible(col - 1, row, col, row - 1, "L", "U");
                    } else {
                        checkIfMovementIsPossible(col - 1, row, "R");
                    }
                } else {
                    if (this.currentRowPosition < meetingPointRow) {
                        checkIfMovementIsPossible(col, row + 1, "U");
                    } else {
                        checkIfMovementIsPossible(col, row - 1, "D");
                    }
                }
            }

            if (movers.stream().filter(mover -> !mover.finished).count() == 0) {
                System.out.println("Optimization done");
                board.optimizationDone();
            }
        }

        public synchronized void suspendThread() {
            System.out.println("Wstrzymuje prace watku: " + getName());
            suspended = true;
        }

        public void resumeThread() {

        }

        private void wakeUpThreads() {
            for (PawnMover mover : waitingMovers) {
                synchronized (mover) {
                    mover.notify();
                }
            }
        }

        private synchronized void movePawn(String direction) {
            switch (direction) {
                case "L":
                    moveLeft();
                    break;
                case "R":
                    moveRight();
                    break;
                case "U":
                    moveUp();
                    break;
                case "D":
                    moveDown();
                    break;
            }
        }

        private void moveLeft() {
            this.currentColPosition = pawn.moveLeft();
            moversBoard[this.currentColPosition][this.currentRowPosition] = this;
            moversBoard[this.currentColPosition + 1][this.currentRowPosition] = null;
        }

        private void moveRight() {
            this.currentColPosition = pawn.moveRight();
            moversBoard[this.currentColPosition][this.currentRowPosition] = this;
            moversBoard[this.currentColPosition - 1][this.currentRowPosition] = null;
        }

        private void moveUp() {
            this.currentRowPosition = pawn.moveUp();
            moversBoard[this.currentColPosition][this.currentRowPosition] = this;
            moversBoard[this.currentColPosition][this.currentRowPosition + 1] = null;
        }

        private void moveDown() {
            this.currentRowPosition = pawn.moveDown();
            moversBoard[this.currentColPosition][this.currentRowPosition] = this;
            moversBoard[this.currentColPosition][this.currentRowPosition - 1] = null;
        }

        private void checkIfMovementIsPossible(int col, int row, String direction) {
            synchronized (lock) {
                while (moversBoard[col][row] != null) {
                    if (moversBoard[col][row] != null && movers.stream().filter(mover -> moversBoard[col][row].pawn.getID() == mover.pawn.getID()).findFirst().get().finished) {
                        finished = true;
                        System.out.println(getName() + " koncze prace");
                        return;
                    }
//                    System.out.println(getName() + " czekam na: " + col + row);
                    try {
                        lock.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    System.out.println(getName() + " budze sie");
                }

                if (!finished) {
                    movePawn(direction);
                    lock.notifyAll();
                }
            }
        }

        private synchronized void checkIf2WayMovementIsPossible(int x1, int y1, int x2, int y2, String firstDirection, String secondDirection) {
            synchronized (lock) {
                while (moversBoard[x1][y1] != null && moversBoard[x2][y2] != null) {
                    if (checkIfIsOnFinalPosition(x1, y1, x2, y2)) {
                        finished = true;
                        System.out.println(getName() + " koncze prace");
                        return;
                    }

                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (moversBoard[x1][y1] == null) {
                    if (!finished) {
                        movePawn(firstDirection);
                        lock.notifyAll();
                    }
                } else if (moversBoard[x2][y2] == null) {
                    if (!finished) {
                        movePawn(secondDirection);
                        lock.notifyAll();
                    }
                }
            }
        }

        private boolean checkIfIsOnFinalPosition(int x1, int y1, int x2, int y2) {
            boolean finishedOneDirection;
            boolean finishedSecondDirection;

            if(moversBoard[x1][y1] != null && moversBoard[x2][y2] != null) {
                finishedOneDirection = movers.stream().filter(mover -> moversBoard[x1][y1].pawn.getID() == mover.pawn.getID()).findFirst().get().finished;
                finishedSecondDirection = movers.stream().filter(mover -> moversBoard[x2][y2].pawn.getID() == mover.pawn.getID()).findFirst().get().finished;
            } else {
                return false;
            }

            return finishedOneDirection && finishedSecondDirection;
        }

    }
}


