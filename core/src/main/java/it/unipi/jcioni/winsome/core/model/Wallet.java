package it.unipi.jcioni.winsome.core.model;

import java.util.concurrent.ConcurrentLinkedDeque;

public class Wallet {

    private ConcurrentLinkedDeque<Transaction> transactions = new ConcurrentLinkedDeque<>();
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
