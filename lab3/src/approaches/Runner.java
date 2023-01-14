package approaches;

import domain.Matrix;
import threads.ColTask;
import threads.KTask;
import threads.MatrixTask;
import threads.RowTask;

import java.util.ArrayList;
import java.util.List;

public class Runner {
    public static final String ROW_AFTER_ROW = "rowAfterRow";
    public static final String COL_AFTER_COL = "colAfterCol";
    public static final String KTH = "Kth";

    protected static List<MatrixTask> getTasks(Matrix matrix1, Matrix matrix2, Matrix result, int nrThreads, String taskType) {
        return switch (taskType) {
            case ROW_AFTER_ROW -> getRowAfterRowTasks(matrix1, matrix2, result, nrThreads);
            case COL_AFTER_COL -> getColAfterColTasks(matrix1, matrix2, result, nrThreads);
            case KTH, default -> getKthTasks(matrix1, matrix2, result, nrThreads);
        };
    }

    /**
     * Each task computes consecutive elements, going row after row. Example for 9x9 matrix:
     * * task 0 computes rows 0 and 1 of the result matrix, plus elements 0-1 of row 2 (20 elements in total);
     * * task 1 computes the remainder of row 2, row 3, and elements 0-3 of row 4 (20 elements);
     * * task 2 computes the remainder of row 4, row 5, and elements 0-5 of row 6 (20 elements);
     * * finally, task 3 computes the remaining elements (21 elements).
     * @return list of tasks
     */
    private static List<MatrixTask> getRowAfterRowTasks(Matrix matrix1, Matrix matrix2, Matrix result, int nrThreads){
        List<MatrixTask> tasks = new ArrayList<>();
        int resultSize = result.getNrRows() * result.getNrCols();  // total nr of elements in the result matrix
        int count = resultSize / nrThreads;  // how many elements does each thread calculate for the result matrix

        for(int i=0; i<nrThreads; i++){
            int iStart = count * i / result.getNrRows();
            int jStart = count * i % result.getNrRows();

            if (i == nrThreads - 1)
                count += resultSize % nrThreads;  // in case the division has a remainder, the elements that remain are assigned to the last task

            tasks.add(new RowTask(matrix1, matrix2, result, iStart, jStart, count));
        }

        return tasks;
    }

    /**
     * Each task computes consecutive elements, going column after column. This is like the previous example, but
     * interchanging the rows with the columns: task 0 takes columns 0 and 1, plus elements 0 and 1 from column 2,
     * and so on
     * @return list of tasks
     */
    private static List<MatrixTask> getColAfterColTasks(Matrix matrix1, Matrix matrix2, Matrix result, int nrThreads) {
        List<MatrixTask> tasks = new ArrayList<>();
        int resultSize = result.getNrRows() * result.getNrCols();  // total nr of elements in the result matrix
        int count = resultSize / nrThreads;  // how many elements does each thread calculate for the result matrix

        for(int i=0; i<nrThreads; i++){
            int iStart = count * i % result.getNrRows();
            int jStart = count * i / result.getNrRows();

            if (i == nrThreads - 1)
                count += resultSize % nrThreads;  // in case the division has a remainder, the elements that remain are assigned to the last task

            tasks.add(new ColTask(matrix1, matrix2, result, iStart, jStart, count));
        }

        return tasks;
    }

    /**
     * Each task takes every k-th element (where k is the number of tasks), going row by row. So, task 0 takes elements
     * (0,0), (0,4), (0,8), (1,3), (1,7), (2,2), (2,6), (3,1), (3,5), (4,0), etc.
     * @return list of tasks
     */
    private static List<MatrixTask> getKthTasks(Matrix matrix1, Matrix matrix2, Matrix result, int nrThreads) {
        List<MatrixTask> tasks = new ArrayList<>();
        int resultSize = result.getNrRows() * result.getNrCols();  // total nr of elements in the result matrix
        int count = resultSize / nrThreads;  // how many elements does each thread calculate for the result matrix

        for(int i=0; i<nrThreads; i++) {
            if (i < resultSize % nrThreads) // add remaining for end
                count++;

            int iStart = i / result.getNrCols();
            int jStart = i % result.getNrCols();

            tasks.add(new KTask(matrix1, matrix2, result, iStart, jStart, count, nrThreads));
        }

        return tasks;
    }
}
