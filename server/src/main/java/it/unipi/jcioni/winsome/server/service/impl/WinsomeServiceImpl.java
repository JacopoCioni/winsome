package it.unipi.jcioni.winsome.server.service.impl;

import it.unipi.jcioni.winsome.core.model.*;
import it.unipi.jcioni.winsome.core.model.WinsomeData;
import it.unipi.jcioni.winsome.core.service.WinsomeService;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class WinsomeServiceImpl implements WinsomeService {

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
        System.out.println("Registrazione utente: " + username + " START");
        String[] array = tags.split("\\s+");
        if (username == null) {
            System.err.println("Errore: non hai inserito l'username.");
            return false;
        } else if (password == null) {
            System.err.println("Errore: non è stata inserita la password.");
            return false;
        } else if (array.length == 0) {
            System.err.println("Errore: non sono stati inseriti i tag.");
            return false;
        } else if (array.length > 5) {
            System.err.println("Errore: ci sono troppi tag.");
            return false;
        } else if (array.length < 3) {
            System.err.println("Errore: non ci sono abbastanza tag.");
            return false;
        }
        for (User u : winsomeData.getUsers()) {
            if (u.getUsername().equals(username)) {
                System.out.println("Error, username already exists.");
                return false;
            }
        }
        User newUser = new User(username, password,
                Arrays.stream(array).map(Tag::new).collect(Collectors.toList()));
        winsomeData.getUsers().add(newUser);
        System.out.println("Registrazione utente: " + username + " END");
        return true;
    }

    @Override
    public HashMap<String, String> startFollowers(String username, String password) throws RemoteException {
        User user = null;
        for (User u : winsomeData.getUsers()) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                user = u;
                break;
            }
        }
        if (user != null) {
            return user.getInteractions().getFollowers();
        }
        return new HashMap<>();
    }
}