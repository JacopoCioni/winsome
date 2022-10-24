package it.unipi.jcioni.winsome.server;

import it.unipi.jcioni.winsome.core.model.Post;
import it.unipi.jcioni.winsome.core.model.User;
import it.unipi.jcioni.winsome.core.model.WinsomeData;

import java.io.File;
import java.io.IOException;
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
            // Aggiungo tutti i post di tutti gli utenti a questa lista
            for (User u: users) {
                if (u.getBlog() != null) {
                    allPosts.addAll(u.getBlog().getPosts());
                }
            }
            //Scrivo gli utenti su file
            String jsonUsers = WinsomeUtils.gson.toJson(users);
            WinsomeUtils.writeFile(jsonUsers, "WinsomeServer"+ File.separator+"Users.json");
            //Scrivo i post su file
            String jsonPosts = WinsomeUtils.gson.toJson(allPosts);
            WinsomeUtils.writeFile(jsonPosts, "WinsomeServer"+ File.separator+"Posts.json");

            System.out.println("[SERV] - Backup eseguito.");
        }
    }
}
