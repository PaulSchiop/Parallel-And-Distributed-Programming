import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class ConsistencyChecker implements Runnable {
    private final Bank[] accounts;
    private final int initialTotalBalance;
    private volatile boolean running = true; // 'volatile' ensures visibility across threads

    public ConsistencyChecker(Bank[] accounts, int initialTotalBalance) {
        this.accounts = accounts;
        this.initialTotalBalance = initialTotalBalance;
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        while (running) {
            List<Lock> acquiredLocks = new ArrayList<>();
            boolean allLocksAcquired = true;

            try {
                for (int i = 0; i < accounts.length; i++) {
                    Lock lock = accounts[i].getLock();
                    if (lock.tryLock()) {
                        acquiredLocks.add(lock);
                    } else {
                        allLocksAcquired = false;
                        break;
                    }
                }

                if (allLocksAcquired) {
                    int currentTotalBalance = 0;
                    for (Bank account : accounts) {
                        currentTotalBalance += account.getBalance();
                    }

                    if (currentTotalBalance != initialTotalBalance) {
                        System.err.println("!!! IN-PROGRESS CONSISTENCY CHECK FAILED !!!");
                        System.err.println("Expected: " + initialTotalBalance + ", Got: " + currentTotalBalance);
                    } else {
                        System.out.println("In-progress check passed. Total: " + currentTotalBalance);
                    }
                }

            } finally {
                for (int i = acquiredLocks.size() - 1; i >= 0; i--) {
                    acquiredLocks.get(i).unlock();
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                this.running = false;
            }
        }
    }
}
