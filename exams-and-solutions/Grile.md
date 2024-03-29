# Polynomial Product
``` cpp
void computeOneElement(
    std::vector<std::vector<int>> const& p,
    std::vector<std::vector<int>> const& q,
    size_t idx,
    std::vector<int>& rez)
 {
    for(size_t i=0; i<q.size(); ++i) {
      rez[idx+i] += p[idx]*q[i];   // statement 1
    }
 }
 
 std::vector<int> polynomialProduct(
    std::vector<int> const& p,
    std::vector<int> const& q,
    size_t nrThreads)
 {
    std::vector<int> rez(p.size()+q.size()-1);
    size_t begin = 0;
    size_t step = p.size()/nrThreads;   // statement 2
    std::vector<std::thread> threads;
    threads.reserve(nrThreads);

    for(size_t th=0; th<nrThreads; ++th) {
      size_t end = begin+step;    // statement 3
      threads.emplace_back([begin, end, &p, &q, &rez]() {
        for(size_t i=begin; i<end; ++i) {
          computeOneElement(p, q, i, rez);
        }
      });
      begin = end;
    }
    for(std::thread& th : threads) {
      th.join();
    }
    return rez;
 };
```

Select one or more:
- **[issue-A] The result may be incorrect because of race conditions between the concurrent threads**
- [issue-A] There is essentially no parallelism because no two threads can access the polynomials at the same time
- **[issue-B] There are elements of the output polynomial that are not computed**
- [issue-B] There are elements of the output polynomial that are computed twice
- [issue-B] The program will attempt to access non-existent elements
- **[fix-A] Make the output polynomial ```std::vector<std::atomic<int>>```**
- **[fix-A] Replace statement 1 with ```rez[idx] += p[idx]*q[i]``` with appropiate changes on the iteration limits on i and idx**
- [fix-B] In statement 2 put ```step = (p.size()+nrThreads-1)/nrThreads```
- [fix-B] After statement 3 add ```if(end>p.size()) end=p.size()```
- **[fix-B] In statement 2 put ```step=(p.size()+nrThreads-1)/nrThreads``` and after statement 3 add ```if(end>p.size()) end = p.size()```**

# Future
Consider the following code for implementing a future mechanism (the set() function is guaranteed to be called exactly once by the user code):

``` cpp
template <typename T>

class Future {
  T val;
  bool hasValue;
  mutex mtx;
  condition_variable cv;
public:
  Future() : hasValue(false) {}
  void set(T v) {
    cv.notify_all();              // statement 1
    unique_lock<mutex> lck(mtx);  // statement 2
    hasValue = true;              // statement 3
    val = v;                      // statement 4
  }
  T get() {
    unique_lock<mutex> lck(mtx);
    while(!hasValue) {
      cv.wait(lck);               // statement 5
    }
    return value;
  }
 };
```

What kind of concurrency issue does it present? How to fix them?

- [issue] a call to get() can return an uninitialized value if simultaneous with the call to set()
- [issue] simultaneous calls to get() and set() can make future calls to get() deadlock
- [issue] a call to get() can deadlock if called after set()
- **[issue] a call to get() can deadlock if called before set()**
- **[issue] a call to get() can deadlock if simultaneous with the call to set()**
- **[fix] a possible fix is to put in the order statement 2, statement 4, statement 3, and statement 1 those four lines**
- [fix] a possible fix is to unlock the mutex just before line marked statement 5 and to lock it back just afterwards
- [fix] a possible fix is to remove the line marked statement 2 (for Java: together with its corresponding mtx.unlock() )
- [fix] a possible fix is to interchange lines marked statement 3 and statement 4
- **[fix] a possible fix is to interchange lines marked statement 1 and statement 2**

# Matrices Product
Consider the following code for computing the product of two matrices (assuming the number of columns of a is equal to the number of rows of b).

```cpp
void computeOneElement(
    std::vector<std::vector<int>> const& a,
    std::vector<std::vector<int>> const& b,
    size_t row, size_t col,
    std::vector<std::vector<int>>& rez,
    std::mutex& mtx)
 {
    mtx.lock();
    int sum = 0;
    for(size_t i=0; i<b.size(); ++i) {
      sum += a[row][i] * b[i][col];
    }
    rez[row][col] = sum;
    mtx.unlock();
 }
 
 std::vector<std::vector<int>> matrixProd(
    std::vector<std::vector<int>> const& a,
    std::vector<std::vector<int>> const& b,
    size_t nrThreads)
 {
    std::mutex mtx;
    size_t outNrRows = a.size();
    size_t outNrCols = b[0].size();
    std::vector<std::vector<int>> rez(outNrRows);
    for(std::vector<int>& row: rez) {
      row.resize(outNrCols);
    }
    size_t begin = 0;
    size_t step = (outNrRows+nrThreads-1)/nrThreads;   // statement 1
    std::vector<std::thread> threads;
    threads.reserve(nrThreads);
    for(size_t th=0; th<nrThreads; ++th) {
      size_t end = begin+step;    // statement 2
      threads.emplace_back([begin, end, outNrCols, &a, &b, &rez, &mtx]() {
        for(size_t i=begin; i<end; ++i {
          for(size_t j=0; j<outNrCols; ++j) {
            computeOneElement(a, b, i, j, rez, mtx);
          }
        }
      });
      begin = end;
    }
    for(std::thread& th : threads) {
      th.join();
    }
    return rez;
 };
 ```
 
