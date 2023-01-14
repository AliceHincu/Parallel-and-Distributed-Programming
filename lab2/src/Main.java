import Domain.Vector;
import Threads.ConsumerThread;
import Threads.DataQueue;
import Threads.ProducerThread;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Create two threads, a producer and a consumer, with the producer feeding the consumer.
 *
 * Requirement: Compute the scalar product of two vectors.
 *
 * Create two threads. The first thread (producer) will compute the products of pairs of elements - one from each vector
 * - and will feed the second thread. The second thread (consumer) will sum up the products computed by the first one.
 * The two threads will behind synchronized with a condition variable and a mutex. The consumer will be cleared to use
 * each product as soon as it is computed by the producer thread.
 **/
public class Main {
    public static void main(String[] args) {

        Vector vec1 = new Vector(new double[]{1,2,3,4});
        Vector vec2 = new Vector(new double[]{4,3,2,1});

        double[] vector1 = new double[1000];
        Arrays.fill(vector1, 1);
        double[] vector2 = new double[1000];
        Arrays.fill(vector2, 1);

        Vector v1 = new Vector(vector1);
        Vector v2 = new Vector(vector2);

        DataQueue products = new DataQueue(2);

        ProducerThread producer = new ProducerThread(products,v1,v2);
        ConsumerThread consumer = new ConsumerThread(products,v1.getSize());

        producer.start();
        consumer.start();
    }
}