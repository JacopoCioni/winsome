package it.unipi.jcioni.winsome.core.service.impl;

import it.unipi.jcioni.winsome.core.exception.LoginException;
import it.unipi.jcioni.winsome.core.model.Post;
import it.unipi.jcioni.winsome.core.model.Tag;
import it.unipi.jcioni.winsome.core.model.User;
import it.unipi.jcioni.winsome.core.service.WinsomeService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WinsomeServiceImpl implements WinsomeService {
    private List<User> users = new ArrayList<>();

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
        for (User u: users) {
            if (u.getUsername().equals(username)) {
                System.out.println("Error, username already exists.");
                return false;
            }
        }
        User newUser = new User(username, password,
                Arrays.stream(array).map(Tag::new).collect(Collectors.toList()));
        users.add(newUser);
        System.out.println("User registration " + username + " END");
        return true;
    }

    /*
    @param username, user username
    @param password, user password
    @return true/false
    @throws NullPointerException se l'username o la password è errata
    @throws LoginException se l'utente è gia loggato
    @effects permette la login di un utente alla piattaforma winsome
     */
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

    /*
    @param username, user username
    @return true/false
    @throws NullPointerException se l'utente non viene trovato
    @effect permette la logout di un utente dalla piattaforma winsome
     */
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
        }
        System.out.println("User logout " + username + " END");
        return true;
    }

    /*
    @param user, user
    @return list of users
    @effects viene restituita la lista degli utenti registrati al servizio che hanno almeno un tag in comune
     */
    @Override
    public List<User> listUsers(User user) throws RemoteException {
        return users.stream().filter(u -> {
            for (int i = 0; i < user.getTags().size(); i++) {
                for (int j = 0; j < u.getTags().size(); j++) {
                    if (user.getTags().get(i).equals(u.getTags().get(j))) {
                        return !user.getUsername().equals(u.getUsername());
                    }
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    /*
    @param title, titolo del post
    @param text, contenuto del post
    @param u, user
    @throws RemoteException
    @effects aggiunge un post alla piattaforma
     */
    @Override
    public void createPost(String title, String text, User u) throws RemoteException {
        if (title.length() > Post.MAX_TITLE_LENGHT || text.length() > Post.MAX_TEXT_LENGHT) {
            throw new RemoteException("Title lenght or text lenght are to long");
        }
        u.getBlog().getPosts().add(new Post(u, title, text));
    }

    public void followUser(String username, String following) throws RemoteException {
        boolean result = false;
        if(username.equals(following)) {
            System.out.println("Error, same user.");
            return;
        }
        for(User u: users) {
            if (u.getUsername().equals(following)) {
                result = true;
                break;
            }
        }
        if(!result) {
            System.out.println("Error, user does not exist.");
            return;
        }

        //Aggiungere follow, da rivedere


    }
}
