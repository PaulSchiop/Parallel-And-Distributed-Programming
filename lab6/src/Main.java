import java.util.List;

public class Main {
    public static void main(String[] args) {
        int NUM_VERTICES = 6;
        int NUM_THREADS = 4;

        // Create a graph (0->1->2->3->4->5->0) plus random edges
        Graph g = new Graph(NUM_VERTICES);

        // Construct a known Hamiltonian cycle
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 0);

        // Add some "noise" (extra edges) to make the search harder/branch out
        g.addEdge(0, 3);
        g.addEdge(1, 4);
        g.addEdge(2, 5);
        g.addEdge(3, 1); // Cycle 3->1->2->3
        g.addEdge(4, 0);

        System.out.println("=== Graph Initialized (" + NUM_VERTICES + " vertices) ===");

        System.out.println("\n--- 1. Testing Manual Threading (" + NUM_THREADS + " threads) ---");
        long start = System.currentTimeMillis();

        ThreadedHamiltonian manualSolver = new ThreadedHamiltonian(g, 0);
        List<Integer> path1 = manualSolver.solve(NUM_THREADS);

        long end = System.currentTimeMillis();

        if (path1 != null) {
            System.out.println("Solution found: " + path1);
        } else {
            System.out.println("No solution found.");
        }
        System.out.println("Time: " + (end - start) + "ms");

        System.out.println("\n--- 2. Testing ForkJoinPool ---");
        start = System.currentTimeMillis();

        ForkJoinHamiltonian forkJoinSolver = new ForkJoinHamiltonian(g, 0);
        List<Integer> path2 = forkJoinSolver.solve();

        end = System.currentTimeMillis();

        if (path2 != null) {
            System.out.println("Solution found: " + path2);
        } else {
            System.out.println("No solution found.");
        }
        System.out.println("Time: " + (end - start) + "ms");
    }
}