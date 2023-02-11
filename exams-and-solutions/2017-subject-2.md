# Exercise 1
(3.5p) Write a parallel (distributed or local, at your choice) program for finding a Hamiltonian path starting at a given vertex. That is, you are given a graph with n vertices and must find a path that starts at vertex 0 and goes through each of the other vertices exactly once. Find a solution, if one exits. If needed, assume you have a function that gets a vector containing a permutation of length n and verifies if it is Hamiltonian path in the given graph or not.

## Solution

# Exercise 2
(2.5p) Consider the following code for transferring money from one account to another. You are required to write a function parsing all accounts (assume you have a vector <Account>) and compute the total amount of money there, so that it doesn't interfere with possible transfers at the same time. Change the transfer function if needed, but it must be able to be called concurrently for independent pair of accounts.

``` cpp
struct Account {
    unsigned id;
    unsigned balance;
    mutex mtx;
};

bool transfer(Account& from, Account& to, unsigned amount) {
  {
    unique_lock<mutex> lck1(from.mtx);
    if(from.balance < amount) return false;
    from.balance -= amount;
  }
  {
    unique_lock<mutex> lck2(to.mtx);
    to.balance += amount;
  }
}
```
  
## Solution
In c++, ```unique_lock``` guarantees an unlocked status on destruction (even if not called explicitly). So because there are 2 scopes inside the function, the first lock is released after the amount was taken from the first balance, and the second lock is released after the amount is transfered to the second balance. So, **the implementation of the transfer function is thread-safe**, as the unique_locks ensure mutual exclusion for the two accounts being transferred between.
  
The problem appears when we want to have the sum. If we want to compute the sum, it may be the case that the amount is taken out of an account, but not yet added to the other one, such that some amount is lost from the entire sum.
- for example, we have account1, account2 and account3. We have a transfer from account1 to account3. The money from the first account is taken, the sum is computed, then the money is transfered to account3. =>  balance discrepancy. We need to lock the two nodes simultaneously
  
``` cpp
bool transfer(Account& from, Account& to, unsigned amount) {
  unique_lock<mutex> lck1(from.mtx);
  unique_lock<mutex> lck2(to.mtx);
  if(from.balance < amount) return false
  from.balance -= amount;
  to.balance += amount;
  return true;
}

int getSum(vector <Account> v) {
  int sum = 0;
  for(auto account : v) {
    account.mtx.lock();
    sum += account.balance;
  }
  // we need to unlock at the end because when we compute the sum and are at account2, a transfer from 1 to 3 may appear and the sum will be bigger since acc1 is not locked
  for(auto account : v) {
    account.mtx.unlock(); 
  }
  return sum;
}
```
  
