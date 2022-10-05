package it.unipi.jcioni.winsome.core.model;

import it.unipi.jcioni.winsome.core.exception.InvalidOperationException;
import it.unipi.jcioni.winsome.core.exception.LoginException;
import it.unipi.jcioni.winsome.core.exception.LogoutException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private Wallet wallet = new Wallet();

    public User(String username, String password, List<Tag> tags) {
        this.username = username;
        this.password = password;
        this.tags = tags;
    }

    public void login() throws LoginException {
        if (logged) throw new LoginException();
        logged = true;
    }

    public void logout() throws LogoutException {
        if (!logged) throw new LogoutException();
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

    public void addFollows(User following) throws InvalidOperationException {
        if (follows.stream().filter(user -> user.equals(following)).collect(Collectors.toList()).size() > 0) {
            throw new InvalidOperationException();
        }
        follows.add(following);
    }

    public void removeFollows(User followed) throws InvalidOperationException {
        if (!follows.remove(followed)) {
            throw new InvalidOperationException();
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
