package main;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Optimizer implements OptimizerInterface {

    private BoardInterface board;
    private Locker[][] lockers;
    private Set<PawnMover> movers;
    private int meetingPointX;
    private int meetingPointY;

    @Override
    public void setBoard(BoardInterface board) {
        this.board = board;

        this.meetingPointX = board.getMeetingPointCol();
        this.meetingPointY = board.getMeetingPointRow();
        this.movers = new HashSet<>();
        this.lockers = new Locker[this.board.getSize()][this.board.getSize()];

        for(int i = 0; i < board.getSize(); i++) {
            for(int j = 0; j < board.getSize(); j++) {
                this.lockers[i][j] = new Locker();
                Optional<PawnInterface> pawn = this.board.get(i,j);

                if(pawn.isPresent()) {
                    PawnMover pawnMover = new PawnMover(pawn.get(), i , j);
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

        OptimizationWatcher watcher = new OptimizationWatcher();
        movers.forEach(PawnMover::start);
        watcher.start();
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
        private int currentPositionX;
        private int currentPositionY;
        private Boolean suspended;
        private boolean finished;

        PawnMover(PawnInterface pawn, int currentPositionX, int currentPositionY) {
            this.pawn = pawn;
            this.currentPositionX = currentPositionX;
            this.currentPositionY = currentPositionY;
            this.suspended = false;
            this.finished = false;
        }

        @Override
        public void run() {
            while(true) {
                int x = this.currentPositionX;
                int y = this.currentPositionY;

                while(suspended) {
                    lockers[x][y].lock.lock();
                    try {
                        lockers[x][y].cannotMove.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lockers[x][y].lock.unlock();
                    }
                }

                if(this.currentPositionX == meetingPointX && this.currentPositionY == meetingPointY)
                    break;

                if(finished)
                    break;

                if(this.currentPositionX < meetingPointX) {
                    if (this.currentPositionY < meetingPointY) {
                        checkIf2WayMovementIsPossible(x + 1, y, x, y + 1, "R", "D");
                    } else if(this.currentPositionY > meetingPointY) {
                        checkIf2WayMovementIsPossible(x + 1, y, x, y - 1, "R", "U");
                    } else {
                        checkIfMovementIsPossible( x + 1, y, "R");
                    }
                } else if(this.currentPositionX > meetingPointX) {
                    if (this.currentPositionY < meetingPointY) {
                        checkIf2WayMovementIsPossible(x - 1, y, x, y + 1, "L", "D");
                    } else if(this.currentPositionY > meetingPointY) {
                        checkIf2WayMovementIsPossible(x - 1, y, x, y - 1,"L", "U");
                    } else {
                        checkIfMovementIsPossible( x - 1, y, "L");
                    }
                } else {
                    if (this.currentPositionY < meetingPointY) {
                        checkIfMovementIsPossible( x, y + 1, "D");
                    } else {
                        checkIfMovementIsPossible( x, y - 1, "U");
                    }
                }
            }
        }

        public void finish() {
            int x = this.currentPositionX;
            int y = this.currentPositionY;

            lockers[x][y].lock.lock();

            try {
                if(!this.finished) {
                    lockers[x][y].cannotMove.signalAll();
                }
            } finally {
                lockers[x][y].lock.unlock();
            }
        }

        public synchronized void suspendThread(){
            System.out.println("Wstrzymuje prace watku: " + getName());
            suspended = true;
        }

        public void resumeThread(){
            lockers[this.currentPositionX][this.currentPositionY].lock.lock();
            try {
                System.out.println("Wznawiam prace watku: " + getName());
                suspended = false;
                lockers[this.currentPositionX][this.currentPositionY].cannotMove.signal();
            } finally {
                lockers[this.currentPositionX][this.currentPositionY].lock.unlock();
            }
        }

        private void movePawn(String direction, int x, int y) {
            switch (direction) {
                case "L":
                    moveLeft(x, y);
                    break;
                case "R":
                    moveRight(x, y);
                    break;
                case "U":
                    moveUp(x, y);
                    break;
                case "D":
                    moveDown(x, y);
                    break;
            }
        }

        private void moveLeft(int x, int y) {
            try {
                this.currentPositionX = pawn.moveLeft();
            } finally {
                lockers[x - 1][y].lock.unlock();
            }
        }

        private void moveRight(int x, int y) {
            try {
                this.currentPositionX = pawn.moveRight();
            } finally {
                lockers[x + 1][y].lock.unlock();
            }
        }

        private void moveUp(int x, int y) {
            try {
                this.currentPositionY = pawn.moveUp();
            } finally {
                lockers[x][y - 1].lock.unlock();
            }
        }

        private void moveDown(int x, int y) {
            try {
                this.currentPositionY = pawn.moveDown();
            } finally {
                lockers[x][y + 1].lock.unlock();
            }
        }

        private void wakeUpThreads(int x, int y) {
//            System.out.println(getName() + " budze watki czekajace na " + x + y);
            if(!lockers[x][y].waitingObjects.isEmpty()) {
                Locker locker = lockers[x][y].waitingObjects.remove();
                locker.lock.lock();
                try {
                    locker.cannotMove.signal();
                } finally {
                    locker.lock.unlock();
                }
            }
        }

        private void checkIfMovementIsPossible(int x, int y, String direction) {
            int currentX = this.currentPositionX;
            int currentY = this.currentPositionY;

            try {
                lockers[currentX][currentY].lock.lock();
                while(board.get(x, y).isPresent()) {
                    if(lockers[x][y].waitingObjects.contains(lockers[currentX][currentY])) {
                        this.finished = true;
                        return;
                    }
                    lockers[x][y].waitingObjects.add(lockers[currentX][currentY]);
//                    System.out.println(getName() + " czekam na: " + x + y);
                    lockers[currentX][currentY].cannotMove.await();
//                    System.out.println(getName() + " budze sie");
                }
                if(board.get(x, y).isEmpty() && lockers[x][y].lock.tryLock()) {
                    movePawn(direction, currentX, currentY);
                    wakeUpThreads(currentX, currentY);
                }
            } catch (InterruptedException e) {}
            finally {
                lockers[currentX][currentY].lock.unlock();
            }
        }

        private void checkIf2WayMovementIsPossible(int x1, int y1, int x2, int y2, String firstDirection, String secondDirection) {
            int x = this.currentPositionX;
            int y = this.currentPositionY;

            try {
                lockers[x][y].lock.lock();
                while(board.get(x1, y1).isPresent() && board.get(x2, y2).isPresent()) {
                    if(lockers[x1][y1].waitingObjects.contains(lockers[x][y]) &&
                        lockers[x2][y2].waitingObjects.contains(lockers[x][y])) {
                        this.finished = true;
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


