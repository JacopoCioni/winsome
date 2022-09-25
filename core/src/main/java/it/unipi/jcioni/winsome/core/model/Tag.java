package it.unipi.jcioni.winsome.core.model;

public class Tag {
    private final String value;

    public Tag(String value) {
        this.value = value.toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
