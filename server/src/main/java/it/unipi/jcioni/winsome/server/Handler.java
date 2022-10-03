package it.unipi.jcioni.winsome.server;

import it.unipi.jcioni.winsome.core.exception.LoginException;
import it.unipi.jcioni.winsome.core.model.User;
import it.unipi.jcioni.winsome.core.service.WinsomeData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Handler implements Runnable {
    private final Socket clientSocket;
    private PrintWriter output = null;
    private BufferedReader input = null;
    private WinsomeData winsomeData;
    private boolean logged = false;


    public Handler(Socket clientSocket, WinsomeData winsomeData) {
        this.clientSocket = clientSocket;
        this.winsomeData = winsomeData;
    }

    public void run() {
        try {
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (output == null || input == null) {
            System.out.println("Errore: input ed output vuoti. Impossibile stabilire la connessione.");
            return;
        }
        else {
            System.out.println("Gestione della richiesta in corso...");
        }

        String request;
        while (true) {
            try {
                request = ricevi(input);
                if (request != null) {
                    String[] temp = request.split(" ");
                    String command  = temp[0];
                    String[] arguments = new String[temp.length-1];
                    System.arraycopy(temp, 1, arguments, 0, temp.length-1);

                    // Gestione della richiesta.
                    switch (command) {
                        case "login":
                            if (arguments.length == 2) {
                                // Temp descrive l'esecuzione del metodo
                                boolean result = login(arguments[0], arguments[1]);
                                if (!result) invia(output, "Errore: l'utente è loggato o i dati sono errati.");
                            } else {
                                // Invio risposta di errore comando al client
                                invia(output, "Errore, utilizzare: login <username> <password>");
                            }
                            break;
                        case "logout":
                            if (arguments.length == 1) {
                                boolean result = logout(arguments[0]);
                                if(!result) invia(output, "Errore: l'utente non è loggato o non è stato trovato.");
                            } else {
                                // Invio risposta di errore comando al client
                                invia(output, "Errore, utilizzare: logout <username>");
                            }
                            break;
                        case "listusers":
                            if(!logged) {
                                invia(output, "Errore, non è ancora stato effettuato il login.");
                            } else {
                                listUsers(); //Da implementare
                            }
                            break;


                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean login(String username, String password) {
        System.out.println("User login " + username + " START");
        User user = winsomeData.getUsers().stream()
                .filter(u ->
                        u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst().orElse(null);
        try {
            user.login();
        } catch (NullPointerException ex) {
            System.out.println("Errore, username o password errati.");
            return false;
        } catch (LoginException ex) {
            System.out.println("Errore, l'utente è già loggato.");
            return false;
        }
        System.out.println("User login " + username + " END");
        logged = true;
        invia(output, "login ok");
        return true;
    }

    private boolean logout(String username) {
        System.out.println("User logout " + username + " START");
        User user = winsomeData.getUsers().stream()
                .filter(u ->
                        u.getUsername().equals(username))
                .findFirst().orElse(null);
        try {
            user.logout();
        } catch (NullPointerException ex) {
            System.out.println("Errore, utente non trovato.");
            return false;
        } catch (LoginException ex) {
            System.out.println("Errore, l'utente non è loggato.");
        }
        System.out.println("User logout " + username + " END");
        logged = false;
        invia(output, "logout ok");
        return true;
    }

    private void listUsers() {

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
