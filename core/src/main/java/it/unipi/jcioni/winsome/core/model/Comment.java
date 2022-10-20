package it.unipi.jcioni.winsome.core.model;

public class Comment {
    private String value;
    private String creator;
    private long commentTime;

    public Comment(String creator, String value) {
        this.creator = creator;
        this.value = value;
        this.commentTime = System.currentTimeMillis();
    }

    public String getValue() {
        return value;
    }

    public String getCreator() {
        return creator;
    }

    public long getCommentTime() {
        return commentTime;
    }
}
