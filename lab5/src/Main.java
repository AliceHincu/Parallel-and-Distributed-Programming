import algorithms.KaratsubaAlgoParallelizedForm;
import algorithms.KaratsubaAlgoSequentialForm;
import algorithms.RegularAlgoParallelizedForm;
import algorithms.RegularAlgoSequentialForm;
import domain.BigNumber;
import domain.Polynomial;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void showPolynomialResults() throws InterruptedException, ExecutionException {
        Polynomial a = new Polynomial(100);
        Polynomial b = new Polynomial(100);

        System.out.println("p:" + a);
        System.out.println("q:" + b);

        // Use both the regular O(n2) algorithm and the Karatsuba algorithm, and each in both the sequential form and a parallelized form
        // regular algorithm, sequential form
        Polynomial p1 = RegularAlgoSequentialForm.multiply(a, b);
        // regular algorithm, parallelized form
        Polynomial p2 = RegularAlgoParallelizedForm.multiply(a, b, 3);
        // Karatsuba algorithm, sequential form
        Polynomial p3 = KaratsubaAlgoSequentialForm.multiply(a, b);
        // Karatsuba algorithm, parallelized form
        Polynomial p4 = KaratsubaAlgoParallelizedForm.multiply(a, b);

        System.out.println("\n== regular algorithm, sequential form ==");
        System.out.println("\t" + p1 + "\n");
        System.out.println("== regular algorithm, parallelized form ==");
        System.out.println("\t" + p2 + "\n");
        System.out.println("== karatsuba algorithm, sequential form ==");
        System.out.println("\t" + p3 + "\n");
        System.out.println("== karatsuba algorithm, parallelized form ==");
        System.out.println("\t" + p4 + "\n");
    }

    public static void showBigNumberResults() {
        BigInteger nr1 = BigNumber.generate(10);
        BigInteger nr2 = BigNumber.generate(10);

        System.out.println("nr1:" + nr1);
        System.out.println("nr2:" + nr2);

        BigInteger r3 = KaratsubaAlgoSequentialForm.multiply(nr1, nr2);

        System.out.println("== karatsuba algorithm, sequential form ==");
        System.out.println("\t" + r3 + "\n");
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
//        showPolynomialResults();
        showBigNumberResults();
    }

}