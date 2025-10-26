import model.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int numAccounts = 10000;
        int numThreads = 9000;
        int initialBalance = 1000;

        Bank[] accounts = new Bank[numAccounts];
        for (int i = 0; i < numAccounts; i++) {
            accounts[i] = new Bank(initialBalance);
        }


        int totalInitialBalance = 0;
        for (Bank account : accounts) {
            totalInitialBalance += account.getBalance();
        }
        System.out.println("Total initial balance: " + totalInitialBalance);


        ConsistencyChecker checker = new ConsistencyChecker(accounts, totalInitialBalance);
        Thread checkerThread = new Thread(checker);

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new TransferOperation(accounts));
        }

        long startTime = System.currentTimeMillis();

        checkerThread.start();

        for (int i = 0; i < numThreads; i++) {
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }

        checker.stop();
        checkerThread.join();

        long endTime = System.currentTimeMillis();

        int totalFinalBalance = 0;
        for (Bank account : accounts) {
            totalFinalBalance += account.getBalance();
        }
        System.out.println("Total final balance: " + totalFinalBalance);

        if (totalInitialBalance == totalFinalBalance) {
            System.out.println("Consistency check passed.");
        } else {
            System.out.println("Consistency check failed.");
        }

        System.out.println("Time taken: " + (endTime - startTime) + " ms");
    }
}