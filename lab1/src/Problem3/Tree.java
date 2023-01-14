package Problem3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.locks.Lock;

public class Tree {
    private final Node root;
    private final List<Node> leaves = new ArrayList<>(); // primary vars
    private final List<Lock> mutexes = new ArrayList<>(); // save the locks for each node; flatten the tree

    public Tree() {
        root = this.generateHardcodedTree();
        populateMutexes();
    }

    public int calculateSum(Node node) {
        if (node.getChildren().isEmpty())
            return node.getData();

        int sum = 0;
        for (Node child : node.getChildren()) {
            sum += calculateSum(child);
        }
        return sum;
    }

    public void lockTree(){
        mutexes.forEach(Lock::lock);
    }

    public void unlockTree(){
        mutexes.forEach(Lock::unlock);
    }

    private Node generateHardcodedTree() {
        // generate child 1
        Node leaf1child1 = new Node(1);
        Node leaf2child1 = new Node(2);
        Node child1 = new Node(Arrays.asList(leaf1child1, leaf2child1));

        // generate child 2
        Node leaf1child2 = new Node(3);
        Node leaf2child2 = new Node(4);
        Node leaf3child2 = new Node(5);
        Node leaf4child2 = new Node(6);
        Node node1child2 = new Node(Arrays.asList(leaf1child2, leaf2child2, leaf3child2, leaf4child2));

        Node leaf5child2 = new Node(7);
        Node child2 = new Node(Arrays.asList(node1child2, leaf5child2));

        // generate child 3
        Node leaf1child3 = new Node(8);
        Node leaf2child3 = new Node(9);
        Node node1child3 = new Node(Arrays.asList(leaf1child3, leaf2child3));

        Node leaf3child3 = new Node(10);
        Node node2child3 = new Node(Arrays.asList(node1child3, leaf3child3));

        Node leaf4child3 = new Node(11);
        Node leaf5child3 = new Node(12);
        Node node3child3 = new Node(Arrays.asList(leaf4child3, leaf5child3));

        Node node4child3 = new Node(Arrays.asList(node2child3, node3child3));

        Node leaf6child3 = new Node(13);
        Node leaf7child3 = new Node(14);
        Node node5child3 = new Node(Arrays.asList(leaf6child3, leaf7child3));

        Node child3 = new Node(Arrays.asList(node4child3, node5child3));

        // generate child 4
        Node leaf1child4 = new Node(1);
        Node leaf2child4 = new Node(2);
        Node node1child4 = new Node(Arrays.asList(leaf1child4, leaf2child4));

        Node leaf3child4 = new Node(3);

        Node leaf4child4 = new Node(4);
        Node leaf5child4 = new Node(5);
        Node leaf6child4 = new Node(6);
        Node node2child4 = new Node(Arrays.asList(leaf4child4, leaf5child4, leaf6child4));
        Node leaf7child4 = new Node(7);
        Node node3child4 = new Node(Arrays.asList(node2child4, leaf7child4));

        Node child4 = new Node(Arrays.asList(node1child4, leaf3child4, node3child4));

        // before generating root, add every leaf.
        leaves.addAll(Arrays.asList(
                leaf1child1, leaf2child1, leaf1child2, leaf2child2, leaf3child2, leaf4child2, leaf5child2, leaf1child3,
                leaf2child3, leaf3child3, leaf4child3, leaf5child3, leaf6child3, leaf7child3, leaf1child4, leaf2child4,
                leaf3child4, leaf4child4, leaf5child4, leaf6child4, leaf7child4));

        // generate root
        return new Node(Arrays.asList(child1, child2, child3, child4));
    }

    private void populateMutexes() {
        //preorder traversal
        Stack<Node> nodes = new Stack<>();
        nodes.push(root);

        while (!nodes.isEmpty()) {
            Node curr = nodes.pop();
            if (curr != null) {
                mutexes.add(curr.getMutex());
                for(int i = curr.getChildren().size() - 1; i >= 0; i--)
                    nodes.add(curr.getChildren().get(i));
            }
        }
    }

    public List<Node> getLeaves() {
        return leaves;
    }

    public Node getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return "Tree{" +
                "root=" + root +
                '}';
    }
}
