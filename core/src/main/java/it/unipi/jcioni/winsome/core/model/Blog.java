package it.unipi.jcioni.winsome.core.model;

import java.util.concurrent.ConcurrentLinkedDeque;

public class Blog {
    private transient ConcurrentLinkedDeque<Post> posts;
    public Blog() {
        this.posts = new ConcurrentLinkedDeque<>();
    }

    public ConcurrentLinkedDeque<Post> getPosts() {
        return posts;
    }
}
