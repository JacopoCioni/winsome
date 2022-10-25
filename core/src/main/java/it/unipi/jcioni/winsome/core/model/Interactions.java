package it.unipi.jcioni.winsome.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Interactions {

    // Key: Utente in sessione <String> (unicità), Value: lista<String> di utenti che segue
    private transient HashMap<String, List<String>> follows;
    // Key: Utente in sessione <String> (unicità), Value: lista<String> di utenti che lo seguono
    private transient HashMap<String, List<String>> followers;

    public Interactions () {
        this.follows = new HashMap<>();
        this.followers = new HashMap<>();
    }



    public HashMap<String, List<String>> getFollows() {
        return follows;
    }

    public HashMap<String, List<String>> getFollowers() {
        return followers;
    }

    public boolean addFollows(String mainUser, String following) {
        for (String u: follows.keySet()) {
            if (u.equals(mainUser)) {
                for (String f: follows.get(u)) {
                    if (f.equals(following)) {
                        System.err.println("[SERV] - Stai già seguendo questo utente.");
                        return false;
                    }
                }
                follows.get(u).add(following);
                return true;
            }
        }
        // Altrimenti non ho mai aggiunto mainUsers a questa lista e quindi lo aggiungo.
        follows.put(mainUser, new ArrayList<>());
        // Dopo aggiungo l'utente seguito da mainUsers alla lista;
        follows.get(mainUser).add(following);
        return true;
    }

    public void addFollowers(String mainUser, String follower) {
        for (String u: followers.keySet()) {
            if (u.equals(mainUser)) {
                for (String f: follows.get(u)) {
                    if (f.equals(follower)) {
                        return;
                    }
                }
                followers.get(u).add(follower);
                return;
            }
        }
        // Altrimenti non ho mai aggiunto mainUsers a questa lista e quindi lo aggiungo.
        followers.put(mainUser, new ArrayList<>());
        //Dopo aggiungo l'utente che segue mainUser alla lista
        followers.get(mainUser).add(follower);
    }

    public boolean removeFollows(String mainUser, String followed) {
        for (String u: follows.keySet()) {
            if (u.equals(mainUser)) {
                if (!follows.get(u).contains(followed)) {
                    System.err.println("[SERV] - Errore, non stavi seguendo questo utente.");
                    return false;
                }
                follows.get(u).remove(followed);
                return true;
            }
        }
        return false;
    }

    public void removeFollowers(String mainUser, String exFollower) {
        for (String u: followers.keySet()) {
            if (u.equals(mainUser)) {
                if (!followers.get(u).contains(exFollower)) {
                    return;
                }
                followers.get(u).remove(exFollower);
                return;
            }
        }
    }
}
