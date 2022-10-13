package it.unipi.jcioni.winsome.client;

import it.unipi.jcioni.winsome.core.service.WinsomeService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Scanner read = new Scanner(System.in);
        Socket socket;
        String username = null;
        while (true) {
            try {
                socket = new Socket(WinsomeService.SERVER_ADDRESS, WinsomeService.SERVER_TCP_PORT);
            } catch (IOException e) {
                System.err.println("Impossibile connettersi al server, riprovare più tardi.");
                break;
            }

            System.out.println("Connessione TCP stabilita.");

            Registry reg;
            WinsomeService stub;
            try {
                reg = LocateRegistry.getRegistry(WinsomeService.SERVER_ADDRESS, WinsomeService.SERVER_RMI_PORT);
                stub = (WinsomeService) reg.lookup(WinsomeService.RMI_SERVER_REGISTRY_NAME);
            } catch (AccessException e) {
                System.err.println("Il registro non è raggiungibile.");
                e.printStackTrace();
                return;
            } catch (NotBoundException | RemoteException e) {
                System.err.println("Errore RMI");
                e.printStackTrace();
                return;
            }
            System.out.println("connessione stabilita");
            try {
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
                            System.out.println("- LISTA DEI COMANDI DISPONIBILI:");
                            System.out.println("  register <username> <password> <tags>  * Effettua la registrazione a Winsome.");
                            System.out.println("  login <username> <password>            * Effettua il login a Winsome.");
                            System.out.println("  logout                                 * Effettua la logout da Winsome.");
                            System.out.println("  listfollowers                          * Mostra la lista degli utenti che ti seguono.");
                            System.out.println("  listusers                              * Mostra la lista degli utenti con almeno un tag in comune.");
                            System.out.println("  listfollowing                          * Mostra la degli utenti che si segue.");
                            System.out.println("  follow <username>                      * Permette di seguire un utente.");
                            System.out.println("  unfollow <username>                    * Permette di smettere di seguire un utente.");
                            System.out.println("  blog                                   * Mostra i post nel proprio blog.");
                            System.out.println("  post <titolo> <contenuto>              * Permette di pubblicare un post.");
                            System.out.println("  showfeed                               * Mostra i post nel proprio feed.");
                            System.out.println("  showpost <idPost>                      * Mostra un post specifico.");
                            System.out.println("  rewin <idPost>                         * Permette di fare il rewin di un post.");
                            System.out.println("  rate <idPost> <+1/-1>                  * Permette di valutare un post.");
                            System.out.println("  comment <idPost> <commento>            * Permette di commentare un post.");
                            System.out.println("  wallet                                 * Mostra il proprio portafoglio.");
                            System.out.println("  walletbtc                              * Mostra il bilancio convertito in Bitcoin.");
                            break;
                        }
                        case "register": {
                            try {
                                if (arguments.length < 3) throw new ArrayIndexOutOfBoundsException();
                                String tags = "";
                                for (int i = 2; i < arguments.length; i++) {
                                    tags += " " + arguments[i];
                                }
                                tags = tags.trim();
                                boolean response = stub.register(arguments[0], arguments[1], tags);
                                if (response) {
                                    System.out.println("Registrazione dell'utente " + arguments[0] + " effettuata con successo.");
                                } else {
                                    System.out.println("Registrazione dell'utente " + arguments[0] + " fallita.");
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                System.err.println("Richiesti almeno 3 argomenti, massimo 7.\n<username> <password> [tags]");
                            } catch (RemoteException ignored) {
                            }
                            break;
                        }
                        case "login": {
                            invia(output, request);
                            String response = ricevi(input);
                            if (response.equalsIgnoreCase("login ok")) {
                                System.out.println("Stato login: " + response);
                                username = arguments[0];
                                //Registrazione della callback per l'aggiornamento della listafollower

                            } else {
                                // ci sarà la risposta del server per capire come mai non è andato a buon fine
                                System.out.println("Errore: " + response);
                            }
                            break;
                        }
                        case "logout": {
                            invia(output, request);
                            String response = ricevi(input);
                            // Se sono connesso alla callback e la richiesta è andata a buon fine allora esco
                            if (response.equalsIgnoreCase("logout ok")) {
                                System.out.println("Stato logout: " + response);
                                // TODO: Gestione della chiusura della callback
                            }
                            break;
                        }
                        case "listfollowers": {
                            if(username == null) {
                                System.out.println("Errore, non è stato effettuato il login.");
                                break;
                            }
                            // TODO: è gestita dal client con la callback, da vedere
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
                            System.out.println("- Server > "+ricevi(input));
                        }
                        default: {
                            invia(output, request);
                            System.out.println("- Server > "+ricevi(input));
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Errore, connessione al server perduta.");
            }
        }

        // Chiusura del client e rimozione della Callback RMI

    }

    private static void invia (PrintWriter output, String send) {
        int bytes = send.getBytes().length;
        output.println(bytes);
        output.print(send);
        output.flush();
    }

    private static String ricevi (BufferedReader input) throws IOException {
        StringBuilder string = new StringBuilder();
        String data = input.readLine();
        if (data == null) throw new IOException();
        int j;
        try {
            j = Integer.parseInt(data);
        } catch (NumberFormatException e) {
            return data;
        }
        int i=0;
        while (i < j) {
            string.append((char) input.read());
            i++;
        }
        return string.toString();
    }
}