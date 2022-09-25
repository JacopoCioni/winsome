package it.unipi.jcioni.winsome.core.service.impl;

import it.unipi.jcioni.winsome.core.model.Post;
import it.unipi.jcioni.winsome.core.model.Tag;
import it.unipi.jcioni.winsome.core.model.User;
import it.unipi.jcioni.winsome.core.service.WinsomeService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WinsomeServiceImpl implements WinsomeService {
    private List<User> users = new ArrayList<>();

    @Override
    public boolean register(String username, String password, ArrayList<Tag> tags) {
        System.out.println("User registration: " + username + " " + tags + "...");
        if(tags.size() > 5) {
            System.out.println("Error, there are to many tags.");
            return false;
        }
        for(User u: users) {
            if(u.getUsername().equals(username)) {
                System.out.println("Error, username already exists.");
                return false;
            }
        }
        if(password == null) return false;
        User newUser = new User(null, null, null, username, password, new ArrayList<User>(), tags);
        users.add(newUser);
        System.out.println("Success");
        return true;
    }

    @Override
    public boolean login(String username, String password) {
        return users.stream()
                .filter(user -> user.getUsername().equals(username) && user.getPassword().equals(password))
                .collect(Collectors.toList()).size() == 1;
    }

    @Override
    public void createPost(String title, String text, User u) throws RemoteException {
        if (title.length() > Post.MAX_TITLE_LENGHT || text.length() > Post.MAX_TEXT_LENGHT) {
            throw new RemoteException("Title lenght or text lenght are to long");
        }
        u.getBlog().getPosts().add(new Post(u, title, text));
    }

    public void followUser(String username, String following) {
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
