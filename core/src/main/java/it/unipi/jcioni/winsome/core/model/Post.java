package it.unipi.jcioni.winsome.core.model;

import java.util.*;

public class Post extends Content {
    private Post rewin;
    public static final int MAX_TITLE_LENGHT = 20;
    public static final int MAX_TEXT_LENGHT = 500;
    private final String idPost = UUID.randomUUID().toString();
    private final Date timestamp = new Date();
    private String title;
    private String text;
    private List<Comment> comments = new ArrayList<>();
    private Map<User, Vote> votes = new HashMap<>();

//    public Post(User creator) {
//        super(creator);
//    }

    // Post rewin
    public Post(User creator, Post rewin, String title, String text) {
        super(creator);
        this.rewin = rewin;
        this.title = title;
        this.text = text;
    }

    // Post originale
    public Post(User creator, String title, String text) {
        super(creator);
        this.title = title;
        this.text = text;
    }

    public Post getRewin() {
        return rewin;
    }

    public String getIdPost() {
        return idPost;
    }

    public Date getTimestamp() {
        return timestamp;
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

    public boolean addVote(User user, Vote vote) {
        if (user == null || vote == null || votes.get(user) != null) return false;
        votes.put(user, vote);
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return idPost.equals(post.idPost);
    }
}
