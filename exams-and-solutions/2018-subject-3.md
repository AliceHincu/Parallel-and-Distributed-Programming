# Exercise 2
Consider the following code for enqueueing a work item to a thread pool.
Find the concurrency issue and fix it. Also, add a mecahnism to end the threads at shutdown.

``` cpp
class ThreadPool {
  condition_variable cv;
  mutex mtx;
  list<function<void()>> work;
  vector <thread> threads;

  void run() {
    while(true) {
      if(work.empty()) {
        unique_lock<mutex> lck(mtx);
        cv.wait(lck);
      } else {
        function<void()> wi = work.front();
        work.pop_front();
        wi();
      }
    }
  }
public:
  explicit ThreadPool(int n) {
    threads.resize(n);
    for(int i = 0; i < n; ++ i) {
      threads.emplace_back([this](){run();});
    }
  }
  void enqueue(function <void()> f) {
    unique_lock<mutex> lck(mtx);
    work.push_back(f);
    cv.notify_one();
  }
}
```

## Solution
The ThreadPool implementation has a race condition where multiple threads can access and modify the shared work list simultaneously. This can lead to data corruption, such as tasks being executed twice or skipped altogether.
- example: both threads can do a ".front()", have the same function, then execute the function twice and pop the next one without executing it.

To fix this issue, you can protect access to the work list with a mutex so that only one thread at a time can access and modify the list. Here's the corrected code:
``` cpp
void run() {
    while(true) {
      function<void()> wi;
      {
        unique_lock<mutex> lck(mtx);
        if(work.empty()) {
          cv.wait(lck);
        } else {
          wi = work.front();
          work.pop_front();
        }
      } // lock out of scope => unlocked
      if(wi) {
        wi();
      }
    }
  }
```
