package approaches;

import domain.Matrix;
import threads.MatrixTask;

import java.util.List;

public class NormalThreadRunner extends Runner {
    public static void run(Matrix matrix1, Matrix matrix2, int nrThreads, String taskType)  {
        Matrix result = new Matrix(matrix1.getNrRows(), matrix2.getNrCols());

        List<MatrixTask> threadsList = getTasks(matrix1, matrix2, result, nrThreads, taskType);
        threadsList.forEach(MatrixTask::start);
        for (MatrixTask thread : threadsList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(result);
    }
}
