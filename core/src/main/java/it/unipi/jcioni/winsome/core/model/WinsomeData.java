package it.unipi.jcioni.winsome.core.model;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/*
    Classe che detiene le informazioni relative al social winsome. In particolar modo questa classe detiene le
    informazioni relative agli utenti e ai rispettivi post.
 */
public class WinsomeData {
    // Lista degli utenti registrati al servizio
    ConcurrentLinkedDeque<User> users;

    public WinsomeData (ConcurrentLinkedDeque<User> winsomeUsers) {
        if (winsomeUsers == null) {
            users = new ConcurrentLinkedDeque<>();
        } else {
            users = new ConcurrentLinkedDeque<>();
            for (User u: winsomeUsers) {
                users.add(u);
            }
        }
    }

    public void setUsersPosts(ConcurrentLinkedDeque<Post> allPosts) {
        for (User u: users) {
            for (Post p: allPosts) {
                if (u.getUsername().equals(p.getCreator())) {
                    u.getBlog().getPosts().add(p);
                }
            }
        }
    }

    public void setUsersFollows(HashMap<String, String> allFollows) {
        for (User u: users) {
            for (String key: allFollows.keySet()) {
                if (!key.equals(u) && allFollows.get(key).equals(u.getUsername())) {
                    u.getInteractions().getFollows().put(key, allFollows.get(key));
                }
            }
        }
    }

    public void setUsersFollowers(HashMap<String, String> allFollowers) {
        for (User u: users) {
            for (String key: allFollowers.keySet()) {
                if (!key.equals(u.getUsername()) && allFollowers.get(key).equals(u.getUsername())) {
                    u.getInteractions().getFollowers().put(key, allFollowers.get(key));
                }
            }
        }
    }

    public ConcurrentLinkedDeque<User> getUsers() {
        return users;
    }
}
