package it.unipi.jcioni.winsome.core.model;

import it.unipi.jcioni.winsome.core.exception.InvalidOperationException;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class User {
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String password;
    private List<User> follows;
    private ArrayList<String> followers;
    private List<Tag> tags;
    private Blog blog;
    private Wallet wallet = new Wallet();

    public User(String username, String password, List<Tag> tags) {
        this.username = username;
        this.password = password;
        this.tags = tags;
        this.follows = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.blog = new Blog();
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<User> getFollows() {
        return follows;
    }

    public ArrayList<String> getFollowers() {
        return followers;
    }

    public void setFollows(List<User> follows) {
        this.follows = follows;
        if (this.follows == null) this.follows = new ArrayList<>();
    }

    public boolean addFollows(User following) {
        for (User u: follows) {
            if (u.equals(following)) {
                System.err.println("[SERV] - Stai già seguendo questo utente.");
                return false;
            }
        }
        return follows.add(following);
    }

    public void addFollowers(String follower) {
        for (String s: followers) {
            if (s.equals(follower)) {
                return;
            }
        }
        followers.add(follower);
    }

    public boolean removeFollows(User followed) {
        boolean result = follows.remove(followed);
        if (!result) {
            System.err.println("[SERV] - Errore, non stavi seguendo questo utente.");
        }
        return result;
        /*
        if (!follows.remove(followed)) {
            throw new InvalidOperationException();
        }
         */
    }

    public void removeFollowers(String exFollower) {
        boolean result = followers.remove(exFollower);
        if (!result) {
            System.err.println("[SERV] - Errore nella rimozione del follower.");
        }
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
        if (this.tags == null) this.tags = new ArrayList<>();
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
