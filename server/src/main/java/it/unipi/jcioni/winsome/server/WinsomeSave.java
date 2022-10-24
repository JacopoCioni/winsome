package it.unipi.jcioni.winsome.server;

import it.unipi.jcioni.winsome.core.model.Post;
import it.unipi.jcioni.winsome.core.model.User;
import it.unipi.jcioni.winsome.core.model.WinsomeData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WinsomeSave implements Runnable{

    private WinsomeData winsomeData;

    public WinsomeSave(WinsomeData winsomeData) {
        this.winsomeData = winsomeData;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            saveData();
        } catch (InterruptedException | IOException e) {
            System.err.println("[SERV] - Backup fallito.");
            e.printStackTrace();
        }
    }

    private void saveData() throws InterruptedException, IOException {
        while (true) {
            // Autosave ogni minuto
            Thread.sleep(60000L);

            // Lista di utenti
            ConcurrentLinkedDeque<User> users = winsomeData.getUsers();
            // Lista di TUTTI i post della piattaforma
            ConcurrentLinkedDeque<Post> allPosts = new ConcurrentLinkedDeque<>();
            // HashMap di TUTTI i follows della piattaforma
            HashMap<String, String> allFollows = new HashMap<>();
            // HashMap di TUTTI i followers della piattaforma
            HashMap<String, String> allFollowers = new HashMap<>();

            // Scorro tutti gli utenti della piattaforma
            for (User u: users) {
                // Aggiungo tutti i post di tutti gli utenti a questa lista
                if (u.getBlog() != null) {
                    allPosts.addAll(u.getBlog().getPosts());
                }
                if (u.getInteractions() != null) {
                    // Aggiungo tutti i follows di tutti gli utenti della piattaforma
                    allFollows.putAll(u.getInteractions().getFollows());
                    // Aggiungo tutti i followers di tutti gli utenti della piattaforma
                    allFollowers.putAll(u.getInteractions().getFollowers());
                }
            }

            // Scrivo gli utenti su file
            String jsonUsers = WinsomeUtils.gson.toJson(users);
            WinsomeUtils.writeFile(jsonUsers, "WinsomeServer"+ File.separator+"Users.json");
            // Scrivo i post su file
            String jsonPosts = WinsomeUtils.gson.toJson(allPosts);
            WinsomeUtils.writeFile(jsonPosts, "WinsomeServer"+ File.separator+"Posts.json");
            // Scrivo i follows su file
            String jsonFollows = WinsomeUtils.gson.toJson(allFollows);
            WinsomeUtils.writeFile(jsonFollows,"WinsomeServer"+ File.separator+"Follows.json");
            // Scrivo tutti i followers su file
            String jsonFollowers = WinsomeUtils.gson.toJson(allFollowers);
            WinsomeUtils.writeFile(jsonFollowers,"WinsomeServer"+ File.separator+"Followers.json");

            System.out.println("[SERV] - Backup eseguito.");

        }
    }
}
