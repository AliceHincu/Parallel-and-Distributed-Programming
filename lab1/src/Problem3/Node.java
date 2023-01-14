package Problem3;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node {
    private Integer data;
    private final List<Node> children = new LinkedList<>();
    private Node parent;
    private final Lock mutex = new ReentrantLock();

    public Node(Integer data) { // for primary vars / leafs
        this.data = data;
    }

    public Node(List<Node> children) {  // for secondary vars/ nodes
        this.children.addAll(children);
        data = this.children.stream().map(Node::getData).reduce(0, Integer::sum);
        this.children.forEach(child -> child.setParent(this));
    }

    public void lock() {
        mutex.lock();
    }

    public void lockTree(){
        for(Node child: children){
            child.lock();
            child.lockTree();
        }
    }

    public void unlock() {
        mutex.unlock();
    }

    public void unlockTree(){
        if(parent!=null && parent.mutex.tryLock()) {
            parent.unlock();
            parent.unlockTree();
        }
    }

    public Integer getData() {
        return data;
    }

    public void setData(Integer newData) {
        if (parent != null) {
            parent.lock();
            parent.setData(parent.getData() + (newData - data));  // here we can have problems, at .getData(), if we don't lock.
            parent.unlock();
        }
        data = newData;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "Node{" +
                "data=" + data +
                ", children=" + children +
                '}';
    }

    public Lock getMutex() {
        return mutex;
    }
}