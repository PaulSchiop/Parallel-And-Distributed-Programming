package model;

import java.util.Random;

public class TransferOperation implements Runnable {
    private final Bank[] accounts;
    private final int numAccounts;
    private final Random random = new Random();

    public TransferOperation(Bank[] accounts) {
        this.accounts = accounts;
        this.numAccounts = accounts.length;
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; i++) {
            int fromAccountIndex = random.nextInt(numAccounts);
            int toAccountIndex = random.nextInt(numAccounts);
            int amount = random.nextInt(100);

            if (fromAccountIndex != toAccountIndex) {
                Bank fromAccount = accounts[fromAccountIndex];
                Bank toAccount = accounts[toAccountIndex];

                Bank firstLock = fromAccountIndex < toAccountIndex ? fromAccount : toAccount;
                Bank secondLock = fromAccountIndex < toAccountIndex ? toAccount : fromAccount;

                firstLock.getLock().lock();
                try {
                    secondLock.getLock().lock();
                    try {
                        if (fromAccount.getBalance() >= amount) {
                            fromAccount.withdraw(amount);
                            toAccount.deposit(amount);
                        }
                    } finally {
                        secondLock.getLock().unlock();
                    }
                } finally {
                    firstLock.getLock().unlock();
                }
            }
        }
    }
}

