package main;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
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


        board.optimizationStart();
        movers.forEach(PawnMover::start);

        boolean finished = false;

        while(!finished) {
            for(Thread thread: movers) {
                if(thread.getState() == Thread.State.TERMINATED || thread.getState() == Thread.State.WAITING) {
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

        movers.forEach(pawnMover -> System.out.println(pawnMover.getState()));

        board.optimizationDone();
    }

    @Override
    public void suspend() {
        movers.forEach(PawnMover::suspendThread);
    }

    @Override
    public void resume() {
        movers.forEach(PawnMover::resumeThread);
    }

    private class Locker {
        public final Queue<Locker> waitingObjects = new ConcurrentLinkedQueue<>();
        public final ReentrantLock lock = new ReentrantLock();
        public final Condition cannotMove = lock.newCondition();
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
                    try {
                        System.out.println("Wstrzymuje watek: " + getName());
                        lockers[x][y].cannotMove.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(this.currentPositionX == meetingPointX && this.currentPositionY == meetingPointY)
                    break;

                if(finished)
                    break;

                if(this.currentPositionX < meetingPointX) {
                    if (this.currentPositionY < meetingPointY) {
                        if(checkIfTwoWayMovementIsPossible(x + 1, y, x, y + 1)) {
                            if(board.get(x + 1, y).isEmpty()) {
                                movePawn("R", x, y);
                            } else if (board.get(x, y + 1).isEmpty()) {
                                movePawn("D", x, y);
                            }
                        }
                    } else if(this.currentPositionY > meetingPointY) {
                        if(checkIfTwoWayMovementIsPossible(x + 1, y, x, y - 1)) {
                            if(board.get(x + 1, y).isEmpty()) {
                                movePawn("R", x, y);
                            } else if(board.get(x, y - 1).isEmpty()) {
                                movePawn("U", x, y);
                            }
                        }
                    } else {
                        movePawn("R", x, y);
                    }
                } else if(this.currentPositionX > meetingPointX) {
                    if (this.currentPositionY < meetingPointY) {
                        if(checkIfTwoWayMovementIsPossible(x - 1, y, x, y + 1)) {
                            if(board.get(x - 1, y).isEmpty()) {
                                movePawn("L", x, y);
                            } else if(board.get(x, y + 1).isEmpty()) {
                                movePawn("D", x, y);
                            }
                        }
                    } else if(this.currentPositionY > meetingPointY) {
                        if(checkIfTwoWayMovementIsPossible(x - 1, y, x, y - 1)) {
                            if(board.get(x - 1, y).isEmpty()) {
                                movePawn("L", x, y);
                            } else if(board.get(x, y - 1).isEmpty()) {
                                movePawn("U", x, y);
                            }
                        }
                    } else {
                        movePawn("L", x, y);
                    }
                } else {
                    if (this.currentPositionY < meetingPointY) {
                        movePawn("D", x, y);
                    } else {
                        movePawn("U", x, y);
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
                    lockers[x][y].cannotMove.signal();
                }
            } finally {
                lockers[x][y].lock.unlock();
            }
        }

        public synchronized void suspendThread(){
            suspended = true;
        }

        public void resumeThread(){
            lockers[this.currentPositionX][this.currentPositionY].lock.lock();
            try {
                System.out.println("Wznawiam prace watku: " + getName());
                suspended = false;
                lockers[this.currentPositionX][this.currentPositionY].cannotMove.signalAll();
            } finally {
                lockers[this.currentPositionX][this.currentPositionY].lock.unlock();
            }
        }

        private void movePawn(String direction, int x, int y) {
            lockers[x][y].lock.lock();

            try {
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

            } finally {
                lockers[x][y].lock.unlock();
            }

            wakeUpThreads(x, y);
        }

        private void moveLeft(int x, int y) {
            if(checkIfMovementIsPossible(x - 1, y))
                this.currentPositionX = pawn.moveLeft();
//            System.out.println(getName() + " ide w lewo");

        }

        private void moveRight(int x, int y) {
            if(checkIfMovementIsPossible(x + 1, y))
                this.currentPositionX = pawn.moveRight();
//            System.out.println(getName() + " ide w prawo");

        }

        private void moveUp(int x, int y) {
            if(checkIfMovementIsPossible(x, y - 1))
                this.currentPositionY = pawn.moveUp();
//            System.out.println(getName() + " ide do gory");

        }

        private void moveDown(int x, int y) {
            if(checkIfMovementIsPossible(x, y + 1))
                this.currentPositionY = pawn.moveDown();
//            System.out.println(getName() + " ide na dol");
        }

        private void wakeUpThreads(int x, int y) {
            while(!lockers[x][y].waitingObjects.isEmpty()) {
                Locker locker = lockers[x][y].waitingObjects.poll();
                if(locker != null) {
                    locker.lock.lock();
                    try {
                        locker.cannotMove.signalAll();
                    } finally {
                        locker.lock.unlock();
                    }
                }
            }
        }

        private boolean checkIfMovementIsPossible(int x, int y) {
            try {
                while(board.get(x, y).isPresent() || suspended) {
                    if(lockers[x][y].waitingObjects.contains(lockers[this.currentPositionX][this.currentPositionY])) {
                        this.finished = true;
                        return false;
                    }

//                    System.out.println(getName() + " czekam");
                    lockers[x][y].waitingObjects.add(lockers[this.currentPositionX][this.currentPositionY]);
                    lockers[this.currentPositionX][this.currentPositionY].cannotMove.await();
                }
            } catch (InterruptedException e) {}

            return true;
        }

        private boolean checkIfTwoWayMovementIsPossible(int x1, int y1, int x2, int y2) {
            lockers[this.currentPositionX][this.currentPositionY].lock.lock();

            try {
                while((board.get(x1, y1).isPresent() && board.get(x2, y2).isPresent()) || suspended) {
                    if(lockers[x1][y1].waitingObjects.contains(lockers[this.currentPositionX][this.currentPositionY]) &&
                            lockers[x2][y2].waitingObjects.contains(lockers[this.currentPositionX][this.currentPositionY])) {
                        this.finished = true;
                        return false;
                    }
//                    System.out.println(getName() + " czekam");
                    lockers[x1][y1].waitingObjects.add(lockers[this.currentPositionX][this.currentPositionY]);
                    lockers[x2][y2].waitingObjects.add(lockers[this.currentPositionX][this.currentPositionY]);
                    lockers[this.currentPositionX][this.currentPositionY].cannotMove.await();
                }
            } catch (InterruptedException e) {} finally {
                lockers[this.currentPositionX][this.currentPositionY].lock.unlock();
            }

            return true;
        }

    }
}


