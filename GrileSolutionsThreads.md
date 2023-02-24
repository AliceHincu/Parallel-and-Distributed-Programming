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
- B: **[issue] two simultaneous calls to ```dequeue()``` may deadlock;**
    - Let’s say the first thread removes the last item from the list, and at the same time, the second thread enters the while loop, checking the state of the list before it being emptied by the other thread (as the modification of the list is not guarded by a lock, this is possible). Then, the second thread will forever wait to be notified, which won’t happen until the threads are closed at the end, resulting in a possible deadlock.
- C: [issue] two simultaneous calls to ```enqueue()``` may deadlock;
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


# 2023-2
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
        // place 1
        while(true) {
            // place 2
            if(!items.empty()) {
                // place 3
                optional<T> ret(items.front());
                items.pop_front();
                return ret;
            }
            if(isClosed) {
                return optional<T>();
            }
            unique_lock<mutex> lck(mtx); // statement 1
            cv.wait(lck);
        }
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
- F: [fix] move line marked statement 1 in the place marked place 1
- **G: [fix] move line marked statement 1 in the place marked place 2**
- H: [fix] move line marked statement 1 in the place marked place 3
- I: [fix] eliminate lines marked statement 1
- J: [fix] put a copy of the line marked statement 2 in the place marked place 3


# 2023-3
Consider the following code for implementing a future mechanism (the `set()` function is guaranteed to be called exactly once by the user code)
``` cpp
template<typename T>
class Future{
	T val;
	bool hasValue;
	mutex mtx;
    condition_variable cv;
public:
	Future() :hasValue(false) {}
	void set(T v){
		cv.notify_all();    
        unique_lock<mutex> lck(mtx); 
        hasValue = true; 
        val = v;   
	}
	T get() {
        unique_lock<mutex> lck(mtx);
        while(!hasValue) {
            cv.wait(lck); 
        }
        return val;
    }
};
```

Which of the following are true? Give a short explanation.
- **A: [issue] a call to get() can deadlock if simultaneous with the call to set()**
    - Let’s say get acquires the lock and then releases it in the wait. Then, set() will notify and get will get the lock, see that hasValue is false and then wait again. set() will change the value of hasValue but it won’t call notify again, so it’s possible that the get() thread will wait forever and not be notified. 
- **B: [issue] a call to get() can deadlock if called after set()**
    - The calls are made on the same thread. When we call get, it will block at the wait line until it is notified, but it won’t be notified because the set() is called after get(), so it will wait forever.
- C: [issue] a call to get() can return an uninitialized value if simultaneous with the call to set()
- D: [issue] simultaneous calls to get() and set() can make future calls to get() deadlock
- E: [issue] a call to get() can deadlock if called before set()
- F: [fix] a possible fix is to remove the line 11
- G: [fix] a possible fix is to interchange lines 12 and 13
- **H: [fix] a possible fix is to reorder lines 10-13 in the order 11, 13, 12, 10**
    - The order of the statements needs to be changed to prevent the call to get() from seeing an uninitialized value. By acquiring the lock, setting the value, and notifying all waiting threads before releasing the lock, the waiting threads are guaranteed to see the updated value. 
- **I: [fix] a possible fix is to interchanges lines 10 and 11**
    - you cannot notify without a lock.
- j: [fix] a possible fix is to unlock the mutex just before line 18 and to lock it back just afterwards.

# 2023-4
Consider the following code for implementing a future mechanism (the `set()` function is guaranteed to be called exactly once by the user code)
``` cpp
template<typename T>
class Future{
	T val;
	bool hasValue;
	mutex mtx;
    condition_variable cv;
public:
	Future() :hasValue(false) {}
	void set(T v){
        unique_lock<mutex> lck(mtx); 
		cv.notify_all();    
        hasValue = true; 
        val = v;   
	}
	T get() {
        unique_lock<mutex> lck(mtx);
        while(!hasValue) {
            cv.wait(lck); 
        }
        return val;
    }
};
```

Which of the following are true? Give a short explanation.
- **A: [issue] a call to get() can deadlock if simultaneous with the call to set()**
- B: [issue] a call to get() can deadlock if called after set()
- C: [issue] a call to get() can return an uninitialized value if simultaneous with the call to set()
- D: [issue] simultaneous calls to get() and set() can make future calls to get() deadlock
- **E: [issue] a call to get() can deadlock if called before set()**
- F: [fix] a possible fix is to move line 11 between line 13 and 14
- **G: [fix] a possible fix is to interchange lines 11 and 13**
- H: [fix] a possible fix is to remove line 17
- **I: [fix] a possible fix is to move line 17 between lines 15 and 16.**
- J: [fix] a possible fix is to move line 17 between lines 15 and 16 and to unlock the mutex before line 18(wait()) and lock it afterwards.

# 2023-3
Consider the following code for implementing a future mechanism (the `set()` function is guaranteed to be called exactly once by the user code)
``` cpp
template<typename T>
class Future{
	list<function<void(T)>> continuations;
	T val;
	bool hasValue;
	mutex mtx;
public:
	Future() :hasValue(false){}
	void set(T v){
		val = v;
		hasValue = true;
		unique_lock<mutex> lck(mtx);
		for(function<void(T)>& f : continuations){
			f(v);
		}
		continuations.clear();
	}
	void addContinuation(function <void(T)> f){
		if(hasValue){
			f(val);
		} else {
			unique_lock<mutex> lck(mtx);
			continuations.push_back(f);
		}
	}
}
```

Which of the following are true? Give a short explanation.
- A: [issue] a call to set() can deadlock if simultaneous with the call to addContinuation();
- B: [issue] a call to get() can deadlock if called after set()
- C: [issue] two simultaneous calls to addContinuation() may lead to a corrupted continuation vector;
- D: [issue] simultaneous calls to addContinuation() and set() may lead to continuations that are never executed;
	``` We can have the following situation:
addContinuation() and set() are executed simultaneously
addContinuation() reaches line 21 ( } else { )
at the same time, set() reaches its end (line 17)
addContinuation() goes on and adds a new f to the list, which will not get executed since set() is done 
=> continuation that is never executed
```
- E: [issue] simultaneous calls to addContinuation() and set() may lead to continuations that are executed twice;
- F: [fix] a possible fix is to move the content of the line 12 between lines 9 and 10;
- G: [fix] a possible fix is to move the content of the line 12 between lines 13 and 14;
- H: [fix] a possible fix is to move the content of the line 22 between lines 18 and 19;
- J: [fix] a possible fix is to move the both the content of line 12 between lines 9 and 10 and the content of line 22 between lines 13 and 14;
- J: [fix] a possible fix is to move the both the content of line 12 between lines 13 and 14 and the content of line 22 between lines 13 and 14;
If we can consider both F and H at the same time, together, then this would be correct: We need to make setting or checking of hasValue atomic with the corresponding operation on the continuations vector. So, we need to lock the
mutex before setting or checking hasValue
