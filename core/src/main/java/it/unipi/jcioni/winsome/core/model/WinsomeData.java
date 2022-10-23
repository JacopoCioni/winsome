package it.unipi.jcioni.winsome.core.model;

import it.unipi.jcioni.winsome.core.model.User;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/*
    Classe che detiene le informazioni relative al social winsome. In particolar modo questa classe detiene le
    informazioni relative agli utenti e ai rispettivi post.
 */
public class WinsomeData {
    // Lista degli utenti registrati al servizio
    ConcurrentLinkedDeque<User> users;

    public WinsomeData (User[] winsomeUsers) {
        if (winsomeUsers == null) {
            users = new ConcurrentLinkedDeque<>();
        } else {
            users = new ConcurrentLinkedDeque<>();
            for (User u: winsomeUsers) {
                users.add(u);
            }
        }
    }

    public ConcurrentLinkedDeque<User> getUsers() {
        return users;
    }
}
