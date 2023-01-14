package algorithms;

import domain.Polynomial;
import domain.PolynomialTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RegularAlgoParallelizedForm {

    /**
     * Calculate <nrThreads> different chunks of the result polynomial by having threads that each calculate a chunk
     * For example, if we have degree 9 and 3 threads:
     *      the first thread will calculate coefficients 0-3,
     *         second thread will calculate coefficients 4-7,
     *          third thread will calculate coefficients 8-10
     */
    public static Polynomial multiply(Polynomial p1, Polynomial p2, int nrThreads) throws InterruptedException {
        // Initialize the product polynomial
        int sizeOfResultCoefficientList = p1.getDegree() + p2.getDegree() + 1;
        List<Integer> coefficients = new ArrayList<>(Collections.nCopies(sizeOfResultCoefficientList, 0));
        Polynomial result = new Polynomial(coefficients);

        // Initialize executor
        ExecutorService executor = Executors.newFixedThreadPool(nrThreads);
        int step = sizeOfResultCoefficientList / nrThreads;

        // Create tasks
        List<Runnable> tasks = new ArrayList<>();
        int start, end;
        for (int i = 0; i <= result.getDegree(); i += step) {
            start = Math.min(i, sizeOfResultCoefficientList);
            end = Math.min(i + step, sizeOfResultCoefficientList);
            PolynomialTask polynomialTask = new PolynomialTask(start, end, p1, p2, result);
            tasks.add(polynomialTask);
        }

        // Execute tasks
        for (Runnable task : tasks) executor.execute(task);

        // Shut down executor service
        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        return result;
    }
}
