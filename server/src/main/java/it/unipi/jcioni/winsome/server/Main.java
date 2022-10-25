package it.unipi.jcioni.winsome.server;

import com.google.gson.reflect.TypeToken;
import it.unipi.jcioni.winsome.core.model.*;
import it.unipi.jcioni.winsome.core.service.WinsomeService;
import it.unipi.jcioni.winsome.server.service.impl.WinsomeServiceImpl;
import it.unipi.jcioni.winsome.core.service.WinsomeCallback;
import it.unipi.jcioni.winsome.server.service.impl.WinsomeCallbackImpl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.*;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    private static WinsomeData WINSOME_DATA;
    public static ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    public static ConcurrentLinkedDeque<Socket> sockets = new ConcurrentLinkedDeque<>();
    private static int serverPort;
    private static int rmiPort;
    private static int rmiCallbackPort;
    private static String rmiServerRegistryName;
    private static String rmiCallbackClientRegistryName;

    public static WinsomeConfig winsomeConfig;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket clientSocket;
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        File serverFolder = new File("WinsomeServer");
        if (!serverFolder.exists()) {
            serverFolder.mkdir();
        }

        try {
            winsomeConfig = new WinsomeConfig(true);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Controllo che i file siano sul disco
        try {
            File userFile = new File("WinsomeServer"+ File.separator+"Users.json");
            if (!userFile.exists()) {
                userFile.createNewFile();
            }
            File postFile = new File("WinsomeServer"+ File.separator+"Posts.json");
            if (!postFile.exists()) {
                postFile.createNewFile();
            }
            File followsFile = new File("WinsomeServer"+ File.separator+"Follows.json");
            if (!followsFile.exists()) {
                followsFile.createNewFile();
            }
            File followersFile = new File("WinsomeServer"+ File.separator+"Followers.json");
            if (!followersFile.exists()) {
                followersFile.createNewFile();
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        // Leggo i dati che ci siano oppure no
        try {
            String winsomeUserJson = WinsomeUtils.readFile(new File("WinsomeServer"+ File.separator+"Users.json"));
            Type typeUserObject = new TypeToken<ConcurrentLinkedDeque<User>>(){}.getType();
            ConcurrentLinkedDeque<User> winsomeUsers = WinsomeUtils.gson.fromJson(winsomeUserJson, typeUserObject);

            String winsomePostJson = WinsomeUtils.readFile(new File("WinsomeServer"+ File.separator+"Posts.json"));
            Type typePostObject = new TypeToken<ConcurrentLinkedDeque<Post>>(){}.getType();
            ConcurrentLinkedDeque<Post> winsomePosts = WinsomeUtils.gson.fromJson(winsomePostJson, typePostObject);

            String winsomeFollowsJson = WinsomeUtils.readFile(new File("WinsomeServer"+ File.separator+"Follows.json"));
            Type typeFollowsObject = new TypeToken<HashMap<String, List<String>>>(){}.getType();
            HashMap<String, List<String>> winsomeFollows = WinsomeUtils.gson.fromJson(winsomeFollowsJson, typeFollowsObject);

            String winsomeFollowersJson = WinsomeUtils.readFile(new File("WinsomeServer"+ File.separator+"Followers.json"));
            Type typeFollowersObject = new TypeToken<HashMap<String, List<String>>>(){}.getType();
            HashMap<String, List<String>> winsomeFollowers = WinsomeUtils.gson.fromJson(winsomeFollowersJson, typeFollowersObject);

            try {
                //Inizializzazione delle variabili dal file di properties
                serverPort = Integer.parseInt(winsomeConfig.getProperties("SERVER_TCP_PORT"));
                rmiPort = Integer.parseInt(winsomeConfig.getProperties("SERVER_RMI_PORT"));
                rmiCallbackPort = Integer.parseInt(winsomeConfig.getProperties("RMI_CALLBACK_CLIENT_PORT"));
                rmiServerRegistryName = winsomeConfig.getProperties("RMI_SERVER_REGISTRY_NAME");
                rmiCallbackClientRegistryName = winsomeConfig.getProperties("RMI_CALLBACK_CLIENT_REGISTRY_NAME");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Inizializzazione del social Winsome
            // Caricamento di tutti gli Utenti - costruzione di WINSOME_DATA
            WINSOME_DATA = new WinsomeData(winsomeUsers);
            // Caricamento di tutti i Post degli utenti
            WINSOME_DATA.setUsersPosts(winsomePosts);
            // Caricamento di tutti i Follows degli utenti
            WINSOME_DATA.setUsersFollows(winsomeFollows);
            // Caricamento di tutti i Followers degli utenti
            WINSOME_DATA.setUsersFollowers(winsomeFollowers);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Avvio WinsomeRewards
        WinsomeRewards winsomeRewards = new WinsomeRewards(WINSOME_DATA, winsomeConfig);
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
            r.rebind(rmiServerRegistryName, stub);
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
            r.rebind(rmiCallbackClientRegistryName, stub);
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
}