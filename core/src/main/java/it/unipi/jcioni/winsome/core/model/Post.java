package it.unipi.jcioni.winsome.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Post extends Content {
    private Post rewin;
    private String text;
    private List<Comment> comments = new ArrayList<>();
    private Map<User, Vote> votes = new HashMap<>();

//    public Post(User creator) {
//        super(creator);
//    }

    // Post rewin
    public Post(User creator, Post rewin, String text) {
        super(creator);
        this.rewin = rewin;
        this.text = text;
    }

    // Post originale
    public Post(User creator, String text) {
        super(creator);
        this.text = text;
    }

    public Post getRewin() {
        return rewin;
    }

    public String getText() {
        return text;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        if (this.comments == null) this.comments = new ArrayList<>();
    }

    public Map<User, Vote> getVotes() {
        return votes;
    }

    public void setVotes(Map<User, Vote> votes) {
        this.votes = votes;
        if (this.votes == null) this.votes = new HashMap<>();
    }

    public Post retrieveOriginal() {
        Post original = this;
        while (original.getRewin() != null) {
            original = original.getRewin();
        }
        return original;
    }
}
