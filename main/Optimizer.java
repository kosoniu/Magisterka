package main;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Optimizer implements OptimizerInterface {

    private BoardInterface board;
    private ExecutorService executorService;
    private Set<Thread> movers;
    private int meetingPointX;
    private int meetingPointY;

    @Override
    public void setBoard(BoardInterface board) {
        this.board = board;

        this.executorService = Executors.newFixedThreadPool(this.board.getSize());
        this.meetingPointX = board.getMeetingPointRow();
        this.meetingPointY = board.getMeetingPointCol();
        this.movers = new HashSet<>();

        for(int i = 0; i < board.getSize(); i++) {
            for(int j = 0; j < board.getSize(); j++) {
                Optional<PawnInterface> pawn;
                try {
                    pawn = this.board.get(i,j);
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
                } catch (NullPointerException e) {}
            }
        }

        board.optimizationStart();
        movers.forEach(thread -> executorService.execute(thread));
    }

    @Override
    public void suspend() {
        executorService.shutdownNow();
    }

    @Override
    public void resume() {

    }

    private class PawnMover extends Thread {

        private PawnInterface pawn;
        private int currentPositionX;
        private int currentPositionY;


        PawnMover(PawnInterface pawn, int currentPositionX, int currentPositionY) {
            this.pawn = pawn;
            this.currentPositionX = currentPositionX;
            this.currentPositionY = currentPositionY;
        }

        @Override
        public void run() {
            while(isMovePossible()) {
                if(this.currentPositionY == meetingPointY) {
                    if(this.currentPositionX <= meetingPointX) {
                        this.currentPositionX = pawn.moveRight();
                    } else {
                        this.currentPositionX = pawn.moveLeft();
                    }
                } else if(this.currentPositionX == meetingPointX) {
                    if(this.currentPositionY <= meetingPointY) {
                        this.currentPositionY = pawn.moveDown();
                    } else {
                        this.currentPositionY = pawn.moveUp();
                    }
                }
            }
        }

        private boolean isMovePossible() {
            boolean isMovePossible = false;

            if(this.currentPositionX == meetingPointX && this.currentPositionY == meetingPointY)
                return isMovePossible;

            if(this.currentPositionY == meetingPointY) {
                if(this.currentPositionX <= meetingPointX) {
                    synchronized (board.get(this.currentPositionX + 1, this.currentPositionY)) {
                        isMovePossible = board.get(this.currentPositionX + 1, this.currentPositionY).isEmpty();
                    }
                } else {
                    synchronized (board.get(this.currentPositionX - 1, this.currentPositionY)) {
                        isMovePossible = board.get(this.currentPositionX - 1, this.currentPositionY).isEmpty();
                    }
                }
            } else if(this.currentPositionX == meetingPointX) {
                if(this.currentPositionY <= meetingPointY) {
                    synchronized (board.get(this.currentPositionX, this.currentPositionY + 1)) {
                        isMovePossible = board.get(this.currentPositionX, this.currentPositionY + 1).isEmpty();
                    }
                } else {
                    synchronized (board.get(this.currentPositionX, this.currentPositionY - 1)) {
                        isMovePossible = board.get(this.currentPositionX, this.currentPositionY - 1).isEmpty();
                    }
                }
            }

            return isMovePossible;
        }
    }
}
