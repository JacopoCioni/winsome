package it.unipi.jcioni.winsome.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Blog {
    private ConcurrentLinkedDeque posts;
    public Blog() {
        this.posts = new ConcurrentLinkedDeque<>();
    }

    public ConcurrentLinkedDeque<Post> getPosts() {
        return posts;
    }
}
