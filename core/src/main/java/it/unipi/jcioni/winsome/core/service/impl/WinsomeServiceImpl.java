package it.unipi.jcioni.winsome.core.service.impl;

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

    @Override
    public boolean register(String username, String password, String tags) {
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

    @Override
    public boolean login(String username, String password) {
        return users.stream()
                .filter(user ->
                        user.getUsername().equals(username) && user.getPassword().equals(password))
                .toList().size() == 1;
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
