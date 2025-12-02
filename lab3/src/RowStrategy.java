public class RowStrategy implements IStrategy{
    @Override
    public void execute(int[][] A, int[][] B, int[][] C, int numberOfThreads, int threadId, int M, int N) {
        final int total_elements = M * N;

        final int el_per_thread = total_elements / numberOfThreads;
        final int remaining_els = total_elements % numberOfThreads;

        int start_index = threadId * el_per_thread + Math.min(threadId, remaining_els);
        int end_index = start_index + el_per_thread + (threadId < remaining_els ? 1 : 0);

        for (int i = start_index; i < end_index; i++) {
            int row = i / N;
            int col = i % N;

            SingleElementHelper.computeSingleElement(A, B, C, row, col, threadId);
        }
    }
}
