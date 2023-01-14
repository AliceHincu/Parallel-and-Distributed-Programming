import domain.DirectedGraph;
import domain.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static void findHamiltonian(DirectedGraph graph, int threadCount) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        Lock lock = new ReentrantLock();
        List<Integer> result = new ArrayList<>(graph.size());

        for (int i = 0; i < graph.size(); i++)
            pool.execute(new Task(graph, i, result, lock));


        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }

    private static void getTimeForCycle(DirectedGraph graph, int threadCount) throws InterruptedException {
        long startTime = System.nanoTime();
        findHamiltonian(graph, threadCount);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        System.out.println("Duration: " + duration + " ms");
    }

    public static void main(String[] args) throws InterruptedException {
        DirectedGraph graph = new DirectedGraph(5);

        System.out.println(graph);
        getTimeForCycle(graph, graph.getNrNodes());
    }
}