package it.unipi.jcioni.winsome.core.model;

import java.util.Date;

/*
    This class represents the single winsome transaction
 */
public class Transaction {
    private final double value;
    private final String msg;
    private final Date timestamp = new Date(System.currentTimeMillis());

    public Transaction(double value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    public double getValue() {
        return value;
    }

    public String getMsg() {
        return msg;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
