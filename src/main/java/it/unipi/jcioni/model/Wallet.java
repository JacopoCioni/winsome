package it.unipi.jcioni.model;

public class Wallet {

    private final User userWallet;
    private int balance;

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
