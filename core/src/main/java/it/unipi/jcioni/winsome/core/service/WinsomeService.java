package it.unipi.jcioni.winsome.core.service;

import it.unipi.jcioni.winsome.core.model.Tag;
import it.unipi.jcioni.winsome.core.model.User;

import java.rmi.Remote;
import java.util.ArrayList;

public interface WinsomeService extends Remote {
    boolean register(String username, String password, ArrayList<Tag> tags);
    boolean login(String username, String password);
    void createPost(String title, String text, User u);
    void followUser(String username, String following);
    void unfollowUser(String username, String unfollowing);
}
