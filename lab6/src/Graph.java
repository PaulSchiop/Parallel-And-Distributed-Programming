import java.util.*;

public class Graph {
    private final int numVertices;
    private final List<List<Integer>> adjacencyList;

    public Graph(int numVertices) {
        this.numVertices = numVertices;
        this.adjacencyList = new ArrayList<>(numVertices);
        for (int i = 0; i < numVertices; i++) {
            this.adjacencyList.add(new ArrayList<>());
        }
    }

    /**
     * Adds a directed edge from one vertex to another.
     */
    public void addEdge(int from, int to) {
        // Validate indices to prevent crashes
        if (from < 0 || from >= numVertices || to < 0 || to >= numVertices) {
            throw new IllegalArgumentException("Vertex index out of bounds");
        }
        adjacencyList.get(from).add(to);
    }

    public List<Integer> getNeighbors(int v) {
        return adjacencyList.get(v);
    }

    public int getNumVertices() {
        return numVertices;
    }
}