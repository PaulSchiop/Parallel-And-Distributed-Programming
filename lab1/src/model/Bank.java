package model;

import java.util.concurrent.locks.*;

public class Bank {
    private int balance;
    private final Lock lock = new ReentrantLock();

    public Bank(int balance) {
        this.balance = balance;
    }

    public int getBalance() {
        return balance;
    }

    public void deposit(int amount) {
        balance += amount;
    }

    public void withdraw(int amount) {
        balance -= amount;
    }

    public Lock getLock() {
        return this.lock;
    }
}
