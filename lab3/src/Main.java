import approaches.NormalThreadRunner;
import approaches.Runner;
import approaches.ThreadPoolRunner;
import domain.Matrix;

import java.util.Scanner;

public class Main {
    private static final int NR_THREADS = 4;
    private static final String THREAD_POOL = "ThreadPool";
    private static final String THREAD_FOR_EACH_TASK = "ThreadForEachTask";
    // ----- MODIFY HERE ----- several programs: 2 approaches and 3 different task types
    private static final String APPROACH = THREAD_POOL;
    private static final String TASK_TYPE = Runner.KTH; // "rowAfterRow", "colAfterCol", "Kth"
    private static int N1;
    private static int M1;
    private static int N2;
    private static int M2;

    private static void read() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- Enter dimensions for first matrix: ");
        System.out.println("Enter N1: ");
        N1 = scanner.nextInt();
        System.out.println("Enter M1: ");
        M1 = scanner.nextInt();
        System.out.println("--- Enter dimensions for second matrix: ");
        System.out.println("Enter N2: ");
        N2 = scanner.nextInt();
        System.out.println("Enter M2: ");
        M2 = scanner.nextInt();
    }

    public static void main(String[] args) {
        read();
        Matrix a = new Matrix(N1, M1);
        Matrix b = new Matrix(N2, M2);

        // in case of random
//        a.generateRandom();
//        b.generateRandom();

        // fill with 1s
        a.generateWithOnlyOnes();
        b.generateWithOnlyOnes();

        // in case of known values ... result should be { { 30, 24, 18 }, { 84, 69, 54 }, { 138, 114, 90 } }
//        int[][] dataMatrix1 = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
//        int[][] dataMatrix2 = { { 9, 8, 7 }, { 6, 5, 4 }, { 3, 2, 1 } };
//
//        Matrix a = new Matrix(dataMatrix1);
//        Matrix b = new Matrix(dataMatrix2);

//        System.out.println(a);
//        System.out.println(b);

        if(a.getNrRows() != b.getNrCols()) {
            System.err.println("The matrices can't be multiplied");
            return;
        }


        float start =  System.nanoTime();
        switch (APPROACH){
            case THREAD_POOL:
                ThreadPoolRunner.run(a,b, NR_THREADS, TASK_TYPE);
                break;
            case THREAD_FOR_EACH_TASK:
                NormalThreadRunner.run(a,b, NR_THREADS, TASK_TYPE);
                break;
            default:
                System.err.println("Invalid approach.");
        }

        float end = System.nanoTime();
        System.out.println("\n >>>>> End work: " + (end - start)/1000 + " microseconds");



    }


}