package mpi;

import domain.Operation;

import java.math.BigInteger;

public class WorkerBigNumbers implements Worker{
    /**
     * Basically if we have 123456 * 15, we split 123456 in: 12, 34, 56. We multiply each of them with 15, then add x
     * zeros, where x is the number of digits remaining in the right. So the result will be:
     * 12*15*(10^4) + 24*15*(10^2) + 56
     * @param me
     */
    @Override
    public void multiply(int me) {
        System.out.printf("Worker %d started\n", me);

        Object[] p = new Object[2];
        Object[] q = new Object[2];
        int[] begin = new int[1];
        int[] end = new int[1];

        MPI.COMM_WORLD.Recv(p, 0, 1, MPI.OBJECT, 0, 0);
        MPI.COMM_WORLD.Recv(q, 0, 1, MPI.OBJECT, 0, 0);

        MPI.COMM_WORLD.Recv(begin, 0, 1, MPI.INT, 0, 0);
        MPI.COMM_WORLD.Recv(end, 0, 1, MPI.INT, 0, 0);

        BigInteger nr1 = (BigInteger) p[0];
        BigInteger nr2 = (BigInteger) q[0];

        StringBuilder bigIntegerValue = new StringBuilder(nr1.toString());

        int nrOfZerosToAdd =  bigIntegerValue.length() - end[0];
        String zeroString = "0".repeat(nrOfZerosToAdd);

        bigIntegerValue.delete(end[0], bigIntegerValue.length());
        bigIntegerValue.delete(0, begin[0]);
        nr1 = new BigInteger(String.valueOf(bigIntegerValue));

        BigInteger resultMultiplication = Operation.multiplyKaratsuba(nr1, nr2);
        BigInteger result = new BigInteger(resultMultiplication.toString() + zeroString);

        MPI.COMM_WORLD.Send(new Object[]{result}, 0, 1, MPI.OBJECT, 0, 0);
    }
}
