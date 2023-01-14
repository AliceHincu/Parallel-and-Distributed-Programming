package threads;

import domain.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KTask extends MatrixTask{
    private final int k;

    public KTask(Matrix firstMatrix, Matrix secondMatrix, Matrix resultMatrix, int rowStart, int colStart, int count, int nrThreads) {
        super(firstMatrix, secondMatrix, resultMatrix, rowStart, colStart, count);
        this.k = nrThreads;
    }

    protected List<List<Integer>> getCoordsToCalculate(){
        List<List<Integer>> coords = new ArrayList<>();
        int row = rowStart, col = colStart;

        while (count > 0 && row < resultMatrix.getNrRows()) {
            coords.add(Arrays.asList(row, col));
            count--;
            row += (col + k) / resultMatrix.getNrCols();
            col = (col + k) % resultMatrix.getNrCols();
        }

        return coords;
    }
}
