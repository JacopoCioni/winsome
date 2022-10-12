package it.unipi.jcioni.winsome.core.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Post {
    //private Post rewin;
    public static final int MAX_TITLE_LENGHT = 20;
    public static final int MAX_TEXT_LENGHT = 500;
    private final String idPost = UUID.randomUUID().toString();
    private final Date timestamp = new Date();
    private String creator;
    private String title;
    private String text;
    private List<Comment> comments;
    private ConcurrentHashMap<String, Vote> votes;

    /* Post rewin
    public Post(String creator, Post rewin, String title, String text) {
        this.creator = creator;
        //this.rewin = rewin;
        this.title = title;
        this.text = text;
        this.comments = new ArrayList<>();
        this.votes = new ConcurrentHashMap<>();
    }
     */

    // Post originale
    public Post(String creator, String title, String text) {
        this.creator = creator;
        this.title = title;
        this.text = text;
        this.comments = new ArrayList<>();
        this.votes = new ConcurrentHashMap<>();
    }

    //public Post getRewin() {
    //    return rewin;
    //}

    public String getIdPost() {
        return idPost;
    }

    public String getCreator() {
        return creator;
    }

    public String getTitle() {
        return title;
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

    public Map<String, Vote> getVotes() {
        return votes;
    }

    public int getNumberOfUpVotes() {
        int upVotes = 0;
        for (Vote vote: votes.values()) {
            if (vote.getValue() == 1) {
                upVotes++;
            }
        }
        return upVotes;
    }

    public int getNumberOfDownVotes() {
        int downVotes = 0;
        for (Vote vote: votes.values()) {
            if (vote.getValue() == 1) {
                downVotes++;
            }
        }
        return downVotes;
    }

/*    public Post retrieveOriginal() {
        Post original = this;
        while (original.getRewin() != null) {
            original = original.getRewin();
        }
        return original;
    }

 */

    public boolean addVote(String user, Vote vote) {
        if (user == null || vote == null || votes.get(user) != null) return false;
        votes.put(user, vote);
        return true;
    }

    public boolean addComment(Comment comment) {
        if (comment == null) return false;
        comments.add(comment);
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
