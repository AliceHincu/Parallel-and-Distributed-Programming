package mpi;

import domain.Operation;
import domain.Polynomial;

public class MasterPolynomial implements Master {
    public static void multiplication(Polynomial p, Polynomial q, int nrProcs, String type) {
        long startTime = System.currentTimeMillis();
        int start = 0, finish = 0;

        int polynomialSize = p.getDegree() + 1;
        int workersNumber = nrProcs - 1;
        int len = polynomialSize / workersNumber;

        for (int i = 1; i < nrProcs; i++) {
            start = finish;
            finish += len;
            if (i == nrProcs - 1)
                finish = polynomialSize;

            MPI.COMM_WORLD.Send(new Object[]{p}, 0, 1, MPI.OBJECT, i, 0);
            MPI.COMM_WORLD.Send(new Object[]{q}, 0, 1, MPI.OBJECT, i, 0);

            MPI.COMM_WORLD.Send(new int[]{start}, 0, 1, MPI.INT, i, 0);
            MPI.COMM_WORLD.Send(new int[]{finish}, 0, 1, MPI.INT, i, 0);

        }

        Object[] results = new Object[nrProcs - 1];
        for (int i = 1; i < nrProcs; i++) {
            MPI.COMM_WORLD.Recv(results, i - 1, 1, MPI.OBJECT, i, 0);
        }

        Polynomial result = Operation.buildResult(results);
        long endTime = System.currentTimeMillis();
        System.out.println(type + " multiplication of polynomials:\n" + result);
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
    }
}
