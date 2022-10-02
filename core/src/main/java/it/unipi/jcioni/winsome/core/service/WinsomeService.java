package it.unipi.jcioni.winsome.core.service;

import it.unipi.jcioni.winsome.core.exception.InvalidOperationException;
import it.unipi.jcioni.winsome.core.exception.PostNotFoundException;
import it.unipi.jcioni.winsome.core.exception.SameUserException;
import it.unipi.jcioni.winsome.core.exception.UserNotFoundException;
import it.unipi.jcioni.winsome.core.model.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface WinsomeService extends Remote {
    int SERVER_TCP_PORT = 8080;
    int SERVER_RMI_PORT = 6969;
    String RMI_SERVER_REGISTRY_NAME = "winsome-server";
    int RMI_CALLBACK_CLIENT_PORT = 6970;
    String RMI_CALLBACK_CLIENT_REGISTRY_NAME = "winsome-server-callback";
    String SERVER_ADDRESS = "localhost";

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

    Post showPost(String idPost)
            throws RemoteException, PostNotFoundException;

    void deletePost(User user, String idPost)
            throws RemoteException, InvalidOperationException, PostNotFoundException;

    Wallet getWallet(User user) throws RemoteException;

    void rewinPost(User user, String idPost) throws RemoteException;

    void ratePost(String idPost, Vote vote, User user)
            throws RemoteException, UserNotFoundException, PostNotFoundException, InvalidOperationException;

    void addComment(String idPost, Comment comment, User user)
            throws RemoteException, UserNotFoundException, PostNotFoundException, InvalidOperationException;
}
