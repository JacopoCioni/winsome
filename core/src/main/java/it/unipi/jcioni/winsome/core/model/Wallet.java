package it.unipi.jcioni.winsome.core.model;

import java.util.concurrent.ConcurrentLinkedDeque;

public class Wallet {

    private final User owner;

    // ThreadSafe queue, you might have multiple threads wanting to access this collection
    private ConcurrentLinkedDeque<Transaction> transactions = new ConcurrentLinkedDeque<>();

    public Wallet(User owner) {
        this.owner = owner;
    }

    public User getOwner() {
        return owner;
    }

    public ConcurrentLinkedDeque<Transaction> getTransactions() {
        return transactions;
    }

    public double balance() {
        double balance = 0;
        for (Transaction t : transactions) {
            balance = balance + t.getValue();
        }
        return balance;
    }

    /*
    This method allows you to add a new transaction in your wallet.
    @param value of the new transaction
    @param message of the new transaction
    @return new balance
     */
    public double addTransaction(double value, String msg) {
        transactions.add(new Transaction(value, msg));
        return balance();
    }
}