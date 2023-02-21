# 2023-1
``` cpp
void mergeSort(int* v, int dataSize, int myId, int nrProc) {
  if(nrProc == 1 || dataSize <= 1) {
    mergeSortLocal(v, dataSize);
  } else {
    int halfLen = dataSize / 2;
    int halfProc = nrProc / 2;
    int child = myId + halfProc;
    MPI_Ssend(&halfLen, 1, MPI_INT, child, 1, MPI_COMM_WORLD);
    MPI_Ssend(&halfProc, 1, MPI_INT, child, 2, MPI_COMM_WORLD);
    MPI_Ssend(v, halfSize, MPI_INT, child, 3, MPI_COMM_WORLD);
    mergeSort(v+halfSize, dataSize-halfSize, myId, nrProc-halfProc);
    MPI_Recv(v, halfSize, MPI_INT, child, 4, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    mergeParts(v, dataSize, halfSize);
  }
}

void worker(int myId) {
  MPI_Status status;
  int dataSize, nrProc;
  MPI_Recv(&dataSize, 1, MPI_INT, MPI_ANY_SOURCE, 1, MPI_COMM_WORLD, &status);
  auto parent = status.MPI_SOURCE;
  MPI_Recv(&nrProc, 1, MPI_INT, parent, 2, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  std::vector v(dataSize);
  MPI_Recv(v.data(), dataSize, MPI_INT, parent, 3, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  mergeSort(v.data(), dataSize, myId, nrProc);
  MPI_Ssend(v.data(), dataSize, MPI_INT, parent, 4, MPI_COMM_WORLD);
}
``` 

Which one of the following issues are present? Describe the changes needed to solve them? (A, C, D)
- **A. the application can deadlock if the length of the vector is smaller than the number of MPI processes.**
- B. the application can produce a wrong result if the input vector size is not a power of 2.
- **C. some worker processes are not used if the number of processes is not a power of 2.**
- **D. the application can deadlock if the number of processes is not a power of 2.**

## Solution
Let’s take an example:
- v=[1,2,3,…,11]
- nrProc=5 (0,1,2,3,4)

Ans:
- C: When the number of processes is not a power of 2, some worker processes will not be used. The mergeSort function uses a recursive approach to divide the work among the MPI processes. Specifically, the input data is split in half (the variable halfProc is calculated as nrProc / 2), and the sorting process is applied to each half recursively. **When the number of MPI processes is not a power of 2, the division of work cannot be done evenly, resulting in some worker processes being left idle**. Here, process number 4 will never be used.
- D: The worker function will only be called once in this program for every process (except the master process), meaning that another send is never received. An example is process 0 that sends data to process 1 twice. **Since MPI_Ssend will not return until a matching receive is posted, process 0 will be a blocking call caused by process 1 because P1 is not receiving anymore => DEADLOCK**
- A: The application can deadlock if the length of the vector is smaller than the number of MPI processes because at least one process will wait to receive data, and **since MPI_Recv() returns when the receive buffer has been filled with valid data, it will be a blocking call => DEADLOCK.**

### Explanation and example
Let’s take an example:
- v=[1,2,3,…,11]
- nrProc=5

Technically this would be the normal flow:
```
0:
halflen = 11/2 = 5 
halfproc = 5/2 = 2
child = 0+2 = 2
send halflen, halfproc, [1,2,3,4,5] to 2
mergesort([6,7,8,9,10,11], 6, 0, 3)
    halflen = 6/2 = 3
		halfproc = 3/2 = 1
		child = 0+1 = 1
		send halflen, halfproc, [6,7,8] to 1
		mergesort([9,10,11], 3, 0, 2)
        halflen = 3/2 = 1
        halfproc = 2/2 = 1
        child = 0+1 =1
        send halflen, halfproc, [9] to 1
        mergesort([10, 11], 2, 0, 1) => mergesortlocal because nrProc == 1
        --
        receive from 1 sorted [9]
			  mergeparts([9], [10, 11])
    receive from 1 sorted [6,7,8]
		mergeparts([6,7,8], [9,10,11])
receive from 2 sorted [1, .., 5]
merge [1,2,3,4,5] with [6,7,8,9,10,11] 
```

The main function usually looks like this:
```
if (rank == 0)
  method();
else 
  worker();
```

This means that the code inside worker will not be executed multiple times if it doesn't have a while loop. As we can see, process 0 sends to process 1 two times the values. But the deadlock actually occurs earlier in this case:

