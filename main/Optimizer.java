package main;

import javafx.util.Pair;

import javax.management.monitor.CounterMonitor;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Optimizer implements OptimizerInterface {

    private BoardInterface board;
    private Locker[][] lockers;
    private ThreadPoolExecutor executorService;
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

        this.executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(movers.size());
        System.out.println(movers.size());

        board.optimizationStart();
//        movers.forEach(thread -> executorService.submit(thread));
        movers.forEach(PawnMover::start);

        boolean finished = false;

        try {
            Thread.sleep(7000);
//            movers.forEach(pawnMover -> System.out.println(pawnMover.getName() + " " + pawnMover.getState()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


//        while(!finished) {
//            for(Thread thread: movers) {
//                if(thread.getState() == Thread.State.WAITING || thread.getState() == Thread.State.TERMINATED) {
//                    finished = true;
//                } else {
//                    finished = false;
//                    break;
//                }
//            }
//        }

//        finished = false;
//        movers.forEach(PawnMover::finish);
//
//        for(int i = 0; i < board.getSize(); i++) {
//            for (int j = 0; j < board.getSize(); j++) {
//                lockers[i][j].lock.lock();
//                try {
//                    lockers[i][j].canMove.signalAll();
//                } finally {
//                    lockers[i][j].lock.unlock();
//                }
//            }
//        }
//
//        movers.forEach(pawnMover -> System.out.println(pawnMover.getState()));
//
//        while(!finished) {
//            for(Thread thread: movers) {
//                if(thread.getState() == Thread.State.TERMINATED) {
//                    finished = true;
//                } else {
//                    finished = false;
//                    break;
//                }
//            }
//        }

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
//        public final Queue<Locker> waitingObjects = new ConcurrentLinkedQueue<>();
        public final Queue<Locker> waitingObjects = new LinkedList<>();
        public final ReentrantLock lock = new ReentrantLock();
        public final Condition cannotMove = lock.newCondition();
        public final Condition canMove = lock.newCondition();
    }

    private class PawnMover extends Thread {

        private PawnInterface pawn;
        private int currentPositionX;
        private int currentPositionY;
        private Boolean suspended;
        private boolean finished;
        private final Object lock = new Object();

        PawnMover(PawnInterface pawn, int currentPositionX, int currentPositionY) {
            this.pawn = pawn;
            this.currentPositionX = currentPositionX;
            this.currentPositionY = currentPositionY;
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
                        checkIfTwoWayMovementIsPossible(x, y,x + 1, y, x, y + 1);

                        if(board.get(x + 1, y).isEmpty()) {
                            tryMoveRight(x, y);
                        } else if (board.get(x, y + 1).isEmpty()) {
                            tryMoveDown(x, y);
                        } else {
                        }
                    } else if(this.currentPositionY > meetingPointY) {
                        checkIfTwoWayMovementIsPossible(x, y,x + 1, y, x, y - 1);

                        if(board.get(x + 1, y).isEmpty()) {
                            tryMoveRight(x, y);
                        } else if(board.get(x, y - 1).isEmpty()) {
                            tryMoveUp(x, y);
                        } else {
                        }
                    } else {
                        tryMoveRight(x, y);
                    }
                } else if(this.currentPositionX > meetingPointX) {
                    if (this.currentPositionY < meetingPointY) {
                        checkIfTwoWayMovementIsPossible(x, y,x - 1, y, x, y + 1);

                        if(board.get(x - 1, y).isEmpty()) {
                            tryMoveLeft(x, y);
                        } else if(board.get(x, y + 1).isEmpty()) {
                            tryMoveDown(x, y);
                        } else {
                        }
                    } else if(this.currentPositionY > meetingPointY) {
                        checkIfTwoWayMovementIsPossible(x, y, x - 1, y, x, y - 1);

                        if(board.get(x - 1, y).isEmpty()) {
                            tryMoveLeft(x, y);
                        } else if(board.get(x, y - 1).isEmpty()) {
                            tryMoveUp(x, y);
                        } else {
                        }
                    } else {
                        tryMoveLeft(x, y);
                    }
                } else {
                    if (this.currentPositionY < meetingPointY) {
                        tryMoveDown(x, y);
                    } else {
                        tryMoveUp(x, y);
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
//            lockers[x - 1][y].lock.lock();
            lockers[x][y].lock.lock();

            try {
                checkIfMovementIsPossible(x - 1, y);

                System.out.println(getName() + " przesuwam pionek w lewo");
                this.currentPositionX = pawn.moveLeft();
//                lockers[x - 1][y].cannotMove.signal();
                lockers[x][y].cannotMove.signalAll();
                wakeUpThreads(x, y);

            } finally {
//                lockers[x - 1][y].lock.unlock();
                lockers[x][y].lock.unlock();
            }
        }

        private void tryMoveRight(int x, int y) {
//            lockers[x + 1][y].lock.lock();
            lockers[x][y].lock.lock();

            try {
                checkIfMovementIsPossible(x + 1, y);

                System.out.println(getName() + " przesuwam pionek w prawo");
                this.currentPositionX = pawn.moveRight();
//                lockers[x + 1][y].cannotMove.signal();
                lockers[x][y].cannotMove.signalAll();
                wakeUpThreads(x, y);

            } finally {
//                lockers[x + 1][y].lock.unlock();
                lockers[x][y].lock.unlock();
            }
        }

        private void tryMoveDown(int x, int y) {
//            lockers[x][y + 1].lock.lock();
            lockers[x][y].lock.lock();

            try {
                checkIfMovementIsPossible(x, y + 1);

                System.out.println(getName() + " przesuwam pionek w dol");
                this.currentPositionY = pawn.moveDown();
//                lockers[x][y + 1].cannotMove.signal();
                lockers[x][y].cannotMove.signalAll();
                wakeUpThreads(x, y);

            } finally {
//                lockers[x][y + 1].lock.unlock();
                lockers[x][y].lock.unlock();
            }
        }

        private void tryMoveUp(int x, int y) {
//            lockers[x][y - 1].lock.lock();
            lockers[x][y].lock.lock();

            try {
                checkIfMovementIsPossible(x, y - 1);

                System.out.println(getName() + " przesuwam pionek w gore");
                this.currentPositionY = pawn.moveUp();
//                lockers[x][y - 1].cannotMove.signal();
                lockers[x][y].cannotMove.signalAll();
                wakeUpThreads(x, y);
            } finally {
//                lockers[x][y - 1].lock.unlock();
                lockers[x][y].lock.unlock();
            }
        }

        private void wakeUpThreads(int x, int y) {
//            lockers[x][y].lock.lock();
//            try {
//                lockers[x][y].cannotMove.signalAll();
//            } finally {
//                lockers[x][y].lock.unlock();
//            }

            while(!lockers[x][y].waitingObjects.isEmpty()) {
                    Locker locker = lockers[x][y].waitingObjects.remove();
                    locker.lock.lock();

                    try {
                        locker.cannotMove.signalAll();
                    } finally {
                        locker.lock.unlock();
                    }
                }

        }

        private void checkIfMovementIsPossible(int x, int y) {
            lockers[x][y].lock.lock();

            try {
                while(board.get(x, y).isPresent()) {
                    if(finished)
                        break;

                    try {
                        System.out.println(getName() + " czekam");
                        lockers[x][y].cannotMove.await();
                        System.out.println(getName() + " budze sie");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                lockers[x][y].lock.unlock();
            }
        }

        private void checkIfTwoWayMovementIsPossible(int x0, int y0, int x1, int y1, int x2, int y2) {
            lockers[x0][y0].lock.lock();

            try {
                while(board.get(x1, y1).isPresent() && board.get(x2, y2).isPresent()) {
                    if(finished)
                        break;

                    System.out.println(getName() + " czekam w 2 kierunkach");
                    try {
                        lockers[x1][y1].lock.lock();
                        lockers[x2][y2].lock.lock();

                        try {
                            lockers[x1][y1].waitingObjects.add(lockers[x0][y0]);
                            lockers[x2][y2].waitingObjects.add(lockers[x0][y0]);
                        } finally {
                            lockers[x1][y1].lock.unlock();
                            lockers[x2][y2].lock.unlock();
                        }

                        lockers[x0][y0].cannotMove.await();
                        System.out.println(getName() + " budze sie");
//                        lockers[x1][y1].waitingObjects.remove(lockers[x0][y0]);
//                        lockers[x2][y2].waitingObjects.remove(lockers[x0][y0]);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                lockers[x0][y0].lock.unlock();
            }

        }

    }
}


