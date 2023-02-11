# Exercise 1
(3p) Write a parallel program that computes the scalar product of two vectors in Java. The program is given the two vectors, as well as the number of threads to use, and must balance the work as well as possible between them - this means that final summing must be done on a balanced tree scheme.

## Solution 1 - with tree
- split the work evenly between working threads - each thread takes a sequence of consecutive elements. Processing of an array of data can often be split into independent blocks: simple way of computing the boundary index: beginIdx = (threadIdx * nrElements) div nrThreads
- calculate NR_THREADS chuncks of that vector 
- add the result of each future with the help of a balanced tree: leftChild + rightChild = parent
``` java
public static int solve(int[] a, int[] b, int NR_THREADS) throws ExecutionException, InterruptedException {
        int n = a.length;
        int chunkSize = n / NR_THREADS;
        int reminder = n % NR_THREADS;

        ExecutorService executor = Executors.newFixedThreadPool(NR_THREADS);
        Future<Integer>[] futures = new Future[NR_THREADS];

        for (int index = 0; index < NR_THREADS; index++) {
            int start = index * chunkSize;
            int end = start + chunkSize;
            if (index == NR_THREADS - 1)
                end += reminder;
            int finalEnd = end; // copy required by java

            futures[index] = executor.submit(() -> {
                int localResult = 0;
                for (int i = start; i < finalEnd; i++) {
                    localResult += a[i] * b[i];
                }
                return localResult;
            });
        }

        // balanced tree scheme
        while (futures.length > 1) {
            int newSize = (futures.length + 1) / 2;
            Future<Integer>[] newFutures = new Future[newSize];

            for (int i = 0; i < newSize; i++) {
                int j = i * 2;
                Future<Integer>[] finalFutures = futures; // copy required by java
                newFutures[i] = executor.submit(() -> finalFutures[j].get() + finalFutures[j+1].get()); // parent is equal to leftChild + rightChild.
            }
            futures = newFutures;
        }

        Integer result = futures[0].get();

        executor.shutdown();
        try {
            if(!executor.awaitTermination(800, TimeUnit.MILLISECONDS))
                executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        return result;
    }
```

## Solution 2
``` java
        // instead of balanced tree
        Integer result = 0;
        for (Future<Integer> future : futures) {
            result += future.get();
        }
```
