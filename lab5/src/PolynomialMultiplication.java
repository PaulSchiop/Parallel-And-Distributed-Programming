import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class PolynomialMultiplication {

    public static class Polynomial {
        int[] coeffs;

        public Polynomial(int[] coeffs) {
            this.coeffs = coeffs;
        }

        public Polynomial(int degree) {
            this.coeffs = new int[degree + 1];
        }

        public int getLength() {
            return coeffs.length;
        }

        @Override
        public String toString() {
            return "Degree " + (coeffs.length - 1);
        }
    }

    public static Polynomial multiplySequential(Polynomial p1, Polynomial p2) {
        int size1 = p1.coeffs.length;
        int size2 = p2.coeffs.length;
        int[] result = new int[size1 + size2 - 1];

        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size2; j++) {
                result[i + j] += p1.coeffs[i] * p2.coeffs[j];
            }
        }
        return new Polynomial(result);
    }


    public static Polynomial multiplyParallel(Polynomial p1, Polynomial p2) throws InterruptedException {
        int size1 = p1.coeffs.length;
        int size2 = p2.coeffs.length;
        int resultSize = size1 + size2 - 1;
        int[] result = new int[resultSize];

        int numThreads = 7;

        int chunkSize = resultSize / numThreads;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int t = 0; t < numThreads; t++) {
            final int start = t * chunkSize;
            final int end = (t == numThreads - 1) ? resultSize : (t + 1) * chunkSize;

            executor.submit(() -> {
                for (int k = start; k < end; k++) {
                    int sum = 0;
                    int startI = Math.max(0, k - size2 + 1);
                    int endI = Math.min(k, size1 - 1);

                    for (int i = startI; i <= endI; i++) {
                        sum += p1.coeffs[i] * p2.coeffs[k - i];
                    }
                    result[k] = sum;
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        return new Polynomial(result);
    }

    public static Polynomial karatsubaSequential(Polynomial p1, Polynomial p2) {
        if (p1.getLength() < 64 || p2.getLength() < 64) {
            return multiplySequential(p1, p2);
        }

        int n = Math.max(p1.getLength(), p2.getLength());
        int half = n / 2;

        Polynomial[] splitP1 = split(p1, half);
        Polynomial[] splitP2 = split(p2, half);

        Polynomial low1 = splitP1[0];
        Polynomial high1 = splitP1[1];
        Polynomial low2 = splitP2[0];
        Polynomial high2 = splitP2[1];

        Polynomial z0 = karatsubaSequential(low1, low2);
        Polynomial z2 = karatsubaSequential(high1, high2);

        Polynomial lowHigh1 = add(low1, high1);
        Polynomial lowHigh2 = add(low2, high2);
        Polynomial z1 = karatsubaSequential(lowHigh1, lowHigh2);

        Polynomial middle = subtract(subtract(z1, z2), z0);

        Polynomial result = new Polynomial(2 * n);
        return assemble(z0, z2, middle, half);
    }

    static class KaratsubaTask extends RecursiveTask<Polynomial> {
        Polynomial p1, p2;
        int threshold = 128; // Tuning parameter

        public KaratsubaTask(Polynomial p1, Polynomial p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        @Override
        protected Polynomial compute() {
            if (p1.getLength() <= threshold || p2.getLength() <= threshold) {
                return karatsubaSequential(p1, p2);
            }

            int n = Math.max(p1.getLength(), p2.getLength());
            int half = n / 2;

            Polynomial[] splitP1 = split(p1, half);
            Polynomial[] splitP2 = split(p2, half);

            // Create subtasks
            KaratsubaTask taskZ0 = new KaratsubaTask(splitP1[0], splitP2[0]);
            KaratsubaTask taskZ2 = new KaratsubaTask(splitP1[1], splitP2[1]);

            // Calculate sum terms for the middle calculation
            Polynomial sumP1 = add(splitP1[0], splitP1[1]);
            Polynomial sumP2 = add(splitP2[0], splitP2[1]);
            KaratsubaTask taskZ1 = new KaratsubaTask(sumP1, sumP2);

            taskZ0.fork();
            taskZ2.fork();
            taskZ1.fork();

            Polynomial z0 = taskZ0.join();
            Polynomial z2 = taskZ2.join();
            Polynomial z1 = taskZ1.join();

            Polynomial middle = subtract(subtract(z1, z2), z0);
            return assemble(z0, z2, middle, half);
        }
    }

    public static Polynomial karatsubaParallel(Polynomial p1, Polynomial p2) {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        return pool.invoke(new KaratsubaTask(p1, p2));
    }

    private static Polynomial[] split(Polynomial p, int splitIdx) {
        int len = p.getLength();
        int[] low = new int[Math.min(len, splitIdx)];
        int[] high = new int[Math.max(0, len - splitIdx)];

        System.arraycopy(p.coeffs, 0, low, 0, low.length);
        if (len > splitIdx) {
            System.arraycopy(p.coeffs, splitIdx, high, 0, high.length);
        }
        return new Polynomial[] { new Polynomial(low), new Polynomial(high) };
    }

    private static Polynomial add(Polynomial p1, Polynomial p2) {
        int maxLen = Math.max(p1.getLength(), p2.getLength());
        int[] result = new int[maxLen];
        for (int i = 0; i < maxLen; i++) {
            int v1 = (i < p1.getLength()) ? p1.coeffs[i] : 0;
            int v2 = (i < p2.getLength()) ? p2.coeffs[i] : 0;
            result[i] = v1 + v2;
        }
        return new Polynomial(result);
    }

    private static Polynomial subtract(Polynomial p1, Polynomial p2) {
        int maxLen = Math.max(p1.getLength(), p2.getLength());
        int[] result = new int[maxLen];
        for (int i = 0; i < maxLen; i++) {
            int v1 = (i < p1.getLength()) ? p1.coeffs[i] : 0;
            int v2 = (i < p2.getLength()) ? p2.coeffs[i] : 0;
            result[i] = v1 - v2;
        }
        return new Polynomial(result);
    }

    private static Polynomial assemble(Polynomial z0, Polynomial z2, Polynomial middle, int half) {
        int len = Math.max(z0.getLength(), Math.max(middle.getLength() + half, z2.getLength() + 2 * half));
        int[] result = new int[len];

        for(int i=0; i<z0.getLength(); i++) result[i] += z0.coeffs[i];

        for(int i=0; i<middle.getLength(); i++) result[i + half] += middle.coeffs[i];

        for(int i=0; i<z2.getLength(); i++) result[i + 2 * half] += z2.coeffs[i];

        return new Polynomial(result);
    }

    // Helper to generate random polynomial
    public static Polynomial generateRandom(int degree) {
        Random r = new Random();
        int[] coeffs = new int[degree + 1];
        for(int i=0; i<=degree; i++) coeffs[i] = r.nextInt(10);
        return new Polynomial(coeffs);
    }

    public static void main(String[] args) throws InterruptedException {
        int DEGREE = 20000;
        System.out.println("Generating polynomials of degree " + DEGREE + "...");
        Polynomial p1 = generateRandom(DEGREE);
        Polynomial p2 = generateRandom(DEGREE);

        System.out.println("Starting tests...");

        multiplySequential(generateRandom(100), generateRandom(100));

        // 1. Sequential
        long start = System.currentTimeMillis();
        multiplySequential(p1, p2);
        long end = System.currentTimeMillis();
        System.out.println("Sequential Regular: " + (end - start) + " ms");

        // 2. Parallel Regular
        start = System.currentTimeMillis();
        multiplyParallel(p1, p2);
        end = System.currentTimeMillis();
        System.out.println("Parallel Regular:   " + (end - start) + " ms");

        // 3. Sequential Karatsuba
        start = System.currentTimeMillis();
        karatsubaSequential(p1, p2);
        end = System.currentTimeMillis();
        System.out.println("Sequential Karatsuba: " + (end - start) + " ms");

        // 4. Parallel Karatsuba
        start = System.currentTimeMillis();
        karatsubaParallel(p1, p2);
        end = System.currentTimeMillis();
        System.out.println("Parallel Karatsuba:   " + (end - start) + " ms");
    }
}