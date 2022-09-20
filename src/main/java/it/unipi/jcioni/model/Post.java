package it.unipi.jcioni.model;

public class Post extends Content {
    private Post rewin;
    private String text;

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

    public Post retrieveOriginal() {
        Post original = this;
        while (original.getRewin() != null) {
            original = original.getRewin();
        }
        return original;
    }
}
