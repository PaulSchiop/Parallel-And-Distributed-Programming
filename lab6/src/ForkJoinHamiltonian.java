import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinHamiltonian {
    private final Graph graph;
    private final int startNode;

    public ForkJoinHamiltonian(Graph graph, int startNode) {
        this.graph = graph;
        this.startNode = startNode;
    }

    public List<Integer> solve() {
        ForkJoinPool pool = ForkJoinPool.commonPool();

        boolean[] visited = new boolean[graph.getNumVertices()];
        visited[startNode] = true;
        List<Integer> path = new ArrayList<>();
        path.add(startNode);

        SearchTask task = new SearchTask(startNode, visited, path);
        return pool.invoke(task);
    }

    private class SearchTask extends RecursiveTask<List<Integer>> {
        private final int current;
        private final boolean[] visited;
        private final List<Integer> path;

        public SearchTask(int current, boolean[] visited, List<Integer> path) {
            this.current = current;
            this.visited = visited;
            this.path = path;
        }

        @Override
        protected List<Integer> compute() {
            // Base Case
            if (path.size() == graph.getNumVertices()) {
                if (graph.getNeighbors(current).contains(startNode)) {
                    return new ArrayList<>(path);
                }
                return null;
            }

            List<Integer> validNeighbors = new ArrayList<>();
            for (int n : graph.getNeighbors(current)) {
                if (!visited[n]) validNeighbors.add(n);
            }

            if (validNeighbors.isEmpty()) return null;

            List<SearchTask> tasks = new ArrayList<>();

            // Fork new tasks for neighbors
            for (int i = 0; i < validNeighbors.size() - 1; i++) {
                int neighbor = validNeighbors.get(i);
                SearchTask t = createSubTask(neighbor);
                tasks.add(t);
                t.fork();
            }

            int lastNeighbor = validNeighbors.getLast();
            SearchTask lastTask = createSubTask(lastNeighbor);
            List<Integer> result = lastTask.compute();

            if (result != null) return result;

            for (SearchTask t : tasks) {
                List<Integer> res = t.join();
                if (res != null) return res;
            }

            return null;
        }

        private SearchTask createSubTask(int neighbor) {
            boolean[] nextVisited = Arrays.copyOf(visited, visited.length);
            nextVisited[neighbor] = true;
            List<Integer> nextPath = new ArrayList<>(path);
            nextPath.add(neighbor);
            return new SearchTask(neighbor, nextVisited, nextPath);
        }
    }
}