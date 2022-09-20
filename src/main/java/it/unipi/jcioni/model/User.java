package it.unipi.jcioni.model;

import java.util.List;

public class User {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private List<User> follows;
    private List<Tag> tags;

    private Blog blog;

    public User(String firstname, String lastname, String email, String password, List<User> follows, List<Tag> tags) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.follows = follows;
        this.tags = tags;
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
    }

    public List<Tag> getTags() {
        return tags;
    }
}
