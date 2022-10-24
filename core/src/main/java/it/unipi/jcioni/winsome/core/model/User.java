package it.unipi.jcioni.winsome.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class User {
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String password;
    /*
    // Key: Utente seguito <User> (unicità), Value: utente che segue <String>
    private HashMap<User, String> follows;
    // Key: Utente che mi segue <String> (unicità), Value: utente seguito <String>
    private HashMap<String, String> followers;

     */
    private Interactions interactions;
    private List<Tag> tags;
    private Blog blog;
    private Wallet wallet = new Wallet();

    public User(String username, String password, List<Tag> tags) {
        this.username = username;
        this.password = password;
        this.tags = tags;
        /*
        this.follows = new HashMap<>();
        this.followers = new HashMap<>();
         */
        this.blog = new Blog();
        this.interactions = new Interactions();
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
/*
    public HashMap<User, String> getFollows() {
        return follows;
    }

    public HashMap<String, String> getFollowers() {
        return followers;
    }

    public boolean addFollows(String mainUser, User following) {
        for (User u: follows.keySet()) {
            if (u.equals(following)) {
                System.err.println("[SERV] - Stai già seguendo questo utente.");
                return false;
            }
        }
        follows.put(following, mainUser);
        return true;
    }

    public void addFollowers(String mainUser, String follower) {
        for (String s: followers.keySet()) {
            if (s.equals(follower)) {
                return;
            }
        }
        followers.put(follower, mainUser);
    }

    public boolean removeFollows(String mainUser, User followed) {
        boolean result = follows.remove(followed, mainUser);
        if (!result) {
            System.err.println("[SERV] - Errore, non stavi seguendo questo utente.");
        }
        return result;
    }

    public void removeFollowers(String exFollower) {
        try{
            followers.remove(exFollower);
        } catch (Exception e) {
            System.err.println("[SERV] - Errore nella rimozione del follower.");
        }

    }
*/
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
