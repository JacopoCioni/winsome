package it.unipi.jcioni.winsome.server;

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
            ConcurrentLinkedDeque<User> users = winsomeData.getUsers();
            String json = WinsomeUtils.gson.toJson(users);
            WinsomeUtils.writeFile(json, "WinsomeServer"+ File.separator+"Users.json");
            System.out.println("[SERV] - Backup eseguito.");
        }
    }
}
