public class InterleavedStrategy implements IStrategy{
    @Override
    public void execute(int[][] A, int[][] B, int[][] C, int numberOfThreads, int threadId, int M, int K) {
        int N = M * K;

        for (int i = threadId; i < N; i += numberOfThreads) {
            int row = i / K;
            int col = i % K;

            SingleElementHelper.computeSingleElement(A, B, C, row, col, threadId);
        }
    }
}
