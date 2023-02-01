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

Which of the following issues are present? Describe the changes needed to solve them.
a. the application can deadlock if the length of the vector is smaller than the number of MPI processes.
b. the application can produce a wrong result if the input vector size is not a power of 2.
**c. some worker processes are not used if the number of processes is not a power of 2.**
d. the application can deadlock if the number of processes is not a power of 2.

c. Yes, some worker processes may not be used if the number of processes is not a power of 2. The algorithm is designed to divide the input vector into two equal parts and distribute the work among the worker processes accordingly. If the number of processes is not a power of 2, it is not possible to divide the input vector into two equal parts, which means that some worker processes may not receive any work to do. This can lead to an inefficient use of the available processing resources, as some worker processes will remain idle. To avoid this issue, it may be necessary to modify the algorithm or the process distribution to ensure that all worker processes are used effectively, regardless of the number of processes used.

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
- [issue-A] The result may be incorrect because of race conditions between the concurrent threads
- [issue-A] There is essentially no parallelism because no two threads can access the polynomials at the same time
- [issue-B] There are elements of the output polynomial that are not computed
- [issue-B] There are elements of the output polynomial that are computed twice
- [issue-B] The program will attempt to access non-existent elements
- [fix-A] Make the output polynomial ```std::vector<std::atomic<int>>```
- [fix-A] Replace statement 1 with ```rez[idx] += p[idx]*q[i]``` with appropiate changes on the iteration limits on i and idx
- [fix-B] In statement 2 put ```step = (p.size()+nrThreads-1)/nrThreads```
- [fix-B] After statement 3 add ```if(end>p.size()) end=p.size()```
- [fix-B] In statement 2 put ```step=(p.size()+nrThreads-1)/nrThreads``` and after statement 3 add ```if(end>p.size()) end = p.size()```
