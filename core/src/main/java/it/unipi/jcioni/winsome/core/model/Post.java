package it.unipi.jcioni.winsome.core.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Post {
    public static final int MAX_TITLE_LENGHT = 20;
    public static final int MAX_TEXT_LENGHT = 500;
    private final String idPost = UUID.randomUUID().toString();
    private final Date timestamp = new Date();
    private String creator;
    private String title;
    private String text;
    private int iterazioniRewards;
    private ConcurrentLinkedDeque<Comment> comments;
    private ConcurrentHashMap<String, Vote> votes;

    public Post(String creator, String title, String text) {
        this.creator = creator;
        this.title = title;
        this.text = text;
        this.comments = new ConcurrentLinkedDeque<>();
        this.votes = new ConcurrentHashMap<>();
        this.iterazioniRewards = 0;
    }

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

    public ConcurrentLinkedDeque<Comment> getComments() {
        return comments;
    }

    public ConcurrentLinkedDeque<Comment> getComments(long timeCheck) {
        ConcurrentLinkedDeque<Comment> temp = new ConcurrentLinkedDeque<>();
        for (Comment c: comments) {
            if (c.getCommentTime() >= timeCheck) {
                temp.add(c);
            }
        }
        return temp;
    }

    public ConcurrentHashMap<String, Vote> getVotes() {
        return votes;
    }

    public ConcurrentHashMap<String, Vote> getVotes(long timeCheck) {
        ConcurrentHashMap<String, Vote> temp = new ConcurrentHashMap<>();
        for (Vote v: votes.values()) {
            if (v.getVoteTime() >= timeCheck) {
                // Ricerco l'autore del voto
                for (String i: votes.keySet()) {
                    if (votes.get(i).equals(v)) {
                        temp.put(i, v);
                    }
                }
            }
        }
        return temp;
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

    public int getNumberOfUserComments(String creator) {
        int value = 0;
        for (Comment c: comments) {
            if (c.getCreator().equals(creator)) {
                value++;
            }
        }
        return value;
    }

    public boolean addVote(String user, Vote vote) {
        // Restituisce false se l'utente ha già votato il post
        if (votes.get(user) != null) return false;
        votes.put(user, vote);
        return true;
    }

    public void addIterazioni() {
        iterazioniRewards++;
    }

    public int getIterazioniRewards() {
        return iterazioniRewards;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return idPost.equals(post.idPost);
    }
}
