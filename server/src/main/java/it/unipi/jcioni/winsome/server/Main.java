package it.unipi.jcioni.winsome.server;

import it.unipi.jcioni.winsome.core.model.*;
import it.unipi.jcioni.winsome.core.service.WinsomeService;
import it.unipi.jcioni.winsome.server.service.impl.WinsomeServiceImpl;
import it.unipi.jcioni.winsome.core.service.WinsomeCallback;
import it.unipi.jcioni.winsome.server.service.impl.WinsomeCallbackImpl;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.*;

import static it.unipi.jcioni.winsome.core.service.WinsomeService.*;

public class Main {
    private static WinsomeData WINSOME_DATA;
    public static ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    public static ConcurrentLinkedDeque<Socket> sockets = new ConcurrentLinkedDeque<>();
    private static int serverPort = SERVER_TCP_PORT;
    private static int rmiPort = SERVER_RMI_PORT;
    private static int rmiCallbackPort = RMI_CALLBACK_CLIENT_PORT;


    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket clientSocket;
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        // Controllo che i file siano sul disco
        try {
            File serverFolder = new File("WinsomeServer");
            if (!serverFolder.exists()) {
                serverFolder.mkdir();
            }
            File userFile = new File("WinsomeServer"+ File.separator+"Users.json");
            if (!userFile.exists()) {
                userFile.createNewFile();
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        // Leggo i dati che ci siano oppure no
        try {
            String winsomeJson = WinsomeUtils.readFile(new File("WinsomeServer"+ File.separator+"Users.json"));
            User[] winsomeUsers = WinsomeUtils.gson.fromJson(winsomeJson, User[].class);
            // Inizializzazione del social Winsome
            WINSOME_DATA = new WinsomeData(winsomeUsers);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Avvio WinsomeRewards
        WinsomeRewards winsomeRewards = new WinsomeRewards(WINSOME_DATA);
        Thread rewardsExecutor = new Thread(winsomeRewards);
        rewardsExecutor.start();

        /* Creazione di un'istanza dell'oggetto WinsomeService */
        WinsomeServiceImpl winsomeServ = new WinsomeServiceImpl(WINSOME_DATA);
        // Inizializzazione RMI register
        try {
            /* Esportazione dell'Oggetto */
            WinsomeService stub = (WinsomeService) UnicastRemoteObject.exportObject(winsomeServ, 0);
            /* Creazione di un registry sulla porta rmiPort */
            LocateRegistry.createRegistry(rmiPort);
            /*Pubblicazione dello stub nel registry */
            Registry r = LocateRegistry.getRegistry(rmiPort);
            r.rebind(RMI_SERVER_REGISTRY_NAME, stub);
            System.out.println("[RMI] - pronto sulla porta: " + rmiPort);
        }
        /* If any communication failures occur... */
        catch (RemoteException e) {
            System.err.println("[RMI] - Errore di comunicazione: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        /* Creazione di un'istanza dell'oggetto WinsomeCallback */
        WinsomeCallbackImpl winsomeCallback = new WinsomeCallbackImpl();
        // Inizializzazione RMI callback
        try {
            /* Esportazione dell'Oggetto */
            WinsomeCallback stub = (WinsomeCallback) UnicastRemoteObject.exportObject(winsomeCallback, 0);
            /* Creazione di un registry sulla porta rmiCallbackPort */
            LocateRegistry.createRegistry(rmiCallbackPort);
            /*Pubblicazione dello stub nel registry */
            Registry r = LocateRegistry.getRegistry(rmiCallbackPort);
            r.rebind(RMI_CALLBACK_CLIENT_REGISTRY_NAME, stub);
            System.out.println("[RMICallback] - pronto sulla porta: "+rmiCallbackPort);
        } catch (RemoteException e) {
            System.err.println("[RMICallback] - Errore di comunicazione: "+e.getMessage());
            e.printStackTrace();
            return;
        }

        //Avvio Thread di backup
        new WinsomeSave(WINSOME_DATA);

        // Apertura socket del server
        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("[SERV] - Pronto sulla porta: " + serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                // bloccante fino a quando non avviene una connessione
                clientSocket = serverSocket.accept();
                sockets.add(clientSocket);
                executor.submit(new Handler(clientSocket, WINSOME_DATA));
            } catch (SocketException e) {
                e.printStackTrace();
                break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Chiusura socket dei client
        for (Socket s: sockets) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Chiusura socket server
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Chiusura WinsomeRewards
        winsomeRewards.stop();
        rewardsExecutor.interrupt();

        // Chiusura del pool
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3000, TimeUnit.MILLISECONDS))
                executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            System.out.println("[SERV] - Il pool è stato chiuso forzatamente.");
        }
        System.out.println("[SERV] - Il pool è stato chiuso.");

        // Chiusura del servizio RMI
        try {
            UnicastRemoteObject.unexportObject(winsomeServ, false);
            UnicastRemoteObject.unexportObject(winsomeCallback, false);
        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        }
        System.out.println("[RMI] - servizio chiuso.");
        System.out.println("[SERV] - Terminato.");
    }

    /* TODO: Da rimuovere
    public static void serialize() {
        // Questa è una prova della serializzazione, di fatto non deve funzionare così
        ConcurrentLinkedDeque<User> utenti = new ConcurrentLinkedDeque<>();

        List<Tag> tags  = new ArrayList<>();
        tags.add(new Tag("Tennis"));
        tags.add(new Tag("Calcio"));
        tags.add(new Tag("Nuoto"));

        ConcurrentLinkedDeque<Post> posts = new ConcurrentLinkedDeque<>();
        posts.add(new Post("Jacopo", "Sedia", "Questa è una sedia."));
        posts.add(new Post("Jacopo", "Sedia", "Questa è una sedia."));
        for (Post p: posts) {
            p.addVote("Samuele", Vote.UP);
            p.addComment(new Comment("Samuele", "Bella sedia!"));
        }

        utenti.add(new User("Samuele", "prova", tags));
        utenti.add(new User("Jacopo", "ciao", tags));
        for (User u: utenti) {
            // u.getBlog().setPosts(posts);
        }

        String json = WinsomeUtils.gson.toJson(utenti);
        System.out.println("File JSon: " + json);
        try {
            WinsomeUtils.writeFile(json, "WinsomeServer"+ File.separator+"Users.json");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

     */

}