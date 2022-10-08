package it.unipi.jcioni.winsome.server;

import it.unipi.jcioni.winsome.core.exception.InvalidOperationException;
import it.unipi.jcioni.winsome.core.exception.LoginException;
import it.unipi.jcioni.winsome.core.exception.LogoutException;
import it.unipi.jcioni.winsome.core.exception.UserNotFoundException;
import it.unipi.jcioni.winsome.core.model.Post;
import it.unipi.jcioni.winsome.core.model.Session;
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
import java.util.stream.Collectors;

public class Handler implements Runnable {
    private final Socket clientSocket;
    private PrintWriter output = null;
    private BufferedReader input = null;
    private WinsomeData winsomeData;
    private Session session;

    // Variabili per rintracciare l'utente in sessione
    private String clientUsername = null;
    private boolean logged = false;


    public Handler(Socket clientSocket, WinsomeData winsomeData) {
        this.clientSocket = clientSocket;
        this.winsomeData = winsomeData;
        this.session = null;
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
                // Sarebbe meglio farla come nel clientMain
                if (request != null) {
                    String[] temp = request.split(" ");
                    String command  = temp[0];
                    String[] arguments = new String[temp.length-1];
                    System.arraycopy(temp, 1, arguments, 0, temp.length-1);

                    // Gestione della richiesta.
                    switch (command) {
                        case "login":
                            // Messo qui momentaneamente per vedere gli utenti registrati
                            for (User u: winsomeData.getUsers())
                                System.out.println(u.getUsername()+" ");
                            // Restituisce TRUE se l'utente non è loggato
                            if (arguments.length == 2) {
                                clientUsername = arguments[0];
                                login(arguments[0], arguments[1]);
                            } else {
                                // Invio risposta di errore comando al client
                                invia(output, "Errore, utilizzare: login <username> <password>");
                            }
                            break;
                        case "logout":
                            // Controllo che l'utente sia loggato
                            if(!clientLogged()) {
                                invia(output, "Errore, non sei loggato.");
                            } else {
                                // Effettuo il logout
                                if (arguments.length == 0) {
                                    logout(session.getUsername());
                                } else {
                                    // Invio risposta di errore comando al client
                                    invia(output, "Errore, utilizzare: logout <username>");
                                }
                            }
                            break;
                        case "listusers":
                            if (arguments.length != 0) {
                                invia(output, "Errore, utilizzare: listusers");
                            } else if (!logged) {
                                invia(output, "Errore, non è ancora stato effettuato il login.");
                            } else {
                                listUsers();
                            }
                            break;
                        case "listfollowing":
                            if (arguments.length != 0) {
                                invia(output, "Errore, utilizzare: listfollowing");
                            } else if (!logged) {
                                invia(output, "Errore, non è ancora stato effettuato il login.");
                            } else {
                                listFollowing();
                            }
                            break;
                        case "follow":
                            if (arguments.length != 1) {
                                invia(output, "Errore, utilizzare: follow <username>");
                            } else if (!logged) {
                                invia(output, "Errore, non è ancora stato effettuato il login.");
                            } else {
                                followUser(arguments[0]);
                            }
                            break;
                        case "unfollow":
                            if (arguments.length != 1) {
                                invia(output, "Errore, utilizzare: unfollow <username>");
                            } else if (!logged) {
                                invia(output, "Errore, non è ancora stato effettuato il login.");
                            } else {
                                unfollowUser(arguments[0]);
                            }
                            break;
                        case "blog":
                            if (arguments.length != 0) {
                                invia(output, "Errore, utilizzare: blog");
                            } else if (!logged) {
                                invia(output, "Errore, non è ancora stato effettuato il login.");
                            } else {
                                viewBlog();
                            }
                            break;
                        case "post":
                            if (arguments.length != 2) {
                                invia(output, "Errore, utilizzare: post <title> <content>");
                            } else if (!logged) {
                               invia(output, "Errore, non è ancora stato effettuato il login.");
                            } else {
                                createPost(arguments[0], arguments[1]);
                            }
                            break;
                        case "showfeed":
                            if (arguments.length != 0) {
                                invia(output, "Errore, utilizzare: showfeed");
                            } else if(!logged) {
                                invia(output, "Errore, non è ancora stato effettuato il login.");
                            } else {
                                showFeed();
                            }
                        case "showpost":
                            // Continuare da qui.
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void login (String username, String password) {
        // Controllo che non sia già in una sessione aperta
        if (clientLogged()) {
            invia(output, "Sei attualmente collegato con l'account: " + session.getUsername());
            return;
        }
        System.out.println("User login: " + username + ": START");
        // Controllo che l'utente sia registrato
        User user = winsomeData.getUsers().stream()
                .filter(u ->
                        u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst().orElse(null);
        if (user == null) {
            invia(output, "Errore, utente non trovato. Verificare username & password.");
            return;
        }
        // Controllo che l'utente di cui voglio fare l'accesso non sia già loggato
        Session temp = Main.sessions.get(username);
        if (temp != null) {
            if (temp.getClientSocket() == clientSocket) {
                invia(output, "Hai già effettuato il login.");
            }
            else {
                invia(output, "L'utente è attualmente collegato in un'altra sessione.");
            }
            return;
        } else {
            Main.sessions.put(username, new Session(clientSocket, username));
            session = new Session(clientSocket, username);
        }
        System.out.println("User login: " + username + ": END");
        invia(output, "login ok");
    }

    private void logout (String username) {
        System.out.println("User logout " + username + ": START");
        // Verifico prima di tutto che l'utente sia registrato cercandolo nella lista utenti registrati
        User user = winsomeData.getUsers().stream()
                .filter(u ->
                        u.getUsername().equals(username))
                .findFirst().orElse(null);
        if (user == null) {
            System.err.println("Errore, utente non trovato.");
            invia(output, "Errore, utente non trovato.");
        } else {
            // Mi occupo di rimuovere la connessione dal servizio, controllando che l'utente sia effettivamente loggato
            if (Main.sessions.get(username) != null) {
                Session temp = Main.sessions.get(username);
                if (temp.getClientSocket() == clientSocket) {
                    // Posso effettivamente rimuovere la connessione dal servizio
                    Main.sessions.remove(username);
                    // Imposto nuovamente la sessione
                    session = null;
                    System.out.println("User logout " + username + ": END");
                    invia(output, "logout ok");
                } else {
                    invia(output, "Errore, non hai effettuato la login.");
                }
            } else {
                invia(output, "Errore, non hai effettuato la login.");
            }
        }
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
        List<Post> sessionUserBlog;
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

    private void createPost (String title, String text) {
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
        if (title.length() > Post.MAX_TITLE_LENGHT || text.length() > Post.MAX_TEXT_LENGHT) {
            invia(output, "Errore, il titolo o il contenuto contengono troppi caratteri.");
        }
        clientUser.getBlog().getPosts().add(new Post(clientUser, title, text));
        invia(output, "Il post intitolato '"+title+"' è stato creato correttamente.");
    }

    private void showFeed() {
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
        List<Post> sessionUserFeed = new ArrayList<>();
        // Aggiungo tutti i post presenti nel mio blog
        clientUser.getFollows().stream().forEach(user -> sessionUserFeed.addAll(user.getBlog().getPosts()));
        // Controllo che il feed non sia vuoto
        if (sessionUserFeed.size() == 0) {
            invia(output, "Il feed è vuoto.");
        }
        // Ordino la lista feed
        sessionUserFeed.stream().sorted((o1, o2) ->
                (int) (o2.getTimestamp().getTime() - o1.getTimestamp().getTime()))
                .collect(Collectors.toList());

        StringBuilder out = new StringBuilder();
        out.append("Lista dei post presenti nel feed: \n");
        for (Post p: sessionUserFeed) {
            out.append("-> Post: "+p.getIdPost()+" - Autore: "+p.getCreator().getUsername()+" - Titolo: "+p.getTitle()+"\n");
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

    // Ritorna true se l'utente è loggato.
    private boolean clientLogged () {
        if (session != null) {
            // è loggato
            return true;
        } else {
            // non è loggato
            return false;
        }
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
