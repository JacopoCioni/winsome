package it.unipi.jcioni.winsome.core.model;

public enum Vote {
    UP(1),
    DOWN(-1);

    private final int value;
    private final long voteTime;

    private Vote(int value) {
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
