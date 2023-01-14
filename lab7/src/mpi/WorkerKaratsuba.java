package mpi;

import domain.Operation;
import domain.Polynomial;

public class WorkerKaratsuba implements Worker{
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

        Polynomial pp = (Polynomial) p[0];
        Polynomial qq = (Polynomial) q[0];

        for (int i = 0; i < begin[0]; i++)
            pp.getCoefficients().set(i, 0);
        for (int j = end[0]; j < pp.getCoefficients().size(); j++)
            pp.getCoefficients().set(j, 0);

        Polynomial result = Operation.multiplyKaratsuba(pp, qq);

        MPI.COMM_WORLD.Send(new Object[]{result}, 0, 1, MPI.OBJECT, 0, 0);
    }
}
