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
- B: [issue] two simultaneous calls to ```dequeue()``` may deadlock;
- C: [issue] two simultaneous calls to ```enqueue()``` may deadlock;
- **D: [issue] two simultaneous calls to ```dequeue()``` may result in corrupted ```items``` list;**
- **E: [issue] a call to ```dequeue()``` can result data corruption or undefined behavior if simultaneous with the call ```enqueue()```;**
- F: [fix] move line marked statement 1 in the place marked place 1 and statement 2 in place 2
- G: [fix] eliminate lines marked statement 1 and statement 2
- **H: [fix] remove line marked statement 2 and insert copies of it in places marked place 3 and place 4**
- I: [fix] insert a satement unlocking the mutex in the place marked place 1 and lock it back in place 2
- J: [fix] remove line marked statement 2 and insert copies of it in places marked place 3 and place 4, and then move statement 1 in the place where statement 2 was.

## Solution
- D: The unlock happens too early. In this situation, two threads can access and modify the same list (inside ```if(!items.empty())```), at the same time, resulting in a corrupted item list.
- 
