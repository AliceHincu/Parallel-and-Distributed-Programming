package threads;

import domain.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColTask extends MatrixTask{
    public ColTask(Matrix firstMatrix, Matrix secondMatrix, Matrix resultMatrix, int rowStart, int colStart, int count) {
        super(firstMatrix, secondMatrix, resultMatrix, rowStart, colStart, count);
    }

    protected List<List<Integer>> getCoordsToCalculate(){
        List<List<Integer>> coords = new ArrayList<>();
        int row = rowStart, col = colStart;
        while (count > 0 &&  row < resultMatrix.getNrRows() && col < resultMatrix.getNrCols()) {
            coords.add(Arrays.asList(row, col));
            row++;
            count--;
            if (row == resultMatrix.getNrRows()) {
                row = 0;
                col++;
            }
        }

        return coords;
    }
}
