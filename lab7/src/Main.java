import domain.BigNumber;
import domain.Polynomial;
import mpi.*;

import java.math.BigInteger;
import java.util.Map;

public class Main {
    private static final String SIMPLE = "Simple";
    private static final String KARATSUBA = "Karatsuba";
    private static final String BIG_NUMBERS = "Big Numbers";
    private static final String MULTIPLICATION = BIG_NUMBERS;

    public static void main(String[] args) {
        MPI.Init(args);

        int me = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        System.out.println("Hello world from <" + me + "> of <" + size + ">");

        if (me == 0) {
            // master process
            if (MULTIPLICATION.equals(KARATSUBA) || MULTIPLICATION.equals(SIMPLE)) {
                System.out.println("Master process generating polynomials:");

                Polynomial p = new Polynomial(100);
                Polynomial q = new Polynomial(100);
                System.out.println("p:" + p);
                System.out.println("q:" + q);

                MasterPolynomial.multiplication(p, q, size, MULTIPLICATION);
            } else {
                System.out.println("Master process generating big numbers:");

//                BigInteger nr1 = BigNumber.generate(10);
//                BigInteger nr2 = BigNumber.generate(10);
                BigInteger nr1 = new BigInteger("1111111111");
                BigInteger nr2 = new BigInteger("1111111111");
                System.out.println("nr1:" + nr1);
                System.out.println("nr2:" + nr2);

                MasterBigNumbers.multiplication(nr1, nr2, size);
            }
        } else {
            // worker process
            Map<String, Worker> workers = Map.of(
                    SIMPLE, new WorkerSimple(),
                    KARATSUBA, new WorkerKaratsuba(),
                    BIG_NUMBERS, new WorkerBigNumbers()
            );
            workers.get(MULTIPLICATION).multiply(me);
        }

        MPI.Finalize();
    }
}