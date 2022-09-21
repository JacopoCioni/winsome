package it.unipi.jcioni.model;

import java.util.concurrent.ConcurrentLinkedDeque;

public class Wallet {

    private final User userWallet;
    private int balance;
    // Coda ThreadSafe, mi immagino che ci siano più Thread in accesso a questa collezione
    private ConcurrentLinkedDeque<Transaction> transactions;

    public Wallet(User userWallet) {
        this.userWallet = userWallet;
    }

    public User getUserWallet() {
        return userWallet;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
