package it.unipi.jcioni.winsome.server;

import it.unipi.jcioni.winsome.core.service.WinsomeData;
import it.unipi.jcioni.winsome.core.service.WinsomeService;
import it.unipi.jcioni.winsome.core.service.impl.WinsomeServiceImpl;

import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static it.unipi.jcioni.winsome.core.service.WinsomeService.*;

public class Main {
    private static WinsomeData WINSOME_DATA;
    public static void main(String[] args) {
        int serverPort = args.length > 0 && args[0] != null && args[0].length() > 0
                ? Integer.parseInt(args[0]) : SERVER_TCP_PORT;
        int rmiPort = args.length > 0 && args[1] != null && args[1].length() > 0
                ? Integer.parseInt(args[1]) : SERVER_RMI_PORT;
        ServerSocket serverSocket = null;
        Socket clientSocket;
        ConcurrentLinkedDeque<Socket> sockets = new ConcurrentLinkedDeque<>();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        // Inizializzazione del social Winsome
        WINSOME_DATA = new WinsomeData();

        try {
            /* Creazione di un'istanza dell'oggetto WinsomeService */
            WinsomeServiceImpl winsomeServ = new WinsomeServiceImpl(WINSOME_DATA);
            /* Esportazione dell'Oggetto */
            WinsomeService stub = (WinsomeService) UnicastRemoteObject.exportObject(winsomeServ, 0);
            /* Creazione di un registry sulla porta rmiPort */
            LocateRegistry.createRegistry(rmiPort);
            /*Pubblicazione dello stub nel registry */
            Registry r = LocateRegistry.getRegistry(rmiPort);
            r.rebind(RMI_SERVER_REGISTRY_NAME, stub);
            System.out.println("RMI pronto sulla porta " + rmiPort);
        }
        /* If any communication failures occur... */
        catch (RemoteException e) {
            System.out.println("Communication error " + e.getMessage());
            e.printStackTrace();
            return;
        }
        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Server pronto sulla porta " + serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                //bloccante fino a quando non avviene una connessione
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

        //Chiusura socket dei client
        for (Socket s: sockets) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Chiusura socket server
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Chiusura del pool
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3000, TimeUnit.MILLISECONDS))
                executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            System.out.println("Il pool è stato chiuso forzatamente.");
        }
        System.out.println("Il pool è stato chiuso.");
        System.out.println("Server closed.");
    }
}