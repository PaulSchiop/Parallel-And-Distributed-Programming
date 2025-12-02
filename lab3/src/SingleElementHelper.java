public class SingleElementHelper {
    public static void computeSingleElement(int[][] A, int[][] B, int[][] C, int row, int col, int threadId) {
        // Correct: n should be the common dimension (cols of A / rows of B)
        int n = B.length; // or A[0].length
        int sum = 0;

        for (int k = 0; k < n; k++) {
            sum += A[row][k] * B[k][col];
        }
        C[row][col] = sum;

        // Add the debug print required by the prompt
        if (Main.DEBUG_PRINT) {
            // Synchronized is required to prevent jumbled/corrupted output
            synchronized (System.out) {
                System.out.println("Thread " + threadId + " computing (" + row + ", " + col + ")");
            }
        }
    }
}