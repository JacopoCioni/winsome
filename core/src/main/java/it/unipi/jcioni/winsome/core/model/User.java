package it.unipi.jcioni.winsome.core.model;

import it.unipi.jcioni.winsome.core.exception.LoginException;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String password;
    private boolean logged = false;
    private List<User> follows = new ArrayList<>();
    private List<Tag> tags;
    private Blog blog;

    public User(String username, String password, List<Tag> tags) {
        this.username = username;
        this.password = password;
        this.tags = tags;
    }

    public void login() throws LoginException {
        if (logged) throw new LoginException();
        logged = true;
    }

    public void logout() {
        logged = false;
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

    public void setFollows(List<User> follows) {
        this.follows = follows;
        if (this.follows == null) this.follows = new ArrayList<>();
    }

    public void addFollows(User following) {
        follows.add(following);
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
}