Identify the issues with this code and how to fix them (fixes marked fix-A are to be considered only as far as the issues marked issue-A are concerned, and similarly for B)

Select one or more:

- **[issue-A] There is essentially no parallelism because no two threads can access the matrices at the same time**
- [issue-A] The result may be incorrect because of race conditions between the concurrent threads
- **[fix-A] Remove the mutex mtx and all references to it**
- [fix-A] Make the output matrix std::vector<std::vector<std::atomic>>
- [issue-B] There are elements of the output matrix that are computed twice
- [issue-B] There are elements of the output matrix that are not computed
- **[issue-B] The program will attempt to access non-existent elements**
- [fix-B] In statement 1 put step = outNrRows/nrThreads
- **[fix-B] After statement 2 add if(end>a.size()) end=a.size()**
- [fix-B] In statement 1 put step = outNrRows/nrThreads and after statement 2 add if(end>a.size()) end=a.size()

# Producer consumer queue
- [f] remove line 18 and insert copies of it in lines 21-22 and 23-24
- [i] a call to dequeue() can result data corruption or undefined behavior if simultaneous with the call to enqueue()

# Future continuations
- simultaneous calls to addContinuation() and set() may lead to continuations that are never executed
-  to solve all concurrency issues, one has to move both the content of line 12 to between lines 9 and 10 and the content of line 22 to between lines 18 and 19

# Scalar product
- some of the terms are not added into the final sum
- there are some concurrency issues requiring the use of mutexes or atomic variables
- to solve issues, one could replace line 10 with int end = begin + (a.size()-begin)/(nrThreads-i)

# Merge Sort
Consider the following excerpt from a program that is supposed to merge-sort a vector. The function **worker()** is called in all processes except process 0, the function mergeSort() is called from process 0 (and from the places described in this excerpt), the function mergeSortLocal() sorts the specified vector and the function mergeParts() merges two sorted adjacent vectors, given the pointer to the first element, the total lengthand the length of the first vector.

```cpp
void mergeSort(int* v, int dataSize, int myId, int nrProc) {
  if(nrProc == 1) {
    mergeSortLocal(v, dataSize);
  } else {
    int halfLen = dataSize / 2;
    int halfProc = (nrProc+1) / 2;
    int child = myId+halfProc;
    MPI_Send(&halfLen, 1, MPI_INT, child, 1, MPI_COMM_WORLD);
    MPI_Send(&halfProc, 1, MPI_INT, child, 2, MPI_COMM_WORLD);
    MPI_Send(v, halfLen, MPI_INT, child, 3, MPI_COMM_WORLD);
    mergeSort(v+halfLen, halfLen, myId, nrProc-halfProc);
    MPI_Recv(v, halfLen, MPI_INT, child, 4, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    mergeParts(v, dataSize, halfLen);
  }
}

void worker(int myId) {
  MPI_Status status;
  int dataSize, nrProc;
  MPI_Recv(&dataSize, 1, MPI_INT, MPI_ANY_SOURCE, 1, MPI_COMM_WORLD, &status);
  auto parent = status.MPI_SOURCE;
  MPI_Recv(&nrProc, 1, MPI_INT, parent, 2, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  std::vector v(dataSize);
  mergeSort(v.data(), dataSize, myId, nrProc);
  MPI_Send(v.data(), dataSize, MPI_INT, parent, 3, MPI_COMM_WORLD);
}
```

Which of the following issues are present? Describe the changes needed to solve them?
- the application can deadlock if the length of the vector is smaller than the number of MPI processes.
- the application can produce a wrong result if the input vector size is not a power of 2.
- **some worker processes are not used if the number of processes is not a power of 2.**
- the application can deadlock if the number of processes is not a power of 2.

c. Yes, some worker processes may not be used if the number of processes is not a power of 2. The algorithm is designed to divide the input vector into two equal parts and distribute the work among the worker processes accordingly. If the number of processes is not a power of 2, it is not possible to divide the input vector into two equal parts, which means that some worker processes may not receive any work to do. This can lead to an inefficient use of the available processing resources, as some worker processes will remain idle. To avoid this issue, it may be necessary to modify the algorithm or the process distribution to ensure that all worker processes are used effectively, regardless of the number of processes used.


