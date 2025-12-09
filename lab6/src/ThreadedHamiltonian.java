import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadedHamiltonian {
    private final Graph graph;
    private final int startNode;

    private final AtomicBoolean solutionFound = new AtomicBoolean(false);

    private volatile List<Integer> resultPath = null;

    public ThreadedHamiltonian(Graph graph, int startNode) {
        this.graph = graph;
        this.startNode = startNode;
    }

    public List<Integer> solve(int numThreads) {
        // Reset state in case object is reused
        solutionFound.set(false);
        resultPath = null;

        boolean[] visited = new boolean[graph.getNumVertices()];
        List<Integer> path = new ArrayList<>();

        path.add(startNode);
        visited[startNode] = true;

        try {
            search(startNode, visited, path, numThreads);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return resultPath;
    }

    public void search(int current, boolean[] visited, List<Integer> path, int threadsAvailable) throws InterruptedException {
        if (solutionFound.get()) {
            return;
        }

        if (path.size() == graph.getNumVertices()) {
            if (graph.getNeighbors(current).contains(startNode)) {
                if (solutionFound.compareAndSet(false, true)) {
                    resultPath = new ArrayList<>(path);
                }
            }
            return;
        }

        List<Integer> validNeighbors = new ArrayList<>();
        for (int neighbor : graph.getNeighbors(current)) {
            if (!visited[neighbor]) {
                validNeighbors.add(neighbor);
            }
        }

        if (validNeighbors.isEmpty()) return;

        int branches = validNeighbors.size();

        if (threadsAvailable > 1) {
            List<Thread> childThreads = new ArrayList<>();

            int baseThreads = threadsAvailable / branches;
            int remainder = threadsAvailable % branches;

            for( int i = 0; i < branches - 1; i++) {
                int threadsForChild = baseThreads + (i < remainder ? 1 : 0);
                if (threadsForChild == 0) threadsForChild = 1;

                int neighbor = validNeighbors.get(i);

                boolean[] nextVisited = Arrays.copyOf(visited, visited.length);
                nextVisited[neighbor] = true;
                List<Integer> nextPath = new ArrayList<>(path);
                nextPath.add(neighbor);

                final int finalThreads = threadsForChild;
                Thread t = new Thread(() -> {
                    try {
                        search(neighbor, nextVisited, nextPath, finalThreads);
                    } catch (InterruptedException _) { }
                });
                childThreads.add(t);
                t.start();
            }

            for (Thread thread : childThreads) {
                thread.join();
            }

            int lastIndex = branches - 1;
            int threadsForLast = baseThreads + (lastIndex < remainder ? 1 : 0);
            int lastNeighbor = validNeighbors.get(lastIndex);

            boolean[] nextVisited = Arrays.copyOf(visited, visited.length);
            nextVisited[lastNeighbor] = true;
            List<Integer> nextPath = new ArrayList<>(path);
            nextPath.add(lastNeighbor);

            search(lastNeighbor, nextVisited, nextPath, threadsForLast);

        } else {
            for (int neighbor : validNeighbors) {
                if (solutionFound.get()) return;

                visited[neighbor] = true;
                path.add(neighbor);

                search(neighbor, visited, path, 1);

                path.removeLast();
                visited[neighbor] = false;
            }
        }


    }
}
