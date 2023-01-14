package approaches;

import domain.Matrix;
import threads.ColTask;
import threads.KTask;
import threads.MatrixTask;
import threads.RowTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPoolRunner extends Runner {
    public static void run(Matrix matrix1, Matrix matrix2, int nrThreads, String taskType)  {
        Matrix result = new Matrix(matrix1.getNrRows(), matrix2.getNrCols());
        ExecutorService executor = Executors.newFixedThreadPool(nrThreads);

        getTasks(matrix1, matrix2, result, nrThreads, taskType).forEach(executor::execute);

        executor.shutdown();
        try {
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        System.out.println(result);
    }
}
