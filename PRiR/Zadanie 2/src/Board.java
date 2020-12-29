import java.util.Optional;

public class Board implements BoardInterface {

    private PawnInterface board[][];
    private int meetingPointCol;
    private int meetingPointRow;
    private int size;
    public boolean optimizationStarted;
    public boolean optimizationEnded;

    public Board(int meetingPointCol, int meetingPointRow, int size) {
        this.meetingPointCol = meetingPointCol;
        this.meetingPointRow = meetingPointRow;
        this.size = size;
        this.board = new PawnInterface[size][size];
    }

    public Board(PawnInterface board[][], int meetingPointCol, int meetingPointRow) {
        this.board = board;
        this.meetingPointCol = meetingPointCol;
        this.meetingPointRow = meetingPointRow;
        this.size = board[0].length;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Optional<PawnInterface> get(int col, int row) {
        return Optional.ofNullable(this.board[col][row]);
    }

    @Override
    public int getMeetingPointCol() {
        return this.meetingPointCol;
    }

    @Override
    public int getMeetingPointRow() { return this.meetingPointRow; }

    @Override
    public void optimizationDone() {
        this.optimizationEnded = true;
    }

    @Override
    public void optimizationStart() {
        this.optimizationStarted = false;
    }
}
