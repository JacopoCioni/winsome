package it.unipi.jcioni.winsome.core.service;

import it.unipi.jcioni.winsome.core.exception.InvalidOperationException;
import it.unipi.jcioni.winsome.core.exception.SameUserException;
import it.unipi.jcioni.winsome.core.exception.UserNotFoundException;
import it.unipi.jcioni.winsome.core.model.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface WinsomeService extends Remote {
    boolean register(String username, String password, String tags) throws RemoteException;
    boolean login(String username, String password) throws RemoteException;
    boolean logout(String username) throws RemoteException;

    List<User> listUsers(User user) throws RemoteException;

    void createPost(String title, String text, User u) throws RemoteException;
    void followUser(String username, String following)
            throws RemoteException, SameUserException, UserNotFoundException, InvalidOperationException;
    // void unfollowUser(String username, String unfollowing);
}
