package Threads;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataQueue {
    private final Queue<Double> queue = new LinkedList<>();
    private final int CAPACITY;
    private final Lock lock = new ReentrantLock();
    private final Condition condVar = lock.newCondition(); // provides inter-thread communication methods

    public DataQueue(int maxSize) {
        this.CAPACITY = maxSize;
    }

    public void put(Double value) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == CAPACITY) {
                System.out.println(Thread.currentThread().getName() + " : Buffer is full, waiting");
                condVar.await(); // queue is full. Wait for signal from Consumer to know when there is an open space in the queue
            }

            queue.add(value);
            System.out.printf("%s added %f into queue %n", Thread.currentThread().getName(), value);
            // if it was empty, after .add() it's not anymore...
            System.out.println(Thread.currentThread().getName() + " : Signalling that buffer is no more empty now");
            condVar.signalAll(); // signal Consumer that, buffer has element now, so it can take it.
        } finally {
            lock.unlock();
        }
    }

    public Double get() throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == 0) {
                System.out.println(Thread.currentThread().getName() + " : Buffer is empty, waiting");
                condVar.await(); // queue is empty. Wait for signal from Producer to know when an element is added.
            }

            Double value = queue.poll();
            if (value != null) {
                System.out.printf("%s consumed %f from queue %n", Thread .currentThread().getName(), value);
                System.out.println(Thread.currentThread().getName() + " : Signalling that buffer may be empty now");
                condVar.signalAll(); // signal producer thread that, buffer may be empty now
            }

            return value;
        } finally {
            lock.unlock();
        }
    }
}
