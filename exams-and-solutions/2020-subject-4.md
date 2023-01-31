# Exercise 2
Consider the following code for a queue with multiple producers and consumers
a. (1.5p) Identify and fix the concurrency issue(s)
b. (2p) Create a mechanism for blocking the producer so that the producers are blocked on enqueue if the number of items in the queue reaches a fixed value (say, 100)

```cpp
template <typename T>
class ProducerConsumerQueue {
  list<T> items;
  condition_variable cv;
  mutex mtx;
public:
  void enqueue(T v) {
    items.push_back(v);
    cv.notify_one();
  }
  
  T dequeue() {
    {
      unique_lock<mutex> lck(mtx);
      while(items.empty()) { // wait for items to have an element
        cv.wait(lck);
      }
    }
    {
      unique_lock<mutex> lck(mtx);
      T ret = items.front();
      items.pop_front();
      return ret;
    }
  }
}
```

The concurrency issue in this code is that there are two separate lock scopes for reading and popping the front element of the items list, so other threads may enqueue elements in between, leading to data races. To fix this issue, the lock scope should be kept continuous from the waiting on the condition variable to the return statement and a lock needs to be added in enqueue function.

Here's a step-by-step example of a data race in the given code:
- The producer thread  ```p1``` adds an item to the items list by calling ```enqueue(v)```.
- The consumer thread  ```c1``` checks if items is empty while holding a lock on mtx.
- The consumer thread  ```c1``` calls cv.wait(lck) to wait for an item to become available.
- The producer thread  ```p1``` calls ```cv.notify_one()``` to signal that an item is available.
- The consumer thread  ```c1``` resumes and acquires a lock on mtx again to retrieve the item.
- !!! Meanwhile, another producer  ```p2``` thread adds another item to the items list by calling ```enqueue(v2)``` 
- The first consumer  ```c1``` retrieves the item from the front of the items list and pops it. (**that means v2 instead of v1**)
- The first consumer  ```c1``` releases the lock on mtx.
- Another consumer thread acquires a lock on mtx to retrieve the item that was just added.
- The first consumer thread returns the item that was popped, leading to an unexpected result, because it was not aware of the item that was added in step 6.
- This is an example of a data race, because the same shared data (items) is being accessed by multiple threads simultaneously, leading to unexpected results.
Basically:
- p1 -> enqueue(v)
- c1-> done with waiting and acquires lock again
- p2 -> enqueue(v2)
- c1-> pop v2
- c2 -> pop v 

FROM DOCUMENTATION:
The ```condition_variable``` class is a synchronization primitive used with a std::mutex to block one or more threads until another thread both modifies a shared variable (the condition) and notifies the condition_variable.

The thread that intends to modify the shared variable must:
- Acquire a std::mutex (typically via std::lock_guard)
- Modify the shared variable while the lock is owned
- Call notify_one or notify_all on the std::condition_variable (can be done after releasing the lock)

Any thread that intends to wait on a std::condition_variable must:
- Acquire a std::unique_lock<std::mutex> on the mutex used to protect the shared variable
- Do one of the following:
  - Check the condition, in case it was already updated and notified
  - Call wait, wait_for, or wait_until on the std::condition_variable (atomically releases the mutex and suspends thread execution until the condition variable is notified, a timeout expires, or a spurious wakeup occurs, then atomically acquires the mutex before returning)
  - Check the condition and resume waiting if not satisfied

CORRECT CODE:
```cpp
template <typename T>
class ProducerConsumerQueue {
  list<T> items;
  condition_variable cv;
  mutex mtx;
public:
  void enqueue(T v) {
    {
      lock_guard<mutex> lck(mtx);
      items.push_back(v);
    }
    cv.notify_one();
  }
  
  T dequeue() {
    unique_lock<mutex> lck(mtx);
    while(items.empty()) { // wait for items to have an element
      cv.wait(lck);
    }
    T ret = items.front();
    items.pop_front();
    return ret;
  }
};
```
