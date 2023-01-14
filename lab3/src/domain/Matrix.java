package domain;

import java.util.Arrays;
import java.util.Random;

final public class Matrix {
    private final int N;             // number of rows
    private final int M;             // number of columns
    private final int[][] data;   // N-by-M array

    /**
     * Create M-by-N matrix of 0's
     */
    public Matrix(int N, int M) {
        this.N = N;
        this.M = M;
        data = new int[N][M];
    }

    /**
     * Create matrix based on 2d array
     */
    public Matrix(int[][] data) {
        N = data.length;
        M = data[0].length;
        this.data = new int[N][M];
        for (int i = 0; i < N; i++)
            System.arraycopy(data[i], 0, this.data[i], 0, M);
    }

    /**
     * Create and return a random M-by-N matrix with values between 0 and 10
     */
    public void generateRandom() {
        Random random = new Random();
        for (int i = 0; i < N; i++)
            for (int j = 0; j < M; j++)
                data[i][j] = random.nextInt(10);
    }

    /**
     * Create and return a M-by-N matrix with only "1" values
     */
    public void generateWithOnlyOnes() {
        for(int[] row: data)
            Arrays.fill(row, 1);
    }

    /**
     * Method to calculate an element from the product of two matrices
     * @param firstMatrix - the first matrix
     * @param secondMatrix - the second matrix
     * @param row - the row from the first matrix that is multiplied
     * @param col - the column from the second matrix that is multiplied
     * @return an element from the product matrix with coord (row, col)
     */
    public static int multiplyMatricesCell(Matrix firstMatrix, Matrix secondMatrix, int row, int col) {
        int[][] first = firstMatrix.getData();
        int[][] second = secondMatrix.getData();

        int cell = 0;
        for (int i = 0; i < second.length; i++)
            cell += first[row][i] * second[i][col];

        return cell;
    }

    public int[][] getData() {
        return data;
    }

    public int getNrRows() {
        return N;
    }

    public int getNrCols() {
        return M;
    }

    public void setElement(int row, int col, int value) {
        data[row][col] = value;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++)
                result.append(data[i][j]).append(" ");
            result.append("\n");
        }

        return result.toString();
    }
}
