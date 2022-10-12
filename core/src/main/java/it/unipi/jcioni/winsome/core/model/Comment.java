package it.unipi.jcioni.winsome.core.model;

public class Comment {
    private String value;
    private String creator;

    public Comment(String creator, String value) {
        this.creator = creator;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getCreator() {
        return creator;
    }
}
