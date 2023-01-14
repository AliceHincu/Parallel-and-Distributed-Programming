package mpi;

import domain.Operation;

import java.math.BigInteger;

public class MasterBigNumbers implements Master{

    public static void multiplication(BigInteger p, BigInteger q, int nrProcs) {
        long startTime = System.currentTimeMillis();
        int start = 0, finish = 0;

        int numberSize = p.toString().length();
        int workersNumber = nrProcs - 1;
        int len = numberSize / workersNumber;

        for (int i = 1; i < nrProcs; i++) {
            start = finish;
            finish += len;
            if (i == nrProcs - 1)
                finish = numberSize;

            MPI.COMM_WORLD.Send(new Object[]{p}, 0, 1, MPI.OBJECT, i, 0);
            MPI.COMM_WORLD.Send(new Object[]{q}, 0, 1, MPI.OBJECT, i, 0);

            MPI.COMM_WORLD.Send(new int[]{start}, 0, 1, MPI.INT, i, 0);
            MPI.COMM_WORLD.Send(new int[]{finish}, 0, 1, MPI.INT, i, 0);

        }

        Object[] results = new Object[nrProcs - 1];
        for (int i = 1; i < nrProcs; i++) {
            MPI.COMM_WORLD.Recv(results, i - 1, 1, MPI.OBJECT, i, 0);
        }

        BigInteger result = Operation.buildResultBigNumber(results);
        long endTime = System.currentTimeMillis();
        System.out.println("Multiplication of numbers:\n" + result);
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
    }
}
