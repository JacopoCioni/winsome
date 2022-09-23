package it.unipi.jcioni.winsome.core.service.impl;

import it.unipi.jcioni.winsome.core.model.Post;
import it.unipi.jcioni.winsome.core.model.User;
import it.unipi.jcioni.winsome.core.service.WinsomeService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WinsomeServiceImpl implements WinsomeService {
    private List<User> users = new ArrayList<>();

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
}
