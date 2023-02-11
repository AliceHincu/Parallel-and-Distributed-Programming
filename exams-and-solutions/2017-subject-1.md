# Exercise 1
(3.5p) Write a parallel (distributed or local, at your choice) program for solving the k-coloring problem. That is, you are given a number k, and n objects and some pairs among them that have distinct colors. Find a solution to color them with at most n colors in total, if one exists. Assume you have a function that gets a vector with n integers representing the assignment of colors to objects and checks if the constraits are obeyed or not.

## Solution
k = number of colors, n = number of nodes.

The maximum number of combinations of colors is k^n, so we will have numbers in the interval x ∈ [0, k^n - 1]. We will map every possible coloring "x" of the graph to a number in base k, which represents a possible solution. 
- So if k = 3 and n = 4 => x ∈ [0, 80] . 
- For x = 11 (in base 10) = 1020 (in base 3) is a valid solution for a graph that doesn't have an edge between the second node and last node. 
- This will be done in a method named "to_color(x)".

``` java
public class Task implements Callable<List<Integer>> {
    // here are the fields

    public Task(int i, int n, int k, int maxi, int NR_THREADS, int[][] graph) {
        // here we initialize the fields
    }

    boolean check(List<Integer> colours) {
        // given
    }

    List<Integer> to_color(int sol) {
        // convert to base k
        List<Integer> v = new ArrayList<>();

        for (int i = 0; i < n; ++i) {
            v.add(sol % k);
            sol /= k;
        }
        return v;
    }

    @Override
    public List<Integer> call() {
        // generate solutions and check if they are valid. If the solution is valid, return it. Else, return an empty list.
        for (int j = indexTask; j < maxi; j += NR_THREADS) {
            List<Integer> col = to_color(j);
            if (check(col))
                return col;
        }
        return new ArrayList<>();
    }
```

And here is the method in main:
``` java
    public static List<Integer> solve(int n, int k, int NR_THREADS, int[][] graph) {
        int maxi = 1;
        for (int i = 0; i < n; ++i) {
            maxi *= k; // k^n ... number of maximum combinations
        }

        ExecutorService executorService = Executors.newFixedThreadPool(NR_THREADS);
        List<Future<List<Integer>>> list = new ArrayList<>();

        for (int t = 0; t < NR_THREADS; ++t) {
            Task callable = new Task(t, n, k, maxi, NR_THREADS, graph);
            Future<List<Integer>> future = executorService.submit(callable);
            list.add(future);
        }

        for(Future<List<Integer>> future: list){
            try {
                List<Integer> sol = future.get();
                if(sol.size() != 0)
                    return sol;

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();
        return null;
    }
```

# Exercise 2
(2.5p) Consider the following code for inserting a new value into a linked list at a given position. We assume that insertions can be called concurrently, but not for the same position. Find and fix the concurrency issue. Also, describe a function for parsing the linked list.

```java
struct Node {
    unsigned payload;
    Node* next;
    Node* prev;
    mutex mtx;
};

void insertAfter(Node* before, unsigned value) {
    Node* node = new Node;
    node->payload = value;
    Node* after = before->next;
    before->mtx.lock();
    before->next = node;
    before->mtx.unlock();
    after->mtx.lock();
    after->prev = node;
    after->mtx.unlock();
    node->prev = before;
    node->next = after;
}
```

## Solution
**The concurrency issue** in this code is that the two calls to lock() on the "before" and "after" nodes may not provide mutual exclusion. If two threads simultaneously call insertAfter() for different "before" nodes, but the "after" node for one of these "before" nodes is the same as the "before" node for the other, then both threads could concurrently modify the same "after" node, leading to a race condition.

Here is an example that illustrates this concurrency issue:

``` java
// the list A -> B
thread1:
  insertAfter(nodeA, x);
thread2:
  insertAfter(nodeA->next, y);
```
  
We would accept the following two cases:
- if the first thread is the one to lock B, then:
    - A → x → B and after we will obtain ```A → x → y → B```
- if the second thread locks B, then:
    - A → B → y and after we will obtain ```A → x → B → y```

But this can also happen ```A → x → B```, which is not correct 
