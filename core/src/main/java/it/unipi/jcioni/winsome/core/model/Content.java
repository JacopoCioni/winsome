package it.unipi.jcioni.winsome.core.model;

public abstract class Content {
    private final User creator;

    public Content (User creator) {
        this.creator = creator;
    }

    public User getCreator() {
        return creator;
    }

}
