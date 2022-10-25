package it.unipi.jcioni.winsome.core.model;

import java.util.List;

public class User {
    private String username;
    private String password;
    private Interactions interactions;
    private List<Tag> tags;
    private Blog blog;
    private Wallet wallet = new Wallet();

    public User(String username, String password, List<Tag> tags) {
        this.username = username;
        this.password = password;
        this.tags = tags;
        this.blog = new Blog();
        this.interactions = new Interactions();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public Interactions getInteractions() {
        return interactions;
    }

    public Blog getBlog() {
        return blog;
    }

    public Wallet getWallet() {
        return wallet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username);
    }
}
