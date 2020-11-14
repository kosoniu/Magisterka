package main;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockOptimizer implements OptimizerInterface {

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
                    pawnMover.setSuspended(false);
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
                if(thread.getState() == Thread.State.WAITING || thread.getState() == Thread.State.TERMINATED) {
                    finished = true;
                } else {
                    finished = false;
                    break;
                }
            }

            if(finished) {
                movers.forEach(PawnMover::finish);
            }
        }


//        try {
//            Thread.sleep(7000);
//            for(Thread thread: movers) {
//                System.out.println(thread.getName() + " " + thread.getState());
//            }
//        } catch (Exception e){}
        board.optimizationDone();
    }

    @Override
    public void suspend() {
        movers.forEach(PawnMover::suspendThread);
//        movers.forEach(thread -> System.out.println(thread.getState()));
    }

    @Override
    public void resume() {
        movers.forEach(PawnMover::resumeThread);
//        movers.forEach(thread -> System.out.println(thread.getState()));
    }

    private class Locker {
        public final Lock lock = new ReentrantLock();
        public final Condition canMove = lock.newCondition();
        public final Condition cannotMove = lock.newCondition();
    }

    private class PawnMover extends Thread {

        private PawnInterface pawn;
        private int currentPositionX;
        private int currentPositionY;
        private Boolean suspended;
        private final Object lock = new Object();
        private boolean finished;

        PawnMover(PawnInterface pawn, int currentPositionX, int currentPositionY) {
            this.pawn = pawn;
            this.currentPositionX = currentPositionX;
            this.currentPositionY = currentPositionY;
            this.finished = false;
        }

        @Override
        public void run() {
            while(true) {
                while(suspended) {
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if(this.currentPositionX == meetingPointX && this.currentPositionY == meetingPointY)
                    break;

                if(finished)
                    break;

                int x = this.currentPositionX;
                int y = this.currentPositionY;

                if(this.currentPositionX < meetingPointX) {
                    if (this.currentPositionY < meetingPointY) {
                        lockers[x + 1][y].lock.lock();
                        lockers[x][y + 1].lock.lock();
                         try {
                             if(board.get(x + 1, y).isEmpty()) {
                                 tryMoveRight(x + 1, y);
                             } else if(board.get(x, y + 1).isEmpty()) {
                                 tryMoveDown(x, y + 1);
                             } else {
                                 checkIfMovementIsPossible(x + 1, y);
                             }
                         } finally {
                             lockers[x + 1][y].lock.unlock();
                             lockers[x][y + 1].lock.unlock();
                         }
                    } else if(this.currentPositionY > meetingPointY) {
                        lockers[x + 1][y].lock.lock();
                        lockers[x][y - 1].lock.lock();

                        try {
                            if(board.get(x + 1, y).isEmpty()) {
                                tryMoveRight(x + 1, y);
                            } else if(board.get(x, y - 1).isEmpty()) {
                                tryMoveUp(x, y - 1);
                            } else {
                                checkIfMovementIsPossible(x + 1, y);
                            }
                        } finally {
                            lockers[x + 1][y].lock.unlock();
                            lockers[x][y - 1].lock.unlock();
                        }
                    } else {
                        lockers[x + 1][y].lock.lock();

                        try {
                            if(board.get(x + 1, y).isEmpty()) {
                                tryMoveRight(x + 1, y);
                            } else {
                                checkIfMovementIsPossible(x + 1, y);
                            }
                        } finally {
                            lockers[x + 1][y].lock.unlock();
                        }
                    }
                } else if(this.currentPositionX > meetingPointX) {
                    if (this.currentPositionY < meetingPointY) {
                        lockers[x - 1][y].lock.lock();
                        lockers[x][y + 1].lock.lock();

                        try {
                            if(board.get(x - 1, y).isEmpty()) {
                                tryMoveLeft(x - 1, y);
                            } else if(board.get(x, y + 1).isEmpty()) {
                                tryMoveDown(x, y + 1);
                            } else {
                                checkIfMovementIsPossible(x - 1, y);
                            }
                        } finally {
                            lockers[x - 1][y].lock.unlock();
                            lockers[x][y + 1].lock.unlock();
                        }
                    } else if(this.currentPositionY > meetingPointY) {
                        lockers[x - 1][y].lock.lock();
                        lockers[x][y - 1].lock.lock();

                        try {
                            if(board.get(x - 1, y).isEmpty()) {
                                tryMoveLeft(x - 1, y);
                            } else if(board.get(x, y - 1).isEmpty()) {
                                tryMoveUp(x, y - 1);
                            } else {
                                checkIfMovementIsPossible(x - 1, y);
                            }
                        } finally {
                            lockers[x - 1][y].lock.unlock();
                            lockers[x][y - 1].lock.unlock();
                        }
                    } else {
                        lockers[x - 1][y].lock.lock();

                        try {
                            if(board.get(x - 1, y).isEmpty()) {
                                tryMoveLeft(x - 1, y);
                            } else {
                                checkIfMovementIsPossible(x - 1, y);
                            }
                        } finally {
                            lockers[x - 1][y].lock.unlock();
                        }
                    }
                } else {
                    if (this.currentPositionY < meetingPointY) {
                        lockers[x][y + 1].lock.lock();

                        try {
                            if(board.get(x, y + 1).isEmpty()) {
                                tryMoveDown(x, y + 1);
                            } else {
                                checkIfMovementIsPossible(x, y + 1);
                            }
                        } finally {
                            lockers[x][y + 1].lock.unlock();

                        }
                    } else {
                        lockers[x][y - 1].lock.lock();

                        try {
                            if(board.get(x, y - 1).isEmpty()) {
                                tryMoveUp(x, y - 1);
                            } else {
                                checkIfMovementIsPossible(x, y - 1);
                            }
                        } finally {
                            lockers[x][y - 1].lock.unlock();
                        }
                    }
                }
            }
        }

        public void finish() {
            this.finished = true;
        }

        public void suspendThread(){
            suspended = true;
        }

        public void resumeThread(){
            suspended = false;
            synchronized (lock) {
                lock.notify();
            }
        }

        public void setSuspended(boolean suspended) {
            this.suspended = suspended;
        }

        private void tryMoveLeft(int x, int y) {
            checkIfMovementIsPossible(x, y);

            try {
                System.out.println(getName() + " przesuwam pionek w lewo");
                this.currentPositionX = pawn.moveLeft();
            } finally {
                lockers[x][y].cannotMove.signalAll();
            }
        }

        private void tryMoveRight(int x, int y) {
            checkIfMovementIsPossible(x, y);

            try {
                System.out.println(getName() + " przesuwam pionek w prawo");
                this.currentPositionX = pawn.moveRight();
            } finally {
                lockers[x][y].cannotMove.signalAll();
            }
        }

        private void tryMoveDown(int x, int y) {
            checkIfMovementIsPossible(x, y);

            try {
                System.out.println(getName() + " przesuwam pionek w dol");
                this.currentPositionX = pawn.moveDown();
            } finally {
                lockers[x][y].cannotMove.signalAll();
            }
        }

        private void tryMoveUp(int x, int y) {
            checkIfMovementIsPossible(x, y);

            try {
                System.out.println(getName() + " przesuwam pionek w gore");
                this.currentPositionX = pawn.moveUp();
            } finally {
                lockers[x][y].cannotMove.signalAll();
            }
        }

        private void checkIfMovementIsPossible(int x, int y) {
            while(board.get(x, y).isPresent()) {
                try {
                    System.out.println(getName() + " czekam");
                    lockers[x][y].cannotMove.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


