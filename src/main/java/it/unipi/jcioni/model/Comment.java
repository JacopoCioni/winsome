package it.unipi.jcioni.model;

public class Comment extends Content {
    private String value;

    public Comment(User creator, String value) {
        super(creator);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
