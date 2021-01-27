import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ExampleBoard {

    public static void main(String[] args) {
        ExampleBoard board = new ExampleBoard();

        List<ExampleBoard.Pawn> pawns = new ArrayList<>();
        pawns.add(board.new Pawn(0));
        pawns.add(board.new Pawn(1));
        pawns.add(board.new Pawn(3));
        pawns.add(board.new Pawn(4));
        pawns.add(board.new Pawn(6));
        pawns.add(board.new Pawn(34));
        pawns.add(board.new Pawn(35));
        pawns.add(board.new Pawn(37));
        pawns.add(board.new Pawn(38));
        pawns.add(board.new Pawn(40));
        pawns.add(board.new Pawn(50));
        pawns.add(board.new Pawn(60));
        pawns.add(board.new Pawn(61));

        ExampleBoard.Solusion solution = board.new Solusion(pawns);

        while (true) {
            showBoard(board.board);
            ExampleBoard.sleep(750);
        }
    }

    static enum Direction {
        LEFT(-1), RIGHT(+1);

        final int delta;

        private Direction(int delta) {
            this.delta = delta;
        }

        public int getNextPosition(int position) {
            return position + delta;
        }
    }

    static String getThreadName() {
        return Thread.currentThread().getName();
    }

    static void sleep(long msec) {
        try {
            Thread.sleep(msec);
        } catch (Exception e) {
        }
    }

    static void sleep() {
        sleep(SLEEP);
    }

    class Pawn {
        int position;
        Direction direction;

        Pawn(int position) {
            this.position = position;
            if (position > randevu) {
                direction = Direction.LEFT;
            } else {
                direction = Direction.RIGHT;
            }
            board.get(position).set(this);
        }

        public int getNextPosition() {
            return direction.getNextPosition(position);
        }

        public int move() {
            int nextPosition = getNextPosition();
            System.out.println("Zlecono wykonanie ruchu z " + position + " na " + nextPosition + " za pomocÄ wÄtku "
                    + getThreadName());

            sleep();

            synchronized (board) {
                board.get(position).set(null);
                board.get(nextPosition).set(this);
            }

            System.out.println(
                    "Wykonono ruch z " + position + " na " + nextPosition + " za pomocÄ wÄtku " + getThreadName());

            position = nextPosition;
            return nextPosition;
        }
    }

    class Solusion {
        boolean current[] = new boolean[SIZE];
        boolean next[] = new boolean[SIZE];

        class PawnMover implements Runnable {
            Pawn pawn;

            public PawnMover(Pawn pawn) {
                this.pawn = pawn;
                synchronized (Solusion.class) {
                    current[pawn.position] = true;
                }
            }

            @Override
            public void run() {
                while (true) {
                    if (pawn.position == randevu)
                        return;
                    int currentPosition = pawn.position;
                    int nextPosition = pawn.direction.getNextPosition(currentPosition );
                    synchronized (Solusion.class) {
                        while (next[nextPosition] || current[nextPosition]) {
                            try {
                                //	System.out.println("Tu wÄtek " + getThreadName() + " nie mogÄ siÄ ruszyÄ - idÄ spaÄ");
                                Solusion.class.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        next[nextPosition] = true; // rezerwacja pozycji, na ktora ide
                    } // synchronized

                    pawn.move(); // move poza sekcja synchronized

                    synchronized (Solusion.class) {
                        next[nextPosition] = false;
                        current[nextPosition] = true;
                        current[currentPosition] = false;
                        Solusion.class.notifyAll();
                    }
                }
            }
        }

        Solusion(List<ExampleBoard.Pawn> pawns) {
            pawns.forEach(p -> {
                new Thread(new PawnMover(p)).start();
            });
        }
    }

    private static final int SIZE = 64;
    private static final int randevu = 25;
    private static final int SLEEP = 1000;
    private final List<AtomicReference<Pawn>> board = new ArrayList<AtomicReference<Pawn>>(SIZE);

    {
        for ( int i = 0; i < SIZE; i++ )
            board.add( new AtomicReference<ExampleBoard.Pawn>());
    }

    private static void showBoard(List<AtomicReference<Pawn>> board) {
        System.out.print("[");
        board.forEach(ar -> {
            Pawn p = ar.get();
            if (p == null) {
                System.out.print(".");
            } else {
                System.out.print("*");
            }
        });
        System.out.println("]");
    }


}