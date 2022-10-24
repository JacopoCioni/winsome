package it.unipi.jcioni.winsome.core.model;

import java.util.HashMap;

public class Interactions {

    // Key: Utente seguito <String> (unicità), Value: utente che segue <String>
    private transient HashMap<String, String> follows;
    // Key: Utente che mi segue <String> (unicità), Value: utente seguito <String>
    private transient HashMap<String, String> followers;

    public Interactions () {
        this.follows = new HashMap<>();
        this.followers = new HashMap<>();
    }

    public HashMap<String, String> getFollows() {
        return follows;
    }

    public HashMap<String, String> getFollowers() {
        return followers;
    }

    public boolean addFollows(String mainUser, String following) {
        for (String u: follows.keySet()) {
            if (u.equals(following)) {
                System.err.println("[SERV] - Stai già seguendo questo utente.");
                return false;
            }
        }
        follows.put(following, mainUser);
        return true;
    }

    public void addFollowers(String mainUser, String follower) {
        for (String s: followers.keySet()) {
            if (s.equals(follower)) {
                return;
            }
        }
        followers.put(follower, mainUser);
    }

    public boolean removeFollows(String mainUser, String followed) {
        boolean result = follows.remove(followed, mainUser);
        if (!result) {
            System.err.println("[SERV] - Errore, non stavi seguendo questo utente.");
        }
        return result;
    }

    public void removeFollowers(String exFollower) {
        try{
            followers.remove(exFollower);
        } catch (Exception e) {
            System.err.println("[SERV] - Errore nella rimozione del follower.");
        }
    }
}
