package it.unipi.jcioni.winsome.core.service;

import it.unipi.jcioni.winsome.core.model.Tag;
import it.unipi.jcioni.winsome.core.model.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface WinsomeService extends Remote {
    boolean register(String username, String password, String tags);
    boolean login(String username, String password);
    void createPost(String title, String text, User u) throws RemoteException;
    void followUser(String username, String following);
    // void unfollowUser(String username, String unfollowing);
}
