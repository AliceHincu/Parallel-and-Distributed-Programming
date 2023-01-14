import java.io.File;
import java.io.IOException;
import java.util.*;

public class Matrix {
    private static final byte[] moveVertically = new byte[]{0, -1, 0, 1};
    private static final byte[] moveHorizontally = new byte[]{-1, 0, 1, 0};
    private static final String[] MOVES = new String[]{"left", "up", "right", "down"};
    private static final String fileName = "src/puzzle.in";
    private static final int NR_ROWS = 4;
    private static final int NR_COLS = 4;
    private static final int FREE_SPACE_VALUE = 0;
    private final int freeSpaceRow;
    private final int freeSpaceCol;
    private int[][] values;
    public final int manhattan;
    private final Matrix previousState;
    public final int numberOfSteps;
    private final String move;

    public Matrix(int[][] values, int freeSpaceRow, int freeSpaceCol, int numberOfSteps, Matrix previousState, String move) {
        this.values = values;
        this.freeSpaceRow = freeSpaceRow;
        this.freeSpaceCol = freeSpaceCol;
        this.numberOfSteps = numberOfSteps;
        this.previousState = previousState;
        this.move = move;
        this.manhattan = manhattanDistance();
    }

    /**
     * The sum where each term is how far is a value from its correct position with both vertical and horizontal
     * measures. This is a heuristic.
     *
     * @return Manhattan Distance for 15 puzzle problem
     */
    public int manhattanDistance() {
        int sum = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (values[i][j] != 0) {
                    int targetI = (values[i][j] - 1) / 4;
                    int targetJ = (values[i][j] - 1) % 4;
                    sum += Math.abs(i - targetI) + Math.abs(j - targetJ);
                }
            }
        }
        return sum;
    }

    /**
     * Generate a list of possible states performing one of the "left", "up", "right", "down" moves.
     *
     * @return The list of possible future states
     */
    public List<Matrix> generatePossibleMoves() {
        List<Matrix> possibleFutureStates = new ArrayList<>();
        for (int k = 0; k < 4; k++) {
            if (freeSpaceRow + moveVertically[k] >= 0 && freeSpaceRow + moveVertically[k] < 4 && freeSpaceCol + moveHorizontally[k] >= 0 && freeSpaceCol + moveHorizontally[k] < 4) {
                int movedFreePosI = freeSpaceRow + moveVertically[k];
                int movedFreePosJ = freeSpaceCol + moveHorizontally[k];
                if (previousState != null && movedFreePosI == previousState.freeSpaceRow && movedFreePosJ == previousState.freeSpaceCol)
                    continue;

                int[][] movedTiles = Arrays.stream(values)
                        .map(int[]::clone)
                        .toArray(int[][]::new);
                movedTiles[freeSpaceRow][freeSpaceCol] = movedTiles[movedFreePosI][movedFreePosJ];
                movedTiles[movedFreePosI][movedFreePosJ] = FREE_SPACE_VALUE;

                possibleFutureStates.add(new Matrix(movedTiles, movedFreePosI, movedFreePosJ, numberOfSteps + 1, this, MOVES[k]));
            }
        }
        return possibleFutureStates;
    }

    public static Matrix readFromFile() throws IOException {
        int freeI = -1, freeJ = -1;
        int[][] matrix = new int[NR_ROWS][NR_COLS];
        Scanner input = new Scanner(new File(fileName));

        for (int i = 0; i < NR_ROWS; ++i) {
            for (int j = 0; j < NR_COLS; ++j) {
                if (input.hasNextInt()) {
                    matrix[i][j] = input.nextInt();
                    if (matrix[i][j] == FREE_SPACE_VALUE) {
                        freeI = i;
                        freeJ = j;
                    }
                }
            }
        }

        return new Matrix(matrix, freeI, freeJ, 0, null, "");
    }

    /**
     * Go from the final state in each previous state, append them to the result and reverse the result in order to
     * start with the initial state and end with the final one.
     * @return The string representation
     */
    @Override
    public String toString() {
        Matrix current = this;
        List<String> strings = new ArrayList<>();
        while (current != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            sb.append(current.move);
            sb.append("\n");
            Arrays.stream(current.values).forEach(row -> sb.append(Arrays.toString(row)).append("\n"));
            strings.add(sb.toString());
            current = current.previousState;
        }
        Collections.reverse(strings);
        return "Moves\n" +
                String.join("", strings) + "\n" +
                numberOfSteps + " steps";
    }
}