```
2: 
receive info from 0
mergesort([1,2,3,4,5],5,2,2)
    halflen = 5/2 = 2
    halfproc = 2/2 = 1
    child = 2+1 = 3
    send halflen, halfproc, [1,2] to 3
    mergesort([3,4,5], 3, 2, 2)
      halflen = 3/2 = 1
      halfproc = 2/2 = 1
      child = 2+1 = 3
      send halflen, halfproc, [3] to 3 !!!!!!!!! blocks because 3 is not receiving anymore, and we use ssend which is a blocking call => DEADLOCK

3:
	receive info from 2
	mergesort([1,2], 2, 3, 1)
	nrproc=1 => mergesortlocal([1,2], 2) => 2:
							receive sorted [1,2] from 3
```

We can also notice that the 4th process was never used. 

# 2023-2
``` cpp
void mergeSort(int* v, int dataSize, int myId, int nrProc) {
  if(nrProc == 1) {
    mergeSortLocal(v, dataSize);
  } else {
    int halfLen = dataSize / 2;
    int halfProc = (nrProc+1) / 2;
    int child = myId + halfProc;
    MPI_Ssend(&halfLen, 1, MPI_INT, child, 1, MPI_COMM_WORLD);
    MPI_Ssend(&halfProc, 1, MPI_INT, child, 2, MPI_COMM_WORLD);
    MPI_Ssend(v, halfSize, MPI_INT, child, 3, MPI_COMM_WORLD);
    mergeSort(v+halfSize, halfSize, myId, nrProc-halfProc);
    MPI_Recv(v, halfSize, MPI_INT, child, 4, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    mergeParts(v, dataSize, halfSize);
  }
}

void worker(int myId) {
  MPI_Status status;
  int dataSize, nrProc;
  MPI_Recv(&dataSize, 1, MPI_INT, MPI_ANY_SOURCE, 1, MPI_COMM_WORLD, &status);
  auto parent = status.MPI_SOURCE;
  MPI_Recv(&nrProc, 1, MPI_INT, parent, 2, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  std::vector v(dataSize);
  MPI_Recv(v.data(), dataSize, MPI_INT, parent, 3, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  mergeSort(v.data(), dataSize, myId, nrProc);
  MPI_Ssend(v.data(), dataSize, MPI_INT, parent, 4, MPI_COMM_WORLD);
}
``` 

Which one of the following issues are present? Describe the changes needed to solve them? (A, C, D)
- A. the application can deadlock if the length of the vector is smaller than the number of MPI processes.
- B. the application can produce a wrong result if the input vector size is not a power of 2.
- C. some worker processes are not used if the number of processes is not a power of 2.
- D. the application can deadlock if the number of processes is not a power of 2.

## Solution
Let’s take an example:
v=[1,2,3,…,11]
nrProc=5

Ans:
- B: In the recursive `mergeSort` function, the input data is divided in half at each level of the recursion until the base case is reached. However, when the size of the input data is not a power of 2, the division is not even, resulting in a part of the data not being sorted properly. **Specifically, if the size of the input data is an odd number, the last element of the input array will not be sorted properly**. 
  - To solve this issue, we can modify the mergeSort function to mergeSort(v+halfSize, **dataSize-halfSize**, myId, nrProc-halfProc);

### Explanation and example
Let’s take an example:
v=[1,2,3,…,11]
nrProc=5
0: 
	halflen=5
	halfproc=3
	child=3
	send halflen, halfproc, [1,2,3,4,5] to 3
	mergesort([6,7,8,9,10], 6, 0, 3) => 11 will be skipped

# MPI Theory
MPI has a number of different "send modes". These represent different choices of buffering (where is the data kept until it is received) and synchronization (when does a send complete). In the following, I use "send buffer" for the user-provided buffer to send.
- **MPI_Send** -> will not return until you can use the send buffer. It may or may not block (it is allowed to buffer, either on the sender or receiver side, or to wait for the matching receive).
- **MPI_Ssend** -> will not return until matching receive posted
MPI_Ssend

Blocking communication is done using MPI_Send() and MPI_Recv(). These functions do not return (i.e., they block) until the communication is finished. This means that **MPI_Send() returns when the buffer passed can be reused**, either because MPI saved it somewhere, or because it has been received by the destination. Similarly, **MPI_Recv() returns when the receive buffer has been filled with valid data**.

In contrast, non-blocking communication is done using MPI_Isend() and MPI_Irecv(). These function return immediately (i.e., they do not block) even if the communication is not finished yet. You must call MPI_Wait() or MPI_Test() to see whether the communication has finished.
