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
```cpp

```
