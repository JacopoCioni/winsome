package it.unipi.jcioni.winsome.core.service.impl;

import it.unipi.jcioni.winsome.core.model.*;
import it.unipi.jcioni.winsome.core.model.WinsomeData;
import it.unipi.jcioni.winsome.core.service.WinsomeService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WinsomeServiceImpl implements WinsomeService {
    // Questa lista deve essere eliminata totalmente
    private List<User> users = new ArrayList<>();
    // WinsomeData mi servirà per accedere alla lista degli utenti che è condivisa da tutto il servizio
    private WinsomeData winsomeData;
    public WinsomeServiceImpl(WinsomeData winsomeData) {
        this.winsomeData = winsomeData;
    }

    /*
    @param username, user username
    @param password, user password
    @return true/false
    @effects registra un utente alla piattaforma winsome.
     */
    @Override
    public boolean register(String username, String password, String tags) throws RemoteException {
        System.out.println("User registration " + username + " START");
        String[] array = tags.split("\\s+");
        if (username == null) {
            System.out.println("Error, there is no username.");
            return false;
        } else if (password == null) {
            System.out.println("Error, there is no password.");
            return false;
        } else if (array.length == 0) {
            System.out.println("Error, there are no tags.");
            return false;
        } else if (array.length > 5) {
            System.out.println("Error, there are to many tags.");
            return false;
        }
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(username)) {
                System.out.println("Error, username already exists.");
                return false;
            }
        }
        User newUser = new User(username, password,
                Arrays.stream(array).map(Tag::new).collect(Collectors.toList()));
        winsomeData.getUsers().add(newUser);
        System.out.println("User registration " + username + " END");
        return true;
    }

    @Override
    public ArrayList<String> startFollowers(String username, String password) throws RemoteException {
        User user = null;
        for(User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                user = u;
                break;
            }
        }
        if (user != null) {
            return user.getFollowers();
        }
        return new ArrayList<>();
    }

    /*
    @param username, user username
    @param password, user password
    @return true/false
    @throws NullPointerException se l'username o la password è errata
    @throws LoginException se l'utente è gia loggato
    @effects permette la login di un utente alla piattaforma winsome

    @Override
    public boolean login(String username, String password) throws RemoteException {
        System.out.println("User login " + username + " START");
        User user = users.stream()
                    .filter(u ->
                            u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst().orElse(null);
        try {
            user.login();
        } catch (NullPointerException ex) {
            System.out.println("Error, wrong username or password");
            return false;
        } catch (LoginException ex) {
            System.out.println("Error, user already logged");
            return false;
        }
        System.out.println("User login " + username + " END");
        return true;
    }

     */

    /*
    @param username, user username
    @return true/false
    @throws NullPointerException se l'utente non viene trovato
    @effects permette la logout di un utente dalla piattaforma winsome

    @Override
    public boolean logout(String username) throws RemoteException {
        System.out.println("User logout " + username + " START");
        User user = users.stream()
                .filter(u ->
                        u.getUsername().equals(username))
                .findFirst().orElse(null);
        try {
            user.logout();
        } catch (NullPointerException ex) {
            System.out.println("Error, user not found");
            return false;
        } catch (LogoutException e) {
            throw new RuntimeException(e);
        }
        System.out.println("User logout " + username + " END");
        return true;
    }

     */

    /*
    @param user, user
    @return list of users
    @effects viene restituita la lista degli utenti registrati al servizio che hanno almeno un tag in comune

    @Override
    public List<User> listUsers(User user) throws RemoteException {
        return users.stream().filter(u -> {
            for (int i = 0; i < user.getTags().size(); i++) {
                for (int j = 0; j < u.getTags().size(); j++) {
                    if (user.getTags().get(i).equals(u.getTags().get(j))) {
                        return !user.equals(u); //user.getUsername().equals(u.getUsername());
                    }
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

     */

    /*
    @Override
    public List<User> listFollowers(User user) throws RemoteException {
        return user.getFollows();
    }

     */

    /*
    @Override
    public List<User> listFollowing(User following) throws RemoteException {
        return users.stream().filter(
                user -> user.getFollows().contains(following))
                .collect(Collectors.toList());
    }

     */

    /*
    @param username, username dello user a cui voglio far followare
    @param following, username dello user che deve essere followato
    @throws RemoteException
    @throws SameUserException se entrambi gli input sono lo stesso username
    @throws UserNotFound se following non è presente nella lista users
    @throws InvalidOperationException se following è già followato da username
    @effects consente a username di followare following

    @Override
    public void followUser(String username, String following)
            throws RemoteException, SameUserException, UserNotFoundException, InvalidOperationException {
        if (username.equals(following)) {
            System.out.println("Error, same user");
            throw new SameUserException();
        }
        if (!existUser(following)) {
            System.out.println("Error, user does not exist");
            throw new UserNotFoundException();
        }
        User follow = users.stream()
                .filter(f ->
                        f.getUsername().equals(following))
                .findFirst().orElse(null);
        User user = users.stream()
                .filter(u ->
                        u.getUsername().equals(username))
                .findFirst().orElse(null);
        user.addFollows(follow);
    }

     */

    /*
    @Override
    public void unfollowUser(String username, String followed)
            throws RemoteException, SameUserException, UserNotFoundException, InvalidOperationException {
        if (username.equals(followed)) {
            System.out.println("Error, same user");
            throw new SameUserException();
        }
        if (!existUser(followed)) {
            System.out.println("Error, user does not exist");
            throw new UserNotFoundException();
        }
        User follow = users.stream()
                .filter(f ->
                        f.getUsername().equals(followed))
                .findFirst().orElse(null);
        User user = users.stream()
                .filter(u ->
                        u.getUsername().equals(username))
                .findFirst().orElse(null);
        user.removeFollows(follow);
    }

     */

    /*
    @Override
    public List<Post> viewBlog(User user) throws RemoteException{
        return user.getBlog().getPosts();
    }

    */

    /*
    @param title, titolo del post
    @param text, contenuto del post
    @param u, user
    @throws RemoteException
    @effects aggiunge un post alla piattaforma

    @Override
    public void createPost(String title, String text, User u) throws RemoteException {
        if (title.length() > Post.MAX_TITLE_LENGHT || text.length() > Post.MAX_TEXT_LENGHT) {
            throw new RemoteException("Title lenght or text lenght are to long");
        }
        u.getBlog().getPosts().add(new Post(u, title, text));
    }

     */

    /*
    @Override
    public List<Post> showFeed(User user) throws RemoteException{
        List<Post> feed = new ArrayList<>();
        user.getFollows().stream().forEach(user1 -> feed.addAll(user1.getBlog().getPosts()));
        return feed.stream().sorted((o1, o2) ->
                (int) (o2.getTimestamp().getTime() - o1.getTimestamp().getTime()))
                .collect(Collectors.toList());
    }
    */

    /*
    private Post showPost(User user, String idPost)
            throws RemoteException, PostNotFoundException {
        Post post = user.getBlog().getPosts().stream()
                .filter(p -> p.getIdPost().equals(idPost))
                .findFirst().orElse(null);
        if (post == null) throw new PostNotFoundException();
        return post;
    }

     */

    /*
    @Override
    public Post showPost(String idPost)
            throws RemoteException, PostNotFoundException {
        for (User u: users) {
            for (Post p: u.getBlog().getPosts()) {
                if (p.getIdPost().equals(idPost)) {
                    return p;
                }
            }
        }
        throw new PostNotFoundException();
    }

     */

    /*
    @Override
    public void deletePost(User user, String idPost)
            throws RemoteException, InvalidOperationException, PostNotFoundException {
        for (User u: users) {
            for (Post p: u.getBlog().getPosts()) {
                if (p.getIdPost().equals(idPost)) {
                    if (!u.equals(user)) throw new InvalidOperationException();
                    u.getBlog().getPosts().remove(p);
                }
            }
        }
        throw new PostNotFoundException();
    }

     */

    /*
    @Override
    public Wallet getWallet(User user) throws RemoteException{
        return user.getWallet();
    }
     */

    /*
    @Override
    public void rewinPost(User user, String idPost) throws RemoteException{
        List<Post> feed = this.showFeed(user);
        Post p = new Post(user,
                feed.stream().filter(post -> post.getIdPost().equals(idPost)).findFirst().orElse(null),
                "Rewin",
                "Rewin post");
        user.getBlog().getPosts().add(p);
    }

     */

    /*
    @param idPost, id del post
    @param vote, voto da assegnare al post
    @param user, utente che vuole assegnare il voto
    @throws RemoteException
    @throws UserNotFoundException se l'utente non viene trovato
    @throws PostNotFoundException se il post non viene trovato
    @throws InvalidOperationException se l'operazione non è corretta
    @effects aggiunge un voto al post

    @Override
    public void ratePost(String idPost, Vote vote, User user)
            throws RemoteException, UserNotFoundException, PostNotFoundException, InvalidOperationException {
        User owner = users.stream()
                .filter(u -> u.getBlog().getPosts().stream()
                        .filter(p -> p.getIdPost().equals(idPost))
                        .findFirst().orElse(null) != null)
                .findFirst().orElse(null);
        if (owner == null) {
            throw new UserNotFoundException();
        }
        Post post = owner.getBlog().getPosts().stream()
                .filter(p -> p.getIdPost().equals(idPost))
                .findFirst().orElse(null);
        if (post == null) {
            throw new PostNotFoundException();
        }
        if (!existPostInUserFeed(post, user)) throw new InvalidOperationException();
        boolean success = post.addVote(user, vote);
        if (!success) throw new InvalidOperationException();
    }

     */

    /*
    @Override
    public void addComment(String idPost, Comment comment, User user)
            throws RemoteException, UserNotFoundException, PostNotFoundException, InvalidOperationException {
        User owner = users.stream()
                .filter(u -> u.getBlog().getPosts().stream()
                        .filter(p -> p.getIdPost().equals(idPost))
                        .findFirst().orElse(null) != null)
                .findFirst().orElse(null);
        if (owner == null) {
            throw new UserNotFoundException();
        }
        Post post = owner.getBlog().getPosts().stream()
                .filter(p -> p.getIdPost().equals(idPost))
                .findFirst().orElse(null);
        if (post == null) {
            throw new PostNotFoundException();
        }
        if(!existPostInUserFeed(post, user)) throw new InvalidOperationException();
        boolean success = post.addComment(user, comment);
        if (!success) throw new InvalidOperationException();
    }

    private boolean existPostInUserFeed (Post post, User user) throws RemoteException{
        return showFeed(user).contains(post);
    }

     */

    /*
    @param user, username dell'utente che voglio cercare nella lista di users
    @return true/false
    @effects restituisce true se lo username fa parte di un utente

    private boolean existUser(String user) {
        boolean result = false;
        for (User u: users) {
            if (u.getUsername().equals(user)) {
                result = true;
                break;
            }
        }
        return result;
    }

     */

    /*
    @param follow, utente che voglio followare
    @return true/false
    @effects restituisce true se la lista dei follow è vuota

    private boolean emptyFollower(User follow) {
        return follow.getFollows() == null;
    }

     */

    /*
    Questo metodo cerca se tra la lista dei follower dell'utente che u vuole followare è presente u
    @param user
    @param follow
    @return true/false
    @effects restituisce true se la lista dei follow contiene user.

    private boolean searchFollower(String user, User follow) {
        for(User f: follow.getFollows()) {
            if (f.getUsername().equals(user)) return true;
        }
        return false;
    }

     */
}
