public class ColumnStrategy implements IStrategy {
    @Override
    public void execute(int[][] A, int[][] B, int[][] C, int numberOfThreads, int threadId, int M, int N) {
        final int totalElements = M * N;

        final int el_per_thread = totalElements / numberOfThreads;
        final int remaining_els = totalElements % numberOfThreads;

        final int start_index = threadId * el_per_thread + Math.min(threadId, remaining_els);
        final int end_index = start_index + el_per_thread + (threadId < remaining_els ? 1 : 0);

        for (int i = start_index; i < end_index; i++) {
            int col = i / M;
            int row = i % M;

            SingleElementHelper.computeSingleElement(A, B, C, row, col, threadId);
        }
    }
}