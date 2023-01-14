import mpi.MPI;

public class Main {
    /**
     * https://stackoverflow.com/questions/36356408/mpj-express-java-mpi-running-in-intellij-idea
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        /* Set up */
        MPI.Init(args);
        final DSM dsm = new DSM();

        /* Local process index */
        if (MPI.COMM_WORLD.Rank() == 0) {
            master(dsm);
        } else {
            worker(dsm);
        }
        /* Tear down */
        MPI.Finalize();
    }

    private static void master(final DSM dsm) throws InterruptedException {
        final Thread thread = new Thread(new Listener(dsm));
        thread.start();

        dsm.subscribe("a");
//        dsm.subscribe("b");
//        dsm.subscribe("c");

//        Thread.sleep(2000); // if there are 2 updates simultaneously, there are chances that at the end of the program, the variable has different values...only if the updates are send one after another (replace 2000 with 1000 to see the behaviour)
//        dsm.updateVariable("a",50);

        Thread.sleep(1000);
        dsm.compareAndExchange("a", 100, 50);

        Thread.sleep(1000);
        dsm.close();
        thread.join();
    }

    private static void worker(final DSM dsm) throws InterruptedException {
        int me = MPI.COMM_WORLD.Rank();
        final Thread thread = new Thread(new Listener(dsm));
        thread.start();

        if(me == 1) {
            dsm.subscribe("a");

            Thread.sleep(1000);

            dsm.updateVariable("a", 100);
        } else {

        }
        thread.join();
    }

}