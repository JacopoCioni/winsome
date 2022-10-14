package it.unipi.jcioni.winsome.core.service;

import it.unipi.jcioni.winsome.core.model.User;

import java.util.concurrent.ConcurrentLinkedDeque;

/*
    Classe che detiene le informazioni relative al social winsome. In particolar modo questa classe detiene le
    informazioni relative agli utenti e ai rispettivi post.
 */
public class WinsomeData {
    // Lista degli utenti registrati al servizio
    ConcurrentLinkedDeque<User> users;

    public WinsomeData () {
        users = new ConcurrentLinkedDeque<>();
    }

    public ConcurrentLinkedDeque<User> getUsers() {
        return users;
    }
}
