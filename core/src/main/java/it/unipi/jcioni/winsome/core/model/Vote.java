package it.unipi.jcioni.winsome.core.model;

public enum Vote {
    UP(1),
    DOWN(-1);

    private final int value;

    private Vote(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
