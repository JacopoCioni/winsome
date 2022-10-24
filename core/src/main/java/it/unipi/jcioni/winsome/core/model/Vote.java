package it.unipi.jcioni.winsome.core.model;

public class Vote {

    private final int value;
    private final long voteTime;

    public Vote(int value) {
        this.value = value;
        this.voteTime = System.currentTimeMillis();
    }


    public int getValue() {
        return value;
    }

    public long getVoteTime() {
        return voteTime;
    }
}
