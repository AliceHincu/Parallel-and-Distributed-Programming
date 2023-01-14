import Problem3.Node;
import Problem3.Notification;
import Problem3.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 3. Summation with fixed structure of inputs
 * We have to keep the values of some integer variables. Some of them are primary variables; they represent input data.
 * The others are secondary variables, and represent aggregations of some other variables. In our case, each secondary
 * variable is a sum of some input variables. The inputs may be primary or secondary variables. However, we assume that
 * the relations do not form cycles.
 * <p>
 * At runtime, we get notifications of value changes for the primary variable. Processing a notification must atomically
 * update the primary variable, as well as any secondary variable depending, directly or indirectly, on it. The updating
 * shall not re-compute the sums; instead, you must use the difference between the old value and the new value of the
 * primary variable.
 * <p>
 * From time to time, as well as at the end, a consistency check shall be performed. It shall verify that all the
 * secondary variables are indeed the sums of their inputs, as specified.
 */
public class Main {
    private static final int NUMBER_OF_THREADS = 50;
    private static final List<Notification> notifications = new ArrayList<>();
    private static Tree tree;
    private static final AtomicInteger seed = new AtomicInteger(0);

    public static void sendNotifications() {
        System.out.println(tree);
        float start =  System.nanoTime() / 1000000;

        List<Node> leaves = tree.getLeaves();
        for (int i = 0; i < NUMBER_OF_THREADS * 200; i++) {
            int randomIndex = new Random(seed.addAndGet(1)).nextInt(leaves.size());
            int randomValue = new Random(seed.addAndGet(1)).nextInt(101);
            Node randomLeaf = leaves.get(randomIndex);
            Notification n = new Notification(randomLeaf, randomValue);
            notifications.add(n);
            System.out.println(n);
        }

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        notifications.forEach((notification -> {
            executor.execute(notification);
            try {
                Thread.sleep(new Random(seed.addAndGet(1)).nextInt(1, 50));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            double r = new Random().nextDouble();
            if (r < 0.1)
                verify();
        }));

        // finish threads
        executor.shutdown();
        try {
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        System.out.println(tree);

        verify();

        float end = System.nanoTime() / 1000000;
        System.out.println("\n >>>>> End work: " + (end - start) / 1000 + " seconds");
    }

    public static void verify() {
        System.out.println("Verifying the variables...");

        tree.lockTree();
        int expectedSum = tree.calculateSum(tree.getRoot());
        int actualSum = tree.getRoot().getData();
        tree.unlockTree();

        if (expectedSum != actualSum)
            System.err.println("Verification failed!");
        else
            System.out.println("Verification Successful!");

    }

    public static void main(String[] args) {
        tree = new Tree();
        sendNotifications();
    }
}