import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    private static final int NR_THREADS = 5;
    private static final int NR_TASKS = 5;
    // For the 15 puzzle, lengths of optimal solutions range from 0 to 80 single-tile moves (there are 17 configurations requiring 80 moves)
    private static final int MINIMUM_OPTIMAL_BOUND = 80;
    private static ExecutorService executorService;

    // https://www.rgpv.ac.in/campus/CS/cs_402%20ADA_Branch_Bound%20Technique_tsp_15puzzel.pdf
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Matrix initialState = Matrix.readFromFile();
        executorService = Executors.newFixedThreadPool(NR_THREADS);

        Matrix solution = solve(initialState);
        System.out.println(solution);

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
    public boolean isSolvable(int[][] puzzle)
    {
        int parity = 0;
        int gridWidth = (int) Math.sqrt(puzzle.length);
        int row = 0; // the current row we are on
        int blankRow = 0; // the row with the blank tile

        if (gridWidth % 2 == 0) { // even grid
            if (blankRow % 2 == 0) { // blank on odd row; counting from bottom
                return parity % 2 == 0;
            } else { // blank on even row; counting from bottom
                return parity % 2 != 0;
            }
        } else { // odd grid
            return parity % 2 == 0;
        }
    }


    /**
     *
     * @param root
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static Matrix solve(Matrix root) throws ExecutionException, InterruptedException {
        long time = System.currentTimeMillis();
        int minimumBound = root.manhattan;
        System.out.println(minimumBound);
        int distance;

        while (true) {
            Pair<Integer, Matrix> solution = searchParallel(root, 0, minimumBound, NR_TASKS);
            distance = solution.getFirst();
            if (distance == -1) {
                System.out.println(solution.getSecond().numberOfSteps + " steps - " + (System.currentTimeMillis() - time) + "ms");
                return solution.getSecond();
            } else {
                System.out.println(distance + " steps - " + (System.currentTimeMillis() - time) + "ms");
            }
            minimumBound = distance;
        }
    }

    /**
     *
     * @param current - the current state of the matrix
     * @param numSteps - how many moves of a tile the program did until now
     * @param bound - minimum bound
     * @param nrThreads - number of threads
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static Pair<Integer, Matrix> searchParallel(Matrix current, int numSteps, int bound, int nrThreads) throws ExecutionException, InterruptedException {
        // if nrThreads is 1, search sequential.
        if (nrThreads <= 1)
            return search(current, numSteps, bound);

        // check if current state can be a possible state towards the solution
        var checkResult = checkEstimation(numSteps + current.manhattan, bound, current);
        if (checkResult != null) return checkResult;

        // call method for the possible states to search for solution
        List<Matrix> moves = current.generatePossibleMoves();
        var futures = moves.stream()
                .map(next -> executorService.submit(() -> searchParallel(next, numSteps + 1, bound, nrThreads / moves.size())))
                .toList();

        // get results for each search. If the distance is -1, the solution was found. Else, we return the minimum
        // compare to the minimum and store the faster solution
        int minimum = Integer.MAX_VALUE;
        for (Future<Pair<Integer, Matrix>> future : futures) {
            Pair<Integer, Matrix> result = future.get();
            int t = result.getFirst();
            if (t == -1)
                return result;
            if (t < minimum)
                minimum = t;
        }

        return new Pair<>(minimum, current);
    }

    /**
     * Simple search
     * @param current the current matrix object
     * @param numSteps the number of steps needed to reach current matrix
     * @param bound manhattan estimation
     * @return the fastest solution
     */
    public static Pair<Integer, Matrix> search(Matrix current, int numSteps, int bound) {
        var checkResult = checkEstimation(numSteps + current.manhattan, bound, current);
        if (checkResult != null) return checkResult;

        int minimum = Integer.MAX_VALUE;
        Matrix solution = null;
        for (Matrix next : current.generatePossibleMoves()) {
            // for each possible moves we have to perform the same search
            Pair<Integer, Matrix> result = search(next, numSteps + 1, bound);
            int currentMinimum = result.getFirst();
            if (currentMinimum == -1) {
                // stop condition reached
                return new Pair<>(-1, result.getSecond());
            }
            if (currentMinimum < minimum) {
                // compare to the minimum and store the faster solution
                minimum = currentMinimum;
                solution = result.getSecond();
            }
        }
        return new Pair<>(minimum, solution);
    }

    /**
     * If the estimation is bigger than the bounds, then return current state since it doesn't make sense searching further
     * If the manhattan distance is 0, we found the solution
     * Else, return null, meaning that we can go on with the searching for a solution
     * @param estimation - numSteps + current.manhattan
     * @param bound
     * @param matrix
     * @return
     */
    private static Pair<Integer, Matrix> checkEstimation(int estimation, int bound, Matrix matrix) {
        if (estimation > bound || estimation > MINIMUM_OPTIMAL_BOUND)
            return new Pair<>(estimation, matrix);
        if (matrix.manhattan == 0)
            return new Pair<>(-1, matrix);

        return null;
    }


}