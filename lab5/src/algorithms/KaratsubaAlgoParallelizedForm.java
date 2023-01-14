package algorithms;

import domain.Polynomial;

import java.util.concurrent.*;

public class KaratsubaAlgoParallelizedForm {
    public static Polynomial multiply(Polynomial p1, Polynomial p2) throws ExecutionException, InterruptedException {
//        if (currentDepth > 3) {
//            return KaratsubaAlgoSequentialForm.multiply(p1, p2);
//        }
        if (p1.getDegree() < 2 || p2.getDegree() < 2) {
            return KaratsubaAlgoSequentialForm.multiply(p1, p2);
        }

        int len = Math.max(p1.getDegree(), p2.getDegree()) / 2;

        Polynomial lowP1 = new Polynomial(p1.getCoefficients().subList(0, len));
        Polynomial highP1 = new Polynomial(p1.getCoefficients().subList(len, p1.getDegree() + 1));
        Polynomial lowP2 = new Polynomial(p2.getCoefficients().subList(0, len));
        Polynomial highP2 = new Polynomial(p2.getCoefficients().subList(len, p2.getDegree() + 1));

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        Callable<Polynomial> task1 = () -> KaratsubaAlgoParallelizedForm.multiply(highP1, highP2);
        Callable<Polynomial> task2 = () -> KaratsubaAlgoParallelizedForm.multiply(lowP1, lowP2);
        Callable<Polynomial> task3 = () -> KaratsubaAlgoParallelizedForm.multiply(Polynomial.add(lowP1, highP1), Polynomial.add(lowP2, highP2));

        Future<Polynomial> f1 = executor.submit(task1);
        Future<Polynomial> f2 = executor.submit(task2);
        Future<Polynomial> f3 = executor.submit(task3);

        executor.shutdown();

        Polynomial z1 = f1.get();
        Polynomial z2 = f2.get();
        Polynomial z3 = f3.get();

        executor.awaitTermination(60, TimeUnit.SECONDS);

        // compute the final result
        Polynomial r1 = Polynomial.shift(z1, 2 * len);
        Polynomial r2 = Polynomial.shift(Polynomial.subtract(Polynomial.subtract(z3, z1), z2), len);
        return Polynomial.add(Polynomial.add(r1, r2), z2);
    }
}
