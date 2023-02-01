1. [Permutations](#permutations)  


# Permutations
Write a distributed program, using MPI, that counts the number of permutations of N that satisfy a given property. You are given a function ```bool pred(vector <int> const& v) that verifies if a given permutation satisifes the property. Your program shall call the function once for each permutation and count the number of times it return true. Also, for any data structure S that you need to transfer between processes, you are given a function ```void send(S s, int to)``` that sends an object to a specified process and ```void receive(S& k, int& from)``` that receives an object and returns it together with the sender ID.

``` java
// will be called after the solution was found to break from while(true)
private static void killAll(int numberOfProcess) {
    for (int i = 1; i < numberOfProcess; ++i) {
        MPI.COMM_WORLD.Send(new int[]{0}, 0, 1, MPI.INT, i, 2);
    }
}

private static void master(int n, int numberOfProcesses) {
    List<Integer> solution = new ArrayList<>();
    int count = back(solution, n, 0, numberOfProcesses);
    System.out.println("Count = " + count);
    killAll(numberOfProcesses);
}

// start backtracking for permutations
private static int back(List<Integer> solution, int n, int me, int numberOfProcesses) {
    if (solution.size() == n) {
        return pred(solution); // the check function (usually already provided)
    }

    int sum = 0; // count of permutations that check the condition
    int child = me + numberOfProcesses / 2;
    /* if-branch: backtracking with MPI
    *  else-branch: normal backtracking
    * */
    if (numberOfProcesses >= 2 && child < numberOfProcesses) {
        List<Integer> toSend = new ArrayList<>(solution); // duplicate sol

        /*
         On the next position of the received solution, the worker will put only odd numbers.
         Here, it will put even numbers on the next position of the same solution. (that's why we duplicate)
         We generate multiple possible solutions in parallel to reduce the total time taken to find all the solutions
         */
        MPI.COMM_WORLD.Send(new int[]{1}, 0, 1, MPI.INT, child, 2);
        MPI.COMM_WORLD.Send(new Object[]{toSend}, 0, 1, MPI.OBJECT, child, 0);

        // continue to generate new solutions locally.
        List<Integer> temp = new ArrayList<>(solution);
        for (int i = 0; i < n; i += 2) {
            if (temp.contains(i)) continue;
            temp.add(i);
            sum += back(temp, n, me, numberOfProcesses / 2); /* split the search space for finding the solution into two parts => division by two */
            temp.remove(temp.size() - 1);
        }
        Object[] receivedData = new Object[1];
        MPI.COMM_WORLD.Recv(receivedData, 0, 1, MPI.OBJECT, child, 1);
        sum += (int) receivedData[0];
    } else {
        for (int i = 0; i < n; i++) {
            if (solution.contains(i)) continue;
            solution.add(i);
            sum += back(solution, n, me, 1);
            solution.remove(solution.size() - 1);
        }
    }
    return sum;
}

private static void worker(int n, int me, int numberOfProcesses) {
    while (true) {
        int[] alive = new int[1];
        MPI.COMM_WORLD.Recv(alive, 0, 1, MPI.INT, MPI.ANY_SOURCE, 2);
        if (alive[0] == 0) 
            break;
            
        Object[] receivedData = new Object[1];
        Status recv = MPI.COMM_WORLD.Recv(receivedData, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, 0);
        int parent = recv.source;
        List<Integer> array = (List<Integer>) receivedData[0];
        
        int sum = 0;
        for (int i = 1; i < n; i += 2) {
            if (array.contains(i)) continue;
            array.add(i);
            sum += back(array, n, me, numberOfProcesses);
            array.remove(array.size() - 1);
        }
        MPI.COMM_WORLD.Send(new Object[]{sum}, 0, 1, MPI.OBJECT, parent, 1);
    }
}

public void run(String[] args) throws FileNotFoundException {
    MPI.Init(args);
    int selfRank = MPI.COMM_WORLD.Rank();
    int numberOfProcesses = MPI.COMM_WORLD.Size();
    int n = 4; // permutations of 4
    if (selfRank == 0) {
        master(n, numberOfProcesses);
    } else {
        worker(n, selfRank, numberOfProcesses);
    }
    MPI.Finalize();
}
```
