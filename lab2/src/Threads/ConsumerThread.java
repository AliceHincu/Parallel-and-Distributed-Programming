package Threads;

import java.util.concurrent.BlockingQueue;

public class ConsumerThread extends Thread {
    public int result = 0;
    public DataQueue products;
    public int length;

    public ConsumerThread(DataQueue products, int length) {
        super("Consumer");
        this.products = products;
        this.length = length;
    }

    @Override
    public void run() {
        for (int i=0;i<this.length;i++) {
            try {
                result += products.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Consumer: Result is now " + result);
        }
        System.out.println("\nConsumer: Final result is: " + result);
    }
}
