

public class MatrixRunnable implements Runnable{
    private final int[][] A, B, C;
    private final int numberOfThreads, threadId;
    private final IStrategy strategy;
    private final int M, N; // M = rows C, N = cols C
    private final int K_DIM; // Common dimension

    public MatrixRunnable(int[][] a, int[][] b, int[][] c, int numberOfThreads, int threadId, IStrategy strategy) {
        A = a;
        B = b;
        C = c;
        this.numberOfThreads = numberOfThreads;
        this.threadId = threadId;
        M = A.length;       // Rows in C
        N = B[0].length;    // Cols in C
        K_DIM = B.length;   // Common dimension
        this.strategy = strategy;
    }

    @Override
    public void run() {
        strategy.execute(A, B, C, numberOfThreads, threadId, M, N);
    }
}
