package main;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Optimizer implements OptimizerInterface {

    private BoardInterface board;
    private Object[][] lockers;
    private ExecutorService executorService;
    private Set<PawnMover> movers;
    private int meetingPointX;
    private int meetingPointY;

    @Override
    public void setBoard(BoardInterface board) {
        this.board = board;

        this.meetingPointX = board.getMeetingPointCol();
        this.meetingPointY = board.getMeetingPointRow();
        this.movers = new HashSet<>();
        this.lockers = new Object[this.board.getSize()][this.board.getSize()];

        for(int i = 0; i < board.getSize(); i++) {
            for(int j = 0; j < board.getSize(); j++) {
                this.lockers[i][j] = new Object();
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

        this.executorService = Executors.newFixedThreadPool(movers.size());
        System.out.println(movers.size());

        board.optimizationStart();
        movers.forEach(thread -> executorService.execute(thread));

//        executorService.shutdown();

//        boolean finished = false;
//
//        while(!finished) {
//            for(Thread thread: movers) {
//                System.out.println(thread.getState());
//                if(thread.isAlive())
//                    break;
//                finished = true;
//                board.optimizationDone();
//            }
//        }

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

    private class PawnMover extends Thread {

        private PawnInterface pawn;
        private int currentPositionX;
        private int currentPositionY;
        private Boolean suspended;
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

                int x = this.currentPositionX;
                int y = this.currentPositionY;

                if(this.currentPositionX < meetingPointX) {
                    if (this.currentPositionY < meetingPointY) {
                        synchronized (lockers[x + 1][y]) {
                            synchronized (lockers[x][y + 1]) {
                                if(board.get(x + 1, y).isEmpty()) {
                                    tryMoveRight(x + 1, y);
                                } else if(board.get(x, y + 1).isEmpty()) {
                                    tryMoveDown(x, y + 1);
                                }
                            }
                        }
                    } else if(this.currentPositionY > meetingPointY) {
                        synchronized (lockers[x + 1][y]) {
                            synchronized (lockers[x][y - 1]) {
                                if(board.get(x + 1, y).isEmpty()) {
                                    tryMoveRight(x + 1, y);
                                } else if(board.get(x, y - 1).isEmpty()) {
                                    tryMoveUp(x, y - 1);
                                }
                            }
                        }
                    } else {
                        synchronized (lockers[x + 1][y]) {
                            tryMoveRight(x + 1, y);
                        }
                    }
                } else if(this.currentPositionX > meetingPointX) {
                    if (this.currentPositionY < meetingPointY) {
                        synchronized (lockers[x - 1][y]) {
                            synchronized (lockers[x][y + 1]) {
                                if(board.get(x - 1, y).isEmpty()) {
                                    tryMoveLeft(x - 1, y);
                                } else if(board.get(x, y + 1).isEmpty()) {
                                    tryMoveDown(x, y + 1);
                                }
                            }
                        }
                    } else if(this.currentPositionY > meetingPointY) {
                        synchronized (lockers[x - 1][y]) {
                            synchronized (lockers[x][y - 1]) {
                                if(board.get(x - 1, y).isEmpty()) {
                                    tryMoveLeft(x - 1, y);
                                } else if(board.get(x, y - 1).isEmpty()) {
                                    tryMoveUp(x, y - 1);
                                }
                            }
                        }
                    } else {
                        synchronized (lockers[x - 1][y]) {
                            tryMoveLeft(x - 1, y);
                        }
                    }
                } else {
                    if (this.currentPositionY < meetingPointY) {
                        synchronized (lockers[x][y + 1]) {
                            tryMoveDown(x, y + 1);
                        }
                    } else if(this.currentPositionY > meetingPointY) {
                        synchronized (lockers[x][y - 1]) {
                            tryMoveUp(x, y - 1);
                        }
                    }
                }
            }
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

            System.out.println(getName() + " przesuwam pionek w lewo");
            this.currentPositionX = pawn.moveLeft();
            lockers[x][y].notifyAll();
        }

        private void tryMoveRight(int x, int y) {
            checkIfMovementIsPossible(x, y);

            System.out.println(getName() + " przesuwam pionek w prawo");
            this.currentPositionX = pawn.moveRight();
            lockers[x][y].notifyAll();
        }

        private void tryMoveDown(int x, int y) {
            checkIfMovementIsPossible(x, y);

            System.out.println(getName() + " przesuwam pionek w dol");
            this.currentPositionY = pawn.moveDown();
            lockers[x][y].notifyAll();
        }

        private void tryMoveUp(int x, int y) {
            checkIfMovementIsPossible(x, y);

            System.out.println(getName() + " przesuwam pionek w gore");
            this.currentPositionY = pawn.moveUp();
            lockers[x][y].notifyAll();
        }

        private void checkIfMovementIsPossible(int x, int y) {
            while(board.get(x, y).isPresent()) {
                try {
                    System.out.println(getName() + " czekam");
                    lockers[x][y].wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


