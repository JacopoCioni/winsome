package it.unipi.jcioni.model;

/*
    This class represents the single winsome transaction
 */
public class Transaction {

        private final double value;
        private final String msg;
        private final long date;

    public Transaction(double value, String msg, long date) {
        this.value = value;
        this.msg = msg;
        this.date = System.currentTimeMillis();
    }

    public double getValue() {
        return value;
    }

    public String getMsg() {
        return msg;
    }

    public long getDate() {
        return date;
    }
}
