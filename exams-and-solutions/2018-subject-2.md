# Exercise 1
Write a parallel (distributed or local, at your choice) program that computes the discrete convolution of a vector with another vector.

The convolution is defined as: r(i) = sum(a(i) * b(i - j), j = 0..n). All three vectors are of length N and, for simplicity, i - j shall be taken modulo N.

## Solution
This is solved using **Simple data decomposition**. 
- Each thread has an index, and it will compute r(index), r(index+NR_THREADS), r(index+NR_THREADS * 2) and so on 
- Inside each thread, for every j=0..n, we calculate a[j] * b[(i - j + n) % n] and add it to r(i), where i is the index of the thread or a multiple of it.
- (i - j + n) % n because i-j can be negative.

``` java
public static int[] solve(int[] a, int[] b, int NR_THREADS) throws InterruptedException {
        int n = a.length;
        int[] result = new int[n];

        ExecutorService executor = Executors.newFixedThreadPool(NR_THREADS);

        for (int index = 0; index < NR_THREADS; index++) {
            int finalI = index; // java wants a final copy
            executor.submit(() -> {
                for(int i = finalI; i < n; i += NR_THREADS) {
                    for (int j = 0; j < n; j++) {
                        result[i] += a[j] * b[(i - j + n) % n];
                    }
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        return result;
    }
```
# Exercise 2
(3p) Consider the following code for enqueueing a continuation on a future. Identify and fix the thread-safety issue.
``` cpp
template<typename T>
class Future {
    list<function<void(T)>> continuations;
    T val;
    bool has_value;
public:
    Future(): has_value(false) {}
    void set(T v) {
      val = v;
      hasValue = true;

      for(function<void(T)>& f: continuations) {
        f(v);
      }

      continuations.clear();
    }
  
    void addContinuation(function<void(T)> f) {
      if(hasValue) {
        f(val);
      } else {
        continuations.push_back(f);
      }
    }
}
```

## Solution
The code has a thread-safety issue because multiple threads can access the shared data members continuations, val, and has_value simultaneously, leading to race conditions. To fix this issue:
- Create a mutex on the hasValue variable, assuming that the iteration on a `list` is thread safe.
  - A thread-safety issue in the code occurs because the variable ```hasValue``` is being accessed by multiple threads simultaneously. For example, one thread might be setting the value while another thread is checking the value of hasValue in the addContinuation method. If the value of hasValue is not updated atomically, it can lead to a race condition where the value of hasValue is temporarily in an inconsistent state, causing incorrect behavior. 
- Otherwise, simply add a mutex so that only one method can be called at a time.
  - A thread-safety issue in the code occurs because the method set and addContinuation can be called simultaneously from multiple threads. For example, one thread might be setting the value while another thread is adding a continuation to the list. This can lead to a race condition where the list of continuations is being modified by multiple threads at the same time, potentially causing data corruption or undefined behavior.

``` cpp
template<typename T>
class Future {
        list<function<void(T)>> continuations;
        T val;
        bool has_value;
        mutex mtx;
    public:
        Future(): has_value(false) {}
        void set(T v) {
                {
                        unique_lock<mutex> lck(mtx); // lock so you can modify
                        val = v;
                        has_value = true;
                }
                for(function<void(T)>& f: continuations) {
                        f(v);
                }
                continuations.clear();
        }
        void addContinuation(function<void(T)> f) {
                  unique_lock<mutex> lck(mtx);
                  if(has_value) {
                    f(val);
                  } else {
                    continuations.push_back(f);
                  }
                }
        }
 }
```

# Exercise 3
(3p) Write a parallel algorithm that computes the product of two matrices.

## Solution

``` java
public static int[][] solve(int[][] a, int[][] b, int NR_THREADS) {
        // check in main that nr of cols of first matrix = nr of lines of second matrix
        int nrRows = a.length;
        int nrCols = b[0].length;
        int n = nrCols;
        int[][] c = new int[nrRows][nrCols];

        // matrix[i][j] = a[i][k] * b[k][j]
        ExecutorService executor = Executors.newFixedThreadPool(NR_THREADS);
        for (int index = 0; index < NR_THREADS; index++) {
            int finalIndex = index;
            executor.submit(() -> {
                for (int i = finalIndex; i < n; i += NR_THREADS) {
                    for (int j = 0; j < n; j++) {
                        for (int k = 0; k < n; k++) {
                            c[i][j] += a[i][k] * b[k][j];
                        }
                    }
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS))
                executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        return c;
    }
```
