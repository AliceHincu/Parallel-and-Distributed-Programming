package Threads;

import Domain.Vector;

import java.util.concurrent.BlockingQueue;

public class ProducerThread extends Thread{
    public DataQueue products;
    public Vector vector1, vector2;

    public ProducerThread(DataQueue products, Vector vector1, Vector vector2) {
        super("Producer");
        this.products = products;
        this.vector1 = vector1;
        this.vector2 = vector2;
    }


    @Override
    public void run() {
        if(vector1.getSize() != vector2.getSize()) {
            System.err.println("Vectors have different dimension!");
            return;
        }

        for(int i = 0; i < vector1.getSize(); i++){
            double element1 = vector1.getVec()[i];
            double element2 = vector2.getVec()[i];
            double product = element1 * element2;
            System.out.println("Producer: Sending " + element1 + "*" + element2 + " = " + product);
            try {
                products.put(product);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
