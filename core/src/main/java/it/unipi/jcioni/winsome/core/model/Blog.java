package it.unipi.jcioni.winsome.core.model;

import java.util.ArrayList;
import java.util.List;

public class Blog {
    private List<Post> posts = new ArrayList<>();

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        if (this.posts == null) this.posts = new ArrayList<>();
    }
}
