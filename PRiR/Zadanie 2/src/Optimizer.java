import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Optimizer implements OptimizerInterface {

    private BoardInterface board;
    private Locker[][] lockers;
    private Set<PawnMover> movers;
    private int meetingPointCol;
    private int meetingPointRow;

    @Override
    public void setBoard(BoardInterface board) {
        this.board = board;

        this.meetingPointCol = board.getMeetingPointCol();
        this.meetingPointRow = board.getMeetingPointRow();
        this.movers = new HashSet<>();
        this.lockers = new Locker[this.board.getSize()][this.board.getSize()];

        for(int i = 0; i < board.getSize(); i++) {
            for(int j = 0; j < board.getSize(); j++) {
                this.lockers[j][i] = new Locker();
                Optional<PawnInterface> pawn = this.board.get(j, i);

                if(pawn.isPresent()) {
                    PawnMover pawnMover = new PawnMover(pawn.get(), j, i);
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

//        OptimizationWatcher watcher = new OptimizationWatcher();
//        watcher.start();
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

    private static class Locker {
        public final ReentrantLock lock = new ReentrantLock();
        public final Condition cannotMove = lock.newCondition();
        public final Queue<Locker> waitingObjects = new ConcurrentLinkedQueue<>();
    }

    private class OptimizationWatcher extends Thread {

        @Override
        public void run() {
            boolean finished = false;
            board.optimizationStart();

            while(!finished) {
                for(PawnMover pawnMover: movers) {
                    if(pawnMover.suspended) {
                        finished = false;
                        break;
                    }

                    if(pawnMover.getState() == Thread.State.TERMINATED || pawnMover.getState() == Thread.State.WAITING) {
                        finished = true;
                    } else {
                        finished = false;
                        break;
                    }
                }
            }

            finished = false;

            while(!finished) {
                for(PawnMover pawnMover: movers) {
                    if(pawnMover.getState() == Thread.State.TERMINATED) {
                        finished = true;
                    } else {
                        finished = false;
                        pawnMover.finish();
                        break;
                    }
                }
            }

            board.optimizationDone();
        }
    }

    private class PawnMover extends Thread {

        private final PawnInterface pawn;
        private int currentColPosition;
        private int currentRowPosition;
        private Boolean suspended;
        private boolean finished;

        PawnMover(PawnInterface pawn, int currentColPosition, int currentRowPosition) {
            this.pawn = pawn;
            this.currentColPosition = currentColPosition;
            this.currentRowPosition = currentRowPosition;
            this.suspended = false;
            this.finished = false;
        }

        @Override
        public void run() {
            while(true) {
                int col = this.currentColPosition;
                int row = this.currentRowPosition;

                while(suspended) {
                    lockers[col][row].lock.lock();
                    try {
                        lockers[col][row].cannotMove.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lockers[col][row].lock.unlock();
                    }
                }

                if(this.currentColPosition == meetingPointCol && this.currentRowPosition == meetingPointRow)
                    break;

                if(finished)
                    break;

                if(this.currentColPosition < meetingPointCol) {
                    if (this.currentRowPosition < meetingPointRow) {
                        checkIf2WayMovementIsPossible(col + 1, row, col, row + 1, "R", "D");
                    } else if(this.currentRowPosition > meetingPointRow) {
                        checkIf2WayMovementIsPossible(col + 1, row, col, row - 1, "R", "U");
                    } else {
                        checkIfMovementIsPossible( col + 1, row, "R");
                    }
                } else if(this.currentColPosition > meetingPointCol) {
                    if (this.currentRowPosition < meetingPointRow) {
                        checkIf2WayMovementIsPossible(col - 1, row, col, row + 1, "L", "D");
                    } else if(this.currentRowPosition > meetingPointRow) {
                        checkIf2WayMovementIsPossible(col - 1, row, col, row - 1,"L", "U");
                    } else {
                        checkIfMovementIsPossible( col - 1, row, "L");
                    }
                } else {
                    if (this.currentRowPosition < meetingPointRow) {
                        checkIfMovementIsPossible( col, row + 1, "D");
                    } else {
                        checkIfMovementIsPossible( col, row - 1, "U");
                    }
                }
            }
        }

        public void finish() {
            int col = this.currentColPosition;
            int row = this.currentRowPosition;

            lockers[col][row].lock.lock();

            try {
                if(!this.finished) {
                    lockers[col][row].cannotMove.signalAll();
                }
            } finally {
                lockers[col][row].lock.unlock();
            }
        }

        public synchronized void suspendThread(){
            System.out.println("Wstrzymuje prace watku: " + getName());
            suspended = true;
        }

        public void resumeThread(){
            lockers[this.currentColPosition][this.currentRowPosition].lock.lock();
            try {
                System.out.println("Wznawiam prace watku: " + getName());
                suspended = false;
                lockers[this.currentColPosition][this.currentRowPosition].cannotMove.signal();
            } finally {
                lockers[this.currentColPosition][this.currentRowPosition].lock.unlock();
            }
        }

        private void movePawn(String direction, int col, int row) {
            switch (direction) {
                case "L" -> moveLeft(col, row);
                case "R" -> moveRight(col, row);
                case "U" -> moveUp(col, row);
                case "D" -> moveDown(col, row);
            }
        }

        private void moveLeft(int col, int row) {
            try {
                this.currentColPosition = pawn.moveLeft();
            } finally {
                lockers[col - 1][row].lock.unlock();
            }
        }

        private void moveRight(int col, int row) {
            try {
                this.currentColPosition = pawn.moveRight();
            } finally {
                lockers[col + 1][row].lock.unlock();
            }
        }

        private void moveUp(int col, int row) {
            try {
                this.currentRowPosition = pawn.moveUp();
            } finally {
                lockers[col][row - 1].lock.unlock();
            }
        }

        private void moveDown(int col, int row) {
            try {
                this.currentRowPosition = pawn.moveDown();
            } finally {
                lockers[col][row + 1].lock.unlock();
            }
        }

        private void wakeUpThreads(int col, int row) {
//            System.out.println(getName() + " budze watki czekajace na " + x + row);
            if(!lockers[col][row].waitingObjects.isEmpty()) {
                Locker locker = lockers[col][row].waitingObjects.remove();
                locker.lock.lock();
                try {
                    locker.cannotMove.signal();
                } finally {
                    locker.lock.unlock();
                }
            }
        }

        private void checkIfMovementIsPossible(int col, int row, String direction) {
            int currentColPosition = this.currentColPosition;
            int currentRowPosition = this.currentRowPosition;

//            System.out.println(getName() + " current position: " + currentColPosition + ", " + currentRowPosition);

            try {
                lockers[currentColPosition][currentRowPosition].lock.lock();
                while(board.get(col, row).isPresent()) {
                    if(lockers[col][row].waitingObjects.contains(lockers[currentColPosition][currentRowPosition])) {
//                        this.finished = true;
                        return;
                    }
                    lockers[col][row].waitingObjects.add(lockers[currentColPosition][currentRowPosition]);
                    System.out.println(getName() + " czekam na: " + col + row);
                    lockers[currentColPosition][currentRowPosition].cannotMove.await();
                    System.out.println(getName() + " budze sie");
                }
                if(lockers[col][row].lock.tryLock()) {
                    movePawn(direction, currentColPosition, currentRowPosition);
                    wakeUpThreads(currentColPosition, currentRowPosition);
                }
            } catch (InterruptedException e) {}
            finally {
                lockers[currentColPosition][currentRowPosition].lock.unlock();
            }
        }

        private void checkIf2WayMovementIsPossible(int x1, int y1, int x2, int y2, String firstDirection, String secondDirection) {
            int x = this.currentColPosition;
            int y = this.currentRowPosition;

            try {
                lockers[x][y].lock.lock();
                while(board.get(x1, y1).isPresent() && board.get(x2, y2).isPresent()) {
                    if(lockers[x1][y1].waitingObjects.contains(lockers[x][y]) &&
                        lockers[x2][y2].waitingObjects.contains(lockers[x][y])) {
//                        this.finished = true;
                        return;
                    }
                    lockers[x1][y1].waitingObjects.add(lockers[x][y]);
                    lockers[x2][y2].waitingObjects.add(lockers[x][y]);
                    lockers[x][y].cannotMove.await();
                }
                if(board.get(x1, y1).isEmpty() && lockers[x1][y1].lock.tryLock()) {
                    movePawn(firstDirection, x, y);
                    wakeUpThreads(x, y);


                } else if(board.get(x2, y2).isEmpty() && lockers[x2][y2].lock.tryLock()) {
                    movePawn(secondDirection, x, y);
                    wakeUpThreads(x, y);


                }
            } catch (InterruptedException e) {}
            finally {
                lockers[x][y].lock.unlock();
            }
        }
    }
}


