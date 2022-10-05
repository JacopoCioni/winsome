package it.unipi.jcioni.winsome.server;

import it.unipi.jcioni.winsome.core.exception.InvalidOperationException;
import it.unipi.jcioni.winsome.core.exception.LoginException;
import it.unipi.jcioni.winsome.core.exception.LogoutException;
import it.unipi.jcioni.winsome.core.model.Post;
import it.unipi.jcioni.winsome.core.model.Tag;
import it.unipi.jcioni.winsome.core.model.User;
import it.unipi.jcioni.winsome.core.service.WinsomeData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Handler implements Runnable {
    private final Socket clientSocket;
    private PrintWriter output = null;
    private BufferedReader input = null;
    private WinsomeData winsomeData;

    // Variabili per rintracciare l'utente in sessione
    private String clientUsername = null;
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
                                clientUsername = arguments[0];
                                // Result descrive l'esecuzione del metodo
                                login(arguments[0], arguments[1]);
                            } else {
                                // Invio risposta di errore comando al client
                                invia(output, "Errore, utilizzare: login <username> <password>");
                            }
                            break;
                        case "logout":
                            if (arguments.length == 1) {
                                logout(arguments[0]);
                            } else {
                                // Invio risposta di errore comando al client
                                invia(output, "Errore, utilizzare: logout <username>");
                            }
                            break;
                        case "listusers":
                            if (!logged) {
                                invia(output, "Errore, non è ancora stato effettuato il login.");
                            } else {
                                listUsers();
                            }
                            break;
                        case "listfollowing":
                            if (!logged) {
                                invia(output, "Errore, non è ancora stato effettuato il login.");
                            } else {
                                listFollowing();
                            }
                            break;
                        case "follow":
                            if (arguments.length != 1) {
                                invia(output, "Errore, utilizzare: follow <username>");
                            }
                            if (!logged) {
                                invia(output, "Errore, non è ancora stato effettuato il login.");
                            } else {
                                followUser(arguments[0]);
                            }
                            break;
                        case "unfollow":
                            if (arguments.length != 1) {
                                invia(output, "Errore, utilizzare: unfollow <username>");
                            }
                            if (!logged) {
                                invia(output, "Errore, non è ancora stato effettuato il login.");
                            } else {
                                unfollowUser(arguments[0]);
                            }
                            break;
                        case "blog":
                            if (!logged) {
                                invia(output, "Errore, non è ancora stato effettuato il login.");
                            } else {
                                viewBlog();
                            }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void login (String username, String password) {
        System.out.println("User login: " + username + " START");
        User user = winsomeData.getUsers().stream()
                .filter(u ->
                        u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst().orElse(null);
        try {
            user.login();
        } catch (NullPointerException ex) {
            System.out.println("Errore, username o password errati.");
            invia(output, "Errore, username o password errati.");
        } catch (LoginException ex) {
            System.out.println("Errore, l'utente è già loggato.");
            invia(output, "Errore, l'utente è già loggato.");
        }
        System.out.println("User login: " + username + " END");
        logged = true;
        invia(output, "login ok");
    }

    private void logout (String username) {
        System.out.println("User logout " + username + " START");
        User user = winsomeData.getUsers().stream()
                .filter(u ->
                        u.getUsername().equals(username))
                .findFirst().orElse(null);
        try {
            user.logout();
        } catch (NullPointerException ex) {
            System.err.println("Errore, utente non trovato.");
        } catch (LogoutException ex) {
            System.err.println("Errore, l'utente non è loggato.");
        }
        System.out.println("User logout " + username + " END");
        logged = false;
        invia(output, "logout ok");
    }

    private void listUsers () {
        List<Tag> sessionUserTag = null;
        // Ricerco la lista dei tag dell'utente che si trova attualmente in sessione
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(clientUsername)) {
                sessionUserTag = u.getTags();
            }
        }
        // Se la lista dovesse essere vuota abbiamo un errore.
        if (sessionUserTag == null) {
            invia(output, "Errore, non hai nessun tag impostato.");
            return;
        }
        List<User> sameTag = new ArrayList<>();
        int cont = 0;
        for (User u: winsomeData.getUsers()) {
            for (Tag t: u.getTags()) {
                for (Tag t2: sessionUserTag) {
                    if (t2.equals(t)) cont++;
                }
                // Utilizzo un contatore per contare i tags.
                // Ogni volta che ne trovo almeno uno in comune aggiungo l'utente alla lista
                if (cont > 0) {
                    sameTag.add(u);
                }
                cont = 0;
            }
        }
        // Finito questo ciclo controllo se ho ho trovato utenti con un tag in comune
        if (sameTag.size() == 0) {
            invia(output, "Nessun utente ha almeno un tag in comune con te.");
            return;
        }
        // A questo punto mi occupo di inoltrare la lista di utenti trovata
        StringBuilder out = new StringBuilder();
        out.append("Lista degli utenti con Tag in comune:\n");
        for (User u: sameTag) {
            out.append("->Utente: ").append(u.getUsername()).append("\n");
        }
        out.append("\n");
        // Composto il messaggio, lo inoltro
        invia(output, out.toString());
    }

    private void listFollowing () {
        List<User> sessionUserFollowing = new ArrayList<>();
        User clientUser = null;
        // Ricerco lo user in sessione
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(clientUsername)) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "Errore, non è stato possibile fornire il servizio.");
            return;
        }
        // Ricerco tutti gli utenti che followano lo user in sessione
        for (User f: winsomeData.getUsers()) {
            if (f.getFollows().contains(clientUser)) {
                sessionUserFollowing.add(f);
            }
        }
        if (sessionUserFollowing.size() == 0) {
            invia(output, "Non sei seguito da nessun utente.");
            return;
        }
        // Invio la risposta
        StringBuilder out = new StringBuilder();
        out.append("Lista degli utente che seguono lo user in sessione:\n");
        for (User user: sessionUserFollowing) {
            out.append("->Utente: ").append(user.getUsername()).append("\n");
        }
        out.append("\n");
        // Composto il messaggio, lo inoltro
        invia(output, out.toString());
    }

    private void followUser (String username) {
        if (username.equals(clientUsername)) {
            invia(output, "Errore, non puoi seguire te stesso.");
            return;
        }
        if (!existUser(username)) {
            invia(output, "Errore, l'utente che vuoi seguire non esiste.");
            return;
        }
        // Cerco l'utente da seguire con username dato in input
        User follow = winsomeData.getUsers().stream()
                .filter(f ->
                        f.getUsername().equals(username))
                .findFirst().orElse(null);
        // Ricerco lo user in sessione
        User clientUser = winsomeData.getUsers().stream()
                .filter(f ->
                        f.getUsername().equals(clientUsername))
                .findFirst().orElse(null);
        // Aggiungo il follow
        try {
            clientUser.addFollows(follow);
            invia(output, "Hai cominciato a seguire l'utente: "+username);
            // Notifica chiamata callback DA FARE
        } catch (InvalidOperationException e) {
            e.printStackTrace();
            invia(output, "Errore, non è stato possibile eseguire l'operazione.");
        }
    }

    private void unfollowUser (String username) {
        if (username.equals(clientUsername)) {
            invia(output, "Errore, non puoi unfolloware te stesso.");
            return;
        }
        if (!existUser(username)) {
            invia(output, "Errore, l'utente che vuoi unfolloware non esiste.");
            return;
        }
        // Cerco l'utente da unfolloware con username dato in input
        User follow = winsomeData.getUsers().stream()
                .filter(f ->
                        f.getUsername().equals(username))
                .findFirst().orElse(null);
        // Ricerco lo user in sessione
        User clientUser = winsomeData.getUsers().stream()
                .filter(f ->
                        f.getUsername().equals(clientUsername))
                .findFirst().orElse(null);
        // Rimuovo il follow
        try {
            clientUser.removeFollows(follow);
            invia(output, "Hai smesso di seguire l'utente: "+username);
            // Notifica chiamata callbak DA FARE
        } catch (InvalidOperationException e) {
            e.printStackTrace();
            invia(output, "Errore, non è stato possibile eseguire l'operazione.");
        }
    }

    private void viewBlog () {
        List<Post> sessionUserBlog = new ArrayList<>();
        User clientUser = null;
        // Ricerco lo user in sessione
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(clientUsername)) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "Errore, non è stato possibile fornire il servizio.");
            return;
        }
        sessionUserBlog = clientUser.getBlog().getPosts();
        if (sessionUserBlog.size() == 0) {
            invia(output, "Il blog è vuoto.");
            return;
        }
        StringBuilder out = new StringBuilder();
        out.append("Lista dei post presenti nel blog: \n");
        for (Post p: sessionUserBlog) {
            out.append("-> PostId: "+p.getIdPost()+"\n");
        }
        out.append("\n");
        // Composto il messaggio, lo inoltro
        invia(output, out.toString());
    }

    private boolean existUser(String user) {
        boolean result = false;
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(user)) {
                result = true;
                break;
            }
        }
        return result;
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
