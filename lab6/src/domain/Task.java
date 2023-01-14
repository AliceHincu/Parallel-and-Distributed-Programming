package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class Task implements Runnable {
    private final DirectedGraph graph;
    private final int startingNode;
    private final List<Integer> path;
    private final Lock lock;
    private final List<Integer> result;

    public Task(DirectedGraph graph, int node, List<Integer> result, Lock lock) {
        this.graph = graph;
        this.startingNode = node;
        path = new ArrayList<>();
        this.lock = lock;
        this.result = result;
    }

    @Override
    public void run() {
        visit(startingNode);
    }

    private void setResult() {
        this.lock.lock();
        this.result.clear();
        this.result.addAll(this.path);
        System.out.println("For starting node: " + this.startingNode + " the cycle is: " + result);
        this.lock.unlock();
    }
    private void visit(int node) {
        path.add(node);

        if (path.size() == graph.size()) {
            if (graph.neighboursOf(node).contains(startingNode))
                setResult();
            return;
        }

        for (int neighbour : graph.neighboursOf(node)) {
            if (!this.path.contains(neighbour))
                visit(neighbour);
        }
    }
}
