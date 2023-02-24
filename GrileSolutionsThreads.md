# Theory
- The condition_variable class is a synchronization primitive used with a std::mutex to block one or more threads until another thread both modifies a shared variable (the condition) and notifies the condition_variable.

**The thread that intends to modify** the shared variable must:
- Acquire a std::mutex (typically via std::lock_guard)
- Modify the shared variable while the lock is owned
- Call notify_one or notify_all on the std::condition_variable (can be done after releasing the lock)
- 
!! Even if the shared variable is atomic, it must be modified while owning the mutex to correctly publish the modification to the waiting thread.

**Any thread that intends to wait** on a std::condition_variable must:
- Acquire a std::unique_lock<std::mutex> on the mutex used to protect the shared variable
- Do one of the following:
    - Check the condition, in case it was already updated and notified
    - Call wait, wait_for, or wait_until on the std::condition_variable (atomically releases the mutex and suspends thread execution until the condition variable is notified, a timeout expires, or a **spurious wakeup occurs**, then atomically acquires the mutex before returning) (practic cand ajung in wait, se da release la lock si asteapta notify-ul, dupa da lock si continua)
    - Check the condition and resume waiting if not satisfied
    - or:
    - Use the predicated overload of wait, wait_for, and wait_until, which performs the same three steps
        - Predicate stop_waiting:  ```cv.wait(lk, []{return i == 1;});``` .... should return false if the waiting should continue, returns true if it should stop.
        ``` cpp
        while (!stop_waiting()) {
            wait(lock);
        }
        ```

# 2023-1
Consider the following code for a queue with multiple producers and consumers. The ```close()``` function is guaranteed to be called exactly once by the user code, and ```enqueue()``` will not be called after that. ```deque()``` is supposed to block if the queue is empty and to return an empty optional if the queue is closed and all elements have been dequeued.

``` cpp
template<typename T>
class ProducerConsumerQueue {
    list<T> items;
    bool isClosed = false;
    condition_variable cv;
    mutex mtx;

public:
    void enqueue(T v) {
        unique_lock<mutex> lck(mtx);
        items.push_back(v);
        cv.notify_one();
    }
    optional<T> dequeue() {
        unique_lock<mutex> lck(mtx); // statement 1
        while(items.empty() && !isClosed) {
            // place 1
            cv.wait(lck);
            // place 2
        }
        lck.unlock(); // statement 2
        if(!items.empty()) {
            optional<T> ret(items.front());
            items.pop_front();
            // place 3
            return ret;
        }
        // place 4
        return optional<T>();
    }
    void close() {
        unique_lock<mutex> lck(mtx);
        isClosed = true;
        cv.notify_all();
    }
};
```

Which of the following are true? Give a short explaination.
- A: [issue] a call to ```dequeue()``` can deadlock if simultaneous with the call to ```enqueue()```;
    - since ```enqueue()``` is correctly implemented, it doesn't matter if it is called simultaneously with ```dequeue()```. If enqueue acquires the lock first, it's ok...if dequeue, acquires the lock first, it is released on wait (items is empty so it enters the while), then enqueue is done, then wait is notified and dequeue continues.
- B: [issue] two simultaneous calls to ```dequeue()``` may deadlock;
    - ???
- C: [issue] two simultaneous calls to ```enqueue()``` may deadlock;
    - ```enqueue()``` is correctly implemented 
- **D: [issue] two simultaneous calls to ```dequeue()``` may result in corrupted ```items``` list;** 
    - This happens because the lock is not used correspondingly. In this situation, two threads can read the same value and pop it twice, resulting in a corrupted item list.
- **E: [issue] a call to ```dequeue()``` can result data corruption or undefined behavior if simultaneous with the call ```enqueue()```;**
        - The dequeue() call modifies the list without locking it => with the enqueue call happening at the same time, it might corrupt the list.    
- F: [fix] move line marked statement 1 in the place marked place 1 and statement 2 in place 2
- G: [fix] eliminate lines marked statement 1 and statement 2
- **H: [fix] remove line marked statement 2 and insert copies of it in places marked place 3 and place 4**
    - This fixes the ‘too early’ lock mentioned above 
- I: [fix] insert a satement unlocking the mutex in the place marked place 1 and lock it back in place 2
- J: [fix] remove line marked statement 2 and insert copies of it in places marked place 3 and place 4, and then move statement 1 in the place where statement 2 was.
