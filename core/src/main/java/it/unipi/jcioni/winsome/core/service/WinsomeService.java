package it.unipi.jcioni.winsome.core.service;

import it.unipi.jcioni.winsome.core.exception.InvalidOperationException;
import it.unipi.jcioni.winsome.core.exception.PostNotFoundException;
import it.unipi.jcioni.winsome.core.exception.SameUserException;
import it.unipi.jcioni.winsome.core.exception.UserNotFoundException;
import it.unipi.jcioni.winsome.core.model.Post;
import it.unipi.jcioni.winsome.core.model.User;
import it.unipi.jcioni.winsome.core.model.Vote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface WinsomeService extends Remote {
    boolean register(String username, String password, String tags) throws RemoteException;
    boolean login(String username, String password) throws RemoteException;
    boolean logout(String username) throws RemoteException;
    List<User> listUsers(User user) throws RemoteException;
    List<User> listFollowers(User user) throws RemoteException;
    List<User> listFollowing(User following) throws RemoteException;
    void followUser(String username, String following)
            throws RemoteException, SameUserException, UserNotFoundException, InvalidOperationException;
    void unfollowUser(String username, String followed)
            throws RemoteException, SameUserException, UserNotFoundException, InvalidOperationException;
    List<Post> viewBlog(User user) throws RemoteException;
    void createPost(String title, String text, User u) throws RemoteException;
    List<Post> showFeed(User user) throws RemoteException;
    void rewinPost(User user, String idPost) throws RemoteException;

    void ratePost(String idPost, Vote vote, User user)
            throws RemoteException, UserNotFoundException, PostNotFoundException, InvalidOperationException;
}
