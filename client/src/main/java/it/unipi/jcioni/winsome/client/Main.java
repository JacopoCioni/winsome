package it.unipi.jcioni.winsome.client;

import it.unipi.jcioni.winsome.core.model.WinsomeConfig;
import it.unipi.jcioni.winsome.core.service.WinsomeNotifyEvent;
import it.unipi.jcioni.winsome.client.service.impl.WinsomeNotifyEventImpl;
import it.unipi.jcioni.winsome.core.service.WinsomeService;
import it.unipi.jcioni.winsome.core.service.WinsomeCallback;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;

import static it.unipi.jcioni.winsome.core.service.WinsomeService.*;

public class Main {

    public static HashMap<String, List<String>> followers = new HashMap<>();
    public static WinsomeConfig winsomeConfig;
    private static int serverPort;
    private static int rmiPort;
    private static int rmiCallbackPort;
    private static String rmiServerRegistryName;
    private static String rmiCallbackClientRegistryName;
    private static String serverAddress;
    private static String multicastAddress;
    private static int multicastPort;
    public static void main(String[] args) {

        try {
            winsomeConfig = new WinsomeConfig(false);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            //Inizializzazione delle variabili dal file di properties
            serverPort = Integer.parseInt(winsomeConfig.getProperties("SERVER_TCP_PORT"));
            rmiPort = Integer.parseInt(winsomeConfig.getProperties("SERVER_RMI_PORT"));
            rmiCallbackPort = Integer.parseInt(winsomeConfig.getProperties("RMI_CALLBACK_CLIENT_PORT"));
            rmiServerRegistryName = winsomeConfig.getProperties("RMI_SERVER_REGISTRY_NAME");
            rmiCallbackClientRegistryName = winsomeConfig.getProperties("RMI_CALLBACK_CLIENT_REGISTRY_NAME");
            serverAddress = winsomeConfig.getProperties("SERVER_ADDRESS");
            multicastPort = Integer.parseInt(winsomeConfig.getProperties("MULTICAST_PORT"));
            multicastAddress = winsomeConfig.getProperties("MULTICAST_ADDRESS");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Socket socket;
        String username = null;
        WinsomeCallback winsomeCallback = null;
        WinsomeNotifyEvent callbackStub;
        WinsomeNotifyEvent callbackObj = null;
        WinsomeWalletUpdate winsomeWalletUpdate;

        Scanner read = new Scanner(System.in);

        winsomeWalletUpdate = new WinsomeWalletUpdate(multicastAddress, multicastPort);
        Thread walletExecutor = new Thread(winsomeWalletUpdate);
        walletExecutor.start();

        while (true) {
            try {
                socket = new Socket(serverAddress, serverPort);
            } catch (IOException e) {
                System.err.println("[CLI] - Impossibile connettersi al server, riprovare più tardi.");
                break;
            }

            System.out.println("[CLI] - Connessione TCP stabilita.");

            Registry reg;
            WinsomeService stub;
            try {
                reg = LocateRegistry.getRegistry(serverAddress, rmiPort);
                stub = (WinsomeService) reg.lookup(rmiServerRegistryName);
            } catch (AccessException e) {
                System.err.println("[CLI] - Il registro non è raggiungibile.");
                e.printStackTrace();
                return;
            } catch (NotBoundException | RemoteException e) {
                System.err.println("Errore [RMI]: "+e.getLocalizedMessage());
                e.printStackTrace();
                return;
            }

            System.out.println("[CLI] - Sei collegato al server!");

            try {
                // Input stream di bytes
                InputStream inputStream = socket.getInputStream();
                BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
                // Output stream di bytes
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter output = new PrintWriter(outputStream, true);


                String command;
                String request;
                while (true) {
                    try {
                        command = read.nextLine();
                    } catch (NoSuchElementException e) {
                        continue;
                    }
                    request = command;
                    command = command.trim();
                    String[] arguments = command.split("\\s+");
                    command = arguments[0];
                    arguments = Arrays.stream(arguments)
                            .collect(Collectors.toList())
                                    .subList(1, arguments.length)
                                            .toArray(new String[arguments.length - 1]);
                    switch (command) {
                        case "help": {
                            System.out.println("[CLI] - LISTA DEI COMANDI DISPONIBILI:");
                            System.out.println(" |-register <username> <password> <tags>  * Effettua la registrazione a Winsome.");
                            System.out.println(" |-login <username> <password>            * Effettua il login a Winsome.");
                            System.out.println(" |-logout                                 * Effettua la logout da Winsome.");
                            System.out.println(" |-listfollowers                          * Mostra la lista degli utenti che ti seguono.");
                            System.out.println(" |-listusers                              * Mostra la lista degli utenti con almeno un tag in comune.");
                            System.out.println(" |-listfollowing                          * Mostra la degli utenti che si segue.");
                            System.out.println(" |-follow <username>                      * Permette di seguire un utente.");
                            System.out.println(" |-unfollow <username>                    * Permette di smettere di seguire un utente.");
                            System.out.println(" |-blog                                   * Mostra i post nel proprio blog.");
                            System.out.println(" |-post <titolo> <contenuto>              * Permette di pubblicare un post.");
                            System.out.println(" |-showfeed                               * Mostra i post nel proprio feed.");
                            System.out.println(" |-showpost <idPost>                      * Mostra un post specifico.");
                            System.out.println(" |-rewin <idPost>                         * Permette di fare il rewin di un post.");
                            System.out.println(" |-rate <idPost> <+1/-1>                  * Permette di valutare un post.");
                            System.out.println(" |-comment <idPost> <commento>            * Permette di commentare un post.");
                            System.out.println(" |-wallet                                 * Mostra il proprio portafoglio.");
                            System.out.println(" |-walletbtc                              * Mostra il bilancio convertito in Bitcoin.");
                            System.out.println(" |-exit                                   * Permette di uscire dalla piattaforma Winsome.");
                            break;
                        }
                        case "register": {
                            try {
                                if (username != null) {
                                    System.out.println("[SERV] - Errore: sei attualmente loggato.");
                                    break;
                                }
                                if (arguments.length < 3) throw new ArrayIndexOutOfBoundsException();
                                String tags = "";
                                for (int i = 2; i < arguments.length; i++) {
                                    tags += " " + arguments[i];
                                }
                                tags = tags.trim();
                                boolean response = stub.register(arguments[0], arguments[1], tags);
                                if (response) {
                                    System.out.println("[SERV] - Registrazione dell'utente " + arguments[0] + " effettuata con successo.");
                                } else {
                                    System.err.println("[SERV] - Registrazione dell'utente " + arguments[0] + " fallita.");
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                System.err.println("[SERV] - Richiesti almeno 3 argomenti, massimo 7 : <username> <password> [tags]");
                            } catch (RemoteException ignored) {
                            }
                            break;
                        }
                        case "login": {
                            invia(output, request);
                            String response = ricevi(input);
                            if (response.equalsIgnoreCase("login ok")) {
                                username = arguments[0];
                                // Registrazione della callback per l'aggiornamento della listafollower
                                try {
                                    Registry callbackReg = LocateRegistry.getRegistry(serverAddress, rmiCallbackPort);
                                    winsomeCallback = (WinsomeCallback) callbackReg.lookup(rmiCallbackClientRegistryName);
                                    callbackObj = new WinsomeNotifyEventImpl();
                                    callbackStub = (WinsomeNotifyEvent) UnicastRemoteObject.exportObject(callbackObj, 0);
                                } catch (Exception e) {
                                    System.err.println("[RMICallback] - Errore: "+e.getLocalizedMessage());
                                    break;
                                }
                                try {
                                    winsomeCallback.registerForCallback(username, callbackStub);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }

                                // Inizializzazione lista dei followers
                                followers = stub.startFollowers(username, arguments[1]);
                                System.out.println("[SERV] - Stato login: " + response);
                            } else {
                                // ci sarà la risposta del server per capire come mai non è andato a buon fine
                                System.err.println("Errore: " + response);
                            }
                            break;
                        }
                        case "logout": {
                            invia(output, request);
                            String response = ricevi(input);
                            // Se sono connesso alla callback e la richiesta è andata a buon fine allora esco
                            if (winsomeCallback!= null && response.equalsIgnoreCase("logout ok")) {
                                System.out.println("[SERV] - Stato logout: " + response);
                                winsomeCallback.unregisterForCallback(username);
                                UnicastRemoteObject.unexportObject(callbackObj, false);
                                callbackObj = null;
                                username = null;
                                break;
                            }
                            System.err.println("Errore: " + response);
                            break;
                        }
                        case "listfollowers": {
                            // Faccio i controlli dal momento in cui è gestita lato client
                            if (arguments.length != 0) {
                                System.err.println("[CLI] - Errore, utilizzare listfollowers.");
                                break;
                            }
                            if(username == null) {
                                System.err.println("[CLI] - Errore, non è stato effettuato il login.");
                                break;
                            }
                            if (followers.size() == 0) {
                                System.out.println("[CLI] - Non hai nessun follower");
                                break;
                            }
                            // Superati i controlli posto stampare la lista dei followers.
                            System.out.println("[CLI] - Lista dei followers: ");
                            for (String s: followers.keySet()) {
                                if (s.equals(username)) {
                                    for (String f: followers.get(s)) {
                                        System.out.println("|-"+f);
                                    }
                                }
                            }
                            break;
                        }
                        case "exit": {
                            if (arguments.length != 0) {
                                System.err.println("[CLI] - Errore, utilizzare exit.");
                                break;
                            }
                            // Chiusura del client
                            socket.close();
                            output.close();
                            input.close();
                            try {
                                if (winsomeCallback != null && username != null) {
                                    // Rimuovo il mio interesse solo se sono loggato
                                    winsomeCallback.unregisterForCallback(username);
                                }
                                if (callbackObj != null) {
                                    UnicastRemoteObject.unexportObject(callbackObj, false);
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            read.close();
                            // Gestione dello wallet update e sua chiusura
                            winsomeWalletUpdate.stop();
                            System.out.println("[CLI] - Terminazione del servizio avvenuta con successo.");
                            System.exit(0);
                        }
                        case "listusers":
                        case "listfollowing":
                        case "follow":
                        case "unfollow":
                        case "blog":
                        case "post":
                        case "showfeed":
                        case "showpost":
                        case "rewin":
                        case "rate":
                        case "comment":
                        case "wallet":
                        case "walletbtc": {
                            invia(output, request);
                            System.out.println(ricevi(input));
                            break;
                        }
                        default: {
                            invia(output, request);
                            System.out.println(ricevi(input));
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("[CLI] - Errore, connessione al server perduta.");
            }
        }

    }

    private static void invia (PrintWriter output, String send) {
        output.println(send);
        output.flush();
    }

    private static String ricevi (BufferedReader input) throws IOException {
        String text = input.readLine();
        if (text == null) throw new IOException();
        text = text.replace('$', '\n');
        return text;
    }
}