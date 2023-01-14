package domain;

import java.util.*;

public class DirectedGraph {
    private Map<Integer, List<Integer>> edges;
    private List<Integer> nodes;

    public DirectedGraph(int nodeCount) {
        this.edges = new HashMap<>();
        this.nodes = new ArrayList<>();

        for (int i = 0; i < nodeCount; i++) {
            this.edges.put(i, new ArrayList<>());
            this.nodes.add(i);
        }

        this.generateRandomGraph();
    }

    private void generateRandomGraph() {
        List<Integer> nodes = getNodes();

        java.util.Collections.shuffle(nodes);

        // create hamiltonian cycle
        for (int i = 1; i < nodes.size(); i++) this.addEdge(nodes.get(i - 1),  nodes.get(i));
        this.addEdge(nodes.get(nodes.size() - 1), nodes.get(0));

        // create random edges
        Random random = new Random();
        for (int i = 0; i < nodes.size() / 2; i++){
            int nodeA = random.nextInt(nodes.size() - 1);
            int nodeB = random.nextInt(nodes.size() - 1);

            while(this.edgeExists(nodeA, nodeB) && nodeA != nodeB){
                nodeA = random.nextInt(nodes.size() - 1);
                nodeB = random.nextInt(nodes.size() - 1);
            }

            this.addEdge(nodeA, nodeB);
        }

    }

    private void addEdge(int nodeA, int nodeB) {
        this.edges.get(nodeA).add(nodeB);
    }

    List<Integer> neighboursOf(int node) {
        return this.edges.get(node);
    }

    boolean edgeExists(int nodeA, int nodeB){
        return  this.edges.get(nodeA).contains(nodeB);
    }

    List<Integer> getNodes(){
        return nodes;
    }

    public int getNrNodes(){
        return nodes.size();
    }

    public int size() {
        return this.edges.size();
    }

    @Override
    public String toString() {
        return "DirectedGraph{" +
                "edges=" + edges +
                ", nodes=" + nodes +
                '}';
    }
}
