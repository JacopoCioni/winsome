package it.unipi.jcioni.winsome.core.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String password;
    private List<User> follows = new ArrayList<>();
    private List<Tag> tags = new ArrayList<>();
    private Blog blog;

    public User(String firstname, String lastname, String email, String username, String password, List<User> follows, List<Tag> tags) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.username = username;
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
