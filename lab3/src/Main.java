import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Main {

    // --- Configuration ---

    // 1: RowStrategy
    // 2: ColumnStrategy
    // 3: InterleavedStrategy
    private static final int STRATEGY_CHOICE = 2;

    private static final int M_DIM = 1000; // Rows in A and C
    private static final int K_DIM = 1000; // Cols in A, Rows in B
    private static final int N_DIM = 1000; // Cols in B and C

    private static final int NUM_THREADS = 8;

    public static final boolean DEBUG_PRINT = true;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Setting up matrices...");
        System.out.printf("A: %d x %d\n", M_DIM, K_DIM);
        System.out.printf("B: %d x %d\n", K_DIM, N_DIM);
        System.out.printf("C: %d x %d (Result)\n", M_DIM, N_DIM);
        System.out.println("Using " + NUM_THREADS + " threads.");

        // 1. Allocate and initialize matrices
        int[][] A = new int[M_DIM][K_DIM];
        int[][] B = new int[K_DIM][N_DIM];
        int[][] C = new int[M_DIM][N_DIM]; // Result matrix

        initializeMatrix(A);
        initializeMatrix(B);

        // 2. Select strategy
        IStrategy strategy;
        String strategyName;
        switch (STRATEGY_CHOICE) {
            case 1:
                strategy = new RowStrategy();
                strategyName = "RowStrategy (Row-Major Chunks)";
                break;
            case 2:
                strategy = new ColumnStrategy();
                strategyName = "ColumnStrategy (Column-Major Chunks)";
                break;
            case 3:
                strategy = new InterleavedStrategy();
                strategyName = "InterleavedStrategy (Cyclic)";
                break;
            default:
                System.err.println("Invalid strategy choice.");
                return;
        }
        System.out.println("Using Strategy: " + strategyName);
        if (DEBUG_PRINT) {
            System.out.println("WARNING: DEBUG_PRINT is true. Performance will be very poor.");
        }


        // 3. Create threads and runnables
        Thread[] threads = new Thread[NUM_THREADS];
        MatrixRunnable[] runnables = new MatrixRunnable[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            runnables[i] = new MatrixRunnable(A, B, C, NUM_THREADS, i, strategy);
            threads[i] = new Thread(runnables[i]);
        }

        // 4. Start timer and launch threads
        System.out.println("Starting computation...");
        long startTime = System.nanoTime();

        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].start();
        }

        // 5. Join all threads (wait for them to finish)
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].join();
        }

        // 6. Stop timer and print results
        long endTime = System.nanoTime();
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        System.out.println("\nComputation finished.");
        System.out.println("Total time: " + durationMillis + " ms");

        // 7. (Optional) Print small results for verification
        if (M_DIM <= 10 && N_DIM <= 10) {
            printMatrix(C, "Result C");
        }
    }

    /**
     * Fills a matrix with simple dummy values.
     */
    private static void initializeMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = (i + j) % 10; // Simple initial values
            }
        }
    }

    /**
     * Prints the contents of a small matrix to the console.
     */
    private static void printMatrix(int[][] matrix, String name) {
        System.out.println("Matrix: " + name + " (" + matrix.length + "x" + matrix[0].length + ")");
        int maxRows = Math.min(matrix.length, 10);
        int maxCols = Math.min(matrix[0].length, 10);

        for (int i = 0; i < maxRows; i++) {
            System.out.print("[");
            for (int j = 0; j < maxCols; j++) {
                System.out.printf("%4d", matrix[i][j]);
                if (j < maxCols - 1) {
                    System.out.print(", ");
                }
            }
            if (matrix[0].length > maxCols) {
                System.out.print(", ...");
            }
            System.out.println("]");
        }
        if (matrix.length > maxRows) {
            System.out.println("...");
        }
        System.out.println();
    }
}