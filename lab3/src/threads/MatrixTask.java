package threads;

import domain.Matrix;

import java.util.List;

public abstract class MatrixTask extends Thread {
    protected final Matrix firstMatrix;
    protected final Matrix secondMatrix;
    protected final Matrix resultMatrix;
    protected final int rowStart;
    protected final int colStart;
    protected int count;

    public MatrixTask(Matrix firstMatrix, Matrix secondMatrix, Matrix resultMatrix, int rowStart, int colStart, int count) {
        this.firstMatrix = firstMatrix;
        this.secondMatrix = secondMatrix;
        this.resultMatrix = resultMatrix;
        this.rowStart = rowStart;
        this.colStart = colStart;
        this.count = count;  // size of task
    }
    protected abstract List<List<Integer>> getCoordsToCalculate();

    @Override
    public void run() {
        List<List<Integer>> coords = getCoordsToCalculate();
        for(List<Integer> coord: coords){
            int row = coord.get(0);
            int col = coord.get(1);
            int resultElement = Matrix.multiplyMatricesCell(firstMatrix, secondMatrix, row, col);
            resultMatrix.setElement(row, col, resultElement);
        }
    }
}
