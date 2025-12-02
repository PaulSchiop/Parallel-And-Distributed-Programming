public interface IStrategy {
    void execute(int[][] A, int[][] B, int[][] C, int numberOfThreads, int threadId, int M, int N);
}
