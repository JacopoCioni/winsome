package it.unipi.jcioni.winsome.core.model;

import java.util.HashMap;
import java.util.List;
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

    public void setUsersFollows(HashMap<String, List<String>> allFollows) {
        for (User u: users) {
            for (String s: allFollows.keySet()) {
                u.getInteractions().getFollows().put(s, allFollows.get(s));
            }
        }
    }

    public void setUsersFollowers(HashMap<String, List<String>> allFollowers) {
        for (User u: users) {
            for (String s: allFollowers.keySet()) {
                u.getInteractions().getFollowers().put(s, allFollowers.get(s));
            }
        }
    }

    public ConcurrentLinkedDeque<User> getUsers() {
        return users;
    }
}
