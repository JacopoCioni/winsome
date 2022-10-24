package it.unipi.jcioni.winsome.server;

import it.unipi.jcioni.winsome.core.model.*;
import it.unipi.jcioni.winsome.core.model.WinsomeData;
import it.unipi.jcioni.winsome.server.service.impl.WinsomeCallbackImpl;

import java.io.*;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class Handler implements Runnable {
    private final Socket clientSocket;
    private BufferedReader input = null;
    private PrintWriter output = null;
    private WinsomeData winsomeData;
    private Session session;

    public Handler(Socket clientSocket, WinsomeData winsomeData) {
        this.clientSocket = clientSocket;
        this.winsomeData = winsomeData;
        this.session = null;
    }

    public void run() {
        try {
            // Input stream di bytes
            InputStream inputStream = clientSocket.getInputStream();
            input = new BufferedReader(new InputStreamReader(inputStream));
            // Output stream di bytes
            OutputStream outputStream = clientSocket.getOutputStream();
            output = new PrintWriter(outputStream, true);
            if (output == null || input == null) {
                System.err.println("[SERV] - Errore: input ed output vuoti, impossibile stabilire la connessione.");
                return;
            }
            else {
                System.out.println("[SERV] - Gestione della richiesta in corso...");
                // Prova per vedere se funziona la serializzazione
                // serialize();
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
                                // TODO: Da rimuovere
                                System.out.println("[SERV] - Utenti registrati: ");
                                for (User u: winsomeData.getUsers())
                                    System.out.println(u.getUsername()+" ");
                                // ------------------
                                if (arguments.length != 2) {
                                    // Invio risposta di errore comando al client
                                    invia(output, "[SERV] - Errore, utilizzare: login <username> <password>");
                                    break;
                                }
                                login(arguments[0], arguments[1]);
                                break;
                            case "logout":
                                if (arguments.length != 0) {
                                    // Invio risposta di errore comando al client
                                    invia(output, "[SERV] - Errore, utilizzare: logout <username>");
                                    break;
                                }
                                if (session == null) {
                                    logout(null);
                                    break;
                                }
                                logout(session.getUsername());
                                break;
                            case "listusers":
                                if (arguments.length != 0) {
                                    invia(output, "[SERV] - Errore, utilizzare: listusers");
                                    break;
                                }
                                listUsers();
                                break;
                            case "listfollowing":
                                if (arguments.length != 0) {
                                    invia(output, "[SERV] - Errore, utilizzare: listfollowing");
                                    break;
                                }
                                listFollowing();
                                break;
                            case "follow":
                                if (arguments.length != 1) {
                                    invia(output, "[SERV] - Errore, utilizzare: follow <username>");
                                    break;
                                }
                                followUser(arguments[0]);
                                break;
                            case "unfollow":
                                if (arguments.length != 1) {
                                    invia(output, "[SERV] - Errore, utilizzare: unfollow <username>");
                                    break;
                                }
                                unfollowUser(arguments[0]);
                                break;
                            case "blog":
                                if (arguments.length != 0) {
                                    invia(output, "[SERV] - Errore, utilizzare: blog");
                                    break;
                                }
                                viewBlog();
                                break;
                            case "post":
                                if (arguments.length != 2) {
                                    invia(output, "[SERV] - Errore, utilizzare: post <title> <content>");
                                    break;
                                }
                                createPost(arguments[0], arguments[1]);
                                break;
                            case "delete":
                                if (arguments.length != 1) {
                                    invia(output, "[SERV] - Errore, utilizzare: delete <idPost>");
                                    break;
                                }
                                deletePost(arguments[0]);
                                break;
                            case "showfeed":
                                if (arguments.length != 0) {
                                    invia(output, "[SERV] - Errore, utilizzare: showfeed");
                                    break;
                                }
                                showFeed();
                                break;
                            case "showpost":
                                if (arguments.length != 1) {
                                    invia(output, "[SERV] - Errore, utilizzare: showpost <idPost>");
                                    break;
                                }
                                showPost(arguments[0]);
                                break;
                            case "rewin":
                                if (arguments.length != 1) {
                                    invia(output, "[SERV] - Errore, utilizzare: rewinpost <idPost>");
                                    break;
                                }
                                rewinPost(arguments[0]);
                                break;
                            case "rate":
                                if (arguments.length != 2) {
                                    invia(output, "[SERV] - Errore, utilizzare: rate <idPost> <voto>");
                                    break;
                                }
                                ratePost(arguments[0], Integer.parseInt(arguments[1]));
                                break;
                            case "comment":
                                if (arguments.length != 2) {
                                    invia(output, "[SERV] - Errore, utilizzare: comment <idPost> <comment>");
                                    break;
                                }
                                addComment(arguments[0], arguments[1]);
                                break;
                            case "wallet":
                                if (arguments.length != 0) {
                                    invia(output, "[SERV] - Errore, utilizzare: wallet");
                                    break;
                                }
                                getWallet();
                                break;
                            case "walletbtc":
                                if (arguments.length != 0) {
                                    invia(output, "[SERV] - Errore, utilizzare: wallet");
                                    break;
                                }
                                getWalletInBitcoin();
                                break;
                            default:
                                invia(output, "[SERV] - Errore, comando non riconosciuto.");
                                break;
                        }
                    } else {
                        break;
                    }
                } catch (IOException e) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login (String username, String password) {
        // Controllo che non sia già in una sessione aperta
        if (clientLogged()) {
            invia(output, "[SERV] - Sei attualmente collegato con l'account: " + session.getUsername());
            return;
        }
        System.out.println("[SERV] - User login: " + username + ": START");
        // Controllo che l'utente sia registrato
        User user = winsomeData.getUsers().stream()
                .filter(u ->
                        u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst().orElse(null);
        if (user == null) {
            invia(output, "[SERV] - Errore, utente non trovato. Verificare username & password.");
            return;
        }
        // Controllo che l'utente di cui voglio fare l'accesso non sia già loggato
        Session temp = Main.sessions.get(username);
        if (temp != null) {
            if (temp.getClientSocket() != clientSocket) {
                invia(output, "[SERV] - L'utente è attualmente collegato in un'altra sessione.");
                return;
            }
        }
        Main.sessions.put(username, new Session(clientSocket, username));
        session = new Session(clientSocket, username);
        System.out.println("[SERV] - User login: " + username + ": END");
        invia(output, "login ok");
    }

    private void logout (String username) {
        if(!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        System.out.println("[SERV] - User logout " + username + ": START");
        // Verifico prima di tutto che l'utente sia registrato cercandolo nella lista utenti registrati
        User user = winsomeData.getUsers().stream()
                .filter(u ->
                        u.getUsername().equals(username))
                .findFirst().orElse(null);
        if (user == null) {
            System.err.println("[SERV] - Errore, utente non trovato.");
            invia(output, "[SERV] - Errore, utente non trovato.");
            return;
        }
        // Mi occupo di rimuovere la connessione dal servizio, controllando che l'utente sia effettivamente loggato
        if (Main.sessions.get(username) != null) { //TODO: var main.sesson.getusername
            Session temp = Main.sessions.get(username);
            if (temp.getClientSocket().equals(clientSocket)) {
                // Posso effettivamente rimuovere la connessione dal servizio
                Main.sessions.remove(username);
                // Imposto nuovamente la sessione
                session = null;
                System.out.println("[SERV] - User logout " + username + ": END");
                invia(output, "logout ok");
            } else {
                invia(output, "[SERV] - Errore, non hai effettuato la login.");
            }
        } else {
            invia(output, "[SERV] - Errore, non hai effettuato la login.");
        }
    }

    private void listUsers () {
        if(!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        List<Tag> sessionUserTag = null;
        User clientUser = null;
        List<User> usersWithSameTag = new ArrayList<>();
        // Ricerco la lista dei tag dell'utente e l'utente che si trova attualmente in sessione
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
                sessionUserTag = u.getTags();
                break;
            }
        }
        for (User user: winsomeData.getUsers()) {
            if (user.getUsername().equals(session.getUsername())) {
                clientUser = user;
                break;
            }
        }
        for (User uTag: winsomeData.getUsers()) {
            for (Tag t: uTag.getTags()) {
                for (Tag sT: sessionUserTag) {
                    if (t.equals(sT) && !usersWithSameTag.contains(uTag)) {
                        usersWithSameTag.add(uTag);
                        break;
                    }
                }
            }
        }
        usersWithSameTag.remove(clientUser);
        // Finito questo ciclo controllo se ho ho trovato utenti con un tag in comune
        if (usersWithSameTag.size() == 0) {
            invia(output, "[SERV] - Nessun utente ha almeno un tag in comune con te.");
            return;
        }
        // A questo punto mi occupo di inoltrare la lista di utenti trovata
        StringBuilder out = new StringBuilder();
        out.append("[SERV] - Lista degli utenti con Tag in comune:$");
        for (User u: usersWithSameTag) {
            out.append("|-Utente: ").append(u.getUsername()).append("$");
        }
        // Composto il messaggio, lo inoltro
        invia(output, out.toString());
    }

    private void listFollowing () {
        if(!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        User clientUser = null;
        // Ricerco lo user in sessione
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "[SERV] - Errore, non è stato possibile fornire il servizio.");
            return;
        }
        // Invio la risposta
        StringBuilder out = new StringBuilder();
        out.append("Lista degli utente che segue lo user in sessione:$");
        for (String user: clientUser.getInteractions().getFollows().keySet()) {
            out.append("|-Utente: ").append(user).append("$");
        }
        // Composto il messaggio, lo inoltro
        invia(output, out.toString());
    }

    private void followUser (String username) {
        if(!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        if (username.equals(session.getUsername())) {
            invia(output, "[SERV] - Errore, non puoi seguire te stesso.");
            return;
        }
        if (!existUser(username)) {
            invia(output, "[SERV] - Errore, l'utente che vuoi seguire non esiste.");
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
                        f.getUsername().equals(session.getUsername()))
                .findFirst().orElse(null);
        // Aggiungo il follow
        boolean result;
        result = clientUser.getInteractions().addFollows(clientUser.getUsername(), username);
        if (!result) {
            invia(output, "[SERV] - Errore: stai già seguendo l'utente '"+username+"'");
            return;
        }
        follow.getInteractions().addFollowers(username, clientUser.getUsername());
        invia(output, "[SERV] - Hai cominciato a seguire l'utente: "+username);
        try {
            // Chi sto seguendo verrrà notificato che ho incominciato a seguirlo
            WinsomeCallbackImpl.addUpdate(username, session.getUsername());
        } catch (RemoteException e) {
            e.printStackTrace();
            System.err.println("[RMICallback] - Errore notifica callback.");
        }
    }

    private void unfollowUser (String username) {
        if(!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        if (username.equals(session.getUsername())) {
            invia(output, "[SERV] - Errore, non puoi unfolloware te stesso.");
            return;
        }
        if (!existUser(username)) {
            invia(output, "[SERV] - Errore, l'utente che vuoi unfolloware non esiste.");
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
                        f.getUsername().equals(session.getUsername()))
                .findFirst().orElse(null);
        // Rimuovo il follow
        boolean result;
        result = clientUser.getInteractions().removeFollows(clientUser.getUsername(), username);
        if (!result) {
            invia(output, "[SERV] - Errore: non segui l'utente '"+username+"'");
            return;
        }
        follow.getInteractions().removeFollowers(clientUser.getUsername());
        invia(output, "[SERV] - Hai smesso di seguire l'utente: "+username);
        try {
            // Chi sto smettendo di seguire verrrà notificato che ho smesso di seguirlo
            WinsomeCallbackImpl.removeUpdate(username, session.getUsername());
        } catch (RemoteException e) {
            e.printStackTrace();
            System.err.println("[RMICallback] - Errore notifica callback.");
        }
    }

    private void viewBlog () {
        if(!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        User clientUser = null;
        // Ricerco lo user in sessione
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "[SERV] - Errore, non è stato possibile fornire il servizio.");
            return;
        }
        ConcurrentLinkedDeque<Post> sessionUserBlog = clientUser.getBlog().getPosts();
        if (sessionUserBlog.size() == 0) {
            invia(output, "[SERV] - Il blog è vuoto.");
            return;
        }
        StringBuilder out = new StringBuilder();
        out.append("[SERV] - Lista dei post presenti nel blog:$");
        for (Post p: sessionUserBlog) {
            out.append("---------------------------------------------$");
            out.append("|-PostId: "+p.getIdPost()+"$");
            out.append("|-Titolo: "+p.getTitle()+"$");
            out.append("|-Data di pubblicazione: "+p.getTimestamp()+"$");
        }
        // Composto il messaggio, lo inoltro
        invia(output, out.toString());
    }

    private void createPost (String title, String text) {
        if(!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        User clientUser = null;
        // Ricerco lo user in sessione
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "[SERV] - Errore, non è stato possibile fornire il servizio.");
            return;
        }
        if (title.length() > Post.MAX_TITLE_LENGHT || text.length() > Post.MAX_TEXT_LENGHT) {
            invia(output, "[SERV] - Errore, il titolo o il contenuto contengono troppi caratteri.");
            return;
        }
        Post newPost = new Post(session.getUsername(), title, text);
        clientUser.getBlog().getPosts().add(newPost);
        invia(output, "[SERV] - Il post con idPost '"+newPost.getIdPost()+"' è stato creato correttamente.");
    }

    public void deletePost(String idPost) {
        if(!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        User clientUser = null;
        // Ricerco lo user in sessione
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "[SERV] - Errore, non è stato possibile fornire il servizio.");
            return;
        }

        // Ricerco il post da eliminare
        Post post = clientUser.getBlog().getPosts().stream()
                .filter(p -> p.getIdPost().equals(idPost))
                .findFirst().orElse(null);
        if (post == null) {
            invia(output, "[SERV] - Errore, post non trovato.");
            return;
        }
        for (User creator: winsomeData.getUsers()) {
            if(creator.getBlog().getPosts().contains(post)) {
                creator.getBlog().getPosts().remove(post);
                invia(output, "[SERV] - Post: "+idPost+" eliminato correttamente.");
                break;
            }
        }
    }

    private void showFeed() {
        if(!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        User clientUser = null;
        // Ricerco lo user in sessione
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "[SERV] - Errore, non è stato possibile fornire il servizio.");
            return;
        }
        List<Post> sessionUserFeed = new ArrayList<>();
        // Aggiungo tutti i post presenti nel blog di chi seguo nel mio feed
        for (String key: clientUser.getInteractions().getFollows().keySet()) {
            for (User user: winsomeData.getUsers()) {
                if (key.equals(user.getUsername())) {
                    sessionUserFeed.addAll(user.getBlog().getPosts());
                    break;
                }
            }
        }
        // clientUser.getInteractions().getFollows().keySet().stream().forEach(user -> sessionUserFeed.addAll(user.getBlog().getPosts()));
        // Controllo che il feed non sia vuoto
        if (sessionUserFeed.size() == 0) {
            invia(output, "[SERV] - Il feed è vuoto.");
            return;
        }
        // Ordino la lista feed
        sessionUserFeed.stream().sorted((o1, o2) ->
                (int) (o2.getTimestamp().getTime() - o1.getTimestamp().getTime()))
                .collect(Collectors.toList());

        StringBuilder out = new StringBuilder();
        out.append("[SERV] - Lista dei post presenti nel feed:$");
        for (Post p: sessionUserFeed) {
            out.append("---------------------------------------------$");
            out.append("* Post: "+p.getIdPost()+"$");
            out.append(" |-Autore: "+p.getCreator()+"$");
            out.append(" |-Titolo: "+p.getTitle()+"$");
            out.append(" |-Data di pubblicazione: "+p.getTimestamp()+"$");

        }
        // Composto il messaggio, lo inoltro
        invia(output, out.toString());
    }

    private void showPost(String idPost) {
        if (!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        Post post = null;
        for (User u: winsomeData.getUsers()) {
            for (Post p: u.getBlog().getPosts()) {
                if (p.getIdPost().equals(idPost)) {
                    post = p;
                }
            }
        }
        if(post == null) {
            invia(output, "[SERV] - Errore, post non trovato.");
        } else {
            StringBuilder out = new StringBuilder();
            out.append("[SERV] - Informazioni sul post ricercato:$");
            out.append("|-Autore: "+post.getCreator()+"$");
            out.append("|-Titolo: "+post.getTitle()+"$");
            out.append("|-Data di pubblicazione: "+post.getTimestamp()+"$");
            out.append("|-Contenuto: "+post.getText()+"$");
            out.append("|-Voti positivi: "+post.getNumberOfUpVotes()+"$");
            out.append("|-Voti negativi: "+post.getNumberOfDownVotes()+"$");
            out.append("|-Commenti: $");
            for (Comment c: post.getComments()) {
                out.append("---------------------------------------------$");
                out.append(" |-Autore: "+c.getCreator()+"$");
                out.append(" |-Msg: "+c.getValue()+"$");
                out.append(" |-Data di pubblicazione: "+c.getCommentTime()+"$");
            }
            invia(output, out.toString());
        }
    }

    private void rewinPost(String idPost) {
        if (!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        User clientUser = null;
        // Ricerco lo user in sessione
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "[SERV] - Errore, non è stato possibile fornire il servizio.");
            return;
        }
        List<Post> sessionUserFeed = new ArrayList<>();
        // Aggiungo tutti i post presenti nel blog di chi seguo nel mio feed
        for (String key: clientUser.getInteractions().getFollows().keySet()) {
            for (User user: winsomeData.getUsers()) {
                if (key.equals(user.getUsername())) {
                    sessionUserFeed.addAll(user.getBlog().getPosts());
                    break;
                }
            }
        }
        // clientUser.getInteractions().getFollows().keySet().stream().forEach(user -> sessionUserFeed.addAll(user.getBlog().getPosts()));
        // Controllo che il feed non sia vuoto
        if (sessionUserFeed.size() == 0) {
            invia(output, "[SERV] - Errore, il feed è vuoto.");
            return;
        }
        // Recupero il post
        Post rPost = sessionUserFeed.stream()
                .filter(post -> post.getIdPost().equals(idPost))
                .findFirst().orElse(null);
        Post p = new Post(clientUser.getUsername(),rPost.getTitle()+" (Rewin)",rPost.getText());
        clientUser.getBlog().getPosts().add(p);
        invia(output, "[SERV] - Rewin del post "+p.getIdPost()+" effettuato correttamente.");
    }

    private void ratePost(String idPost, int vote) {
        if (!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        if (vote != 1 && vote != -1) {
            invia(output, "[SERV] - Errore, voto non valido.");
            return;
        }
        // Ricerco il creatore del post da votare
        User creatore = winsomeData.getUsers().stream()
                .filter(user -> user.getBlog().getPosts().stream()
                        .filter(post -> post.getIdPost().equals(idPost))
                        .findFirst().orElse(null) != null)
                .findFirst().orElse(null);
        if (creatore == null) {
            invia(output, "[SERV] - Errore, utente non trovato.");
            return;
        }
        // Ricerco il post da votare
        Post post = creatore.getBlog().getPosts().stream()
                .filter(p -> p.getIdPost().equals(idPost))
                .findFirst().orElse(null);
        if (post == null) {
            invia(output, "[SERV] - Errore, post non trovato.");
            return;
        }
        if (session.getUsername().equals(creatore.getUsername())) {
            invia(output, "[SERV] - Errore, non puoi votare il tuo post.");
            return;
        }
        // Recupero il feed
        User clientUser = null;
        for(User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "[SERV] - Errore, non è stato possibile fornire il servizio.");
            return;
        }
        List<Post> sessionUserFeed = new ArrayList<>();
        // Aggiungo tutti i post presenti nel blog di chi seguo nel mio feed
        for (String key: clientUser.getInteractions().getFollows().keySet()) {
            for (User user: winsomeData.getUsers()) {
                if (key.equals(user.getUsername())) {
                    sessionUserFeed.addAll(user.getBlog().getPosts());
                    break;
                }
            }
        }
        // clientUser.getInteractions().getFollows().keySet().stream().forEach(user -> sessionUserFeed.addAll(user.getBlog().getPosts()));
        // Controllo che il feed non sia vuoto
        if (sessionUserFeed.size() == 0) {
            invia(output, "[SERV] - Errore, il feed è vuoto.");
            return;
        }
        // Controllo che il post faccia parte del feed
        if (!sessionUserFeed.contains(post)) {
            invia(output, "[SERV] - Errore, il post non fa parte del tuo feed.");
            return;
        }
        boolean result;
        if (vote == 1) {
            result = post.addVote(session.getUsername(), Vote.UP);
        } else {
            result = post.addVote(session.getUsername(), Vote.DOWN);
        }
        if (result) {
            invia(output, "[SERV] - Voto inserito correttamente.");
        } else {
            invia(output, "[SERV] - Errore, hai già votato questo post.");
        }
    }

    private void addComment(String idPost, String comment) {
        if (!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        // Ricerco il creatore del post da commentare
        User creatore = winsomeData.getUsers().stream()
                .filter(user -> user.getBlog().getPosts().stream()
                        .filter(post -> post.getIdPost().equals(idPost))
                        .findFirst().orElse(null) != null)
                .findFirst().orElse(null);
        if (creatore == null) {
            invia(output, "[SERV] - Errore, utente non trovato.");
            return;
        }
        // Ricerco il post da commentare
        Post post = creatore.getBlog().getPosts().stream()
                .filter(p -> p.getIdPost().equals(idPost))
                .findFirst().orElse(null);
        if (post == null) {
            invia(output, "[SERV] - Errore, post non trovato.");
            return;
        }
        // Controllo che il commento non sia rivolto ad un post creato dall'utente
        if (session.getUsername().equals(creatore.getUsername())) {
            invia(output, "[SERV] - Errore, non puoi commentare il tuo post.");
            return;
        }
        // Recupero il feed
        User clientUser = null;
        for(User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "[SERV] - Errore, non è stato possibile fornire il servizio.");
            return;
        }
        List<Post> sessionUserFeed = new ArrayList<>();
        // Aggiungo tutti i post presenti nel blog di chi seguo nel mio feed
        for (String key: clientUser.getInteractions().getFollows().keySet()) {
            for (User user: winsomeData.getUsers()) {
                if (key.equals(user.getUsername())) {
                    sessionUserFeed.addAll(user.getBlog().getPosts());
                    break;
                }
            }
        }
        // clientUser.getInteractions().getFollows().keySet().stream().forEach(user -> sessionUserFeed.addAll(user.getBlog().getPosts()));
        // Controllo che il feed non sia vuoto
        if (sessionUserFeed.size() == 0) {
            invia(output, "[SERV] - Errore, il feed è vuoto.");
            return;
        }
        // Controllo che il post faccia parte del feed
        if (!sessionUserFeed.contains(post)) {
            invia(output, "[SERV] - Errore, il post non fa parte del tuo feed.");
            return;
        }
        // Controllo che l'utente non abbia già commentato il post
        for(Comment c: post.getComments()) {
            if (c.getCreator().equals(session.getUsername())) {
                invia(output,"[SERV] - Errore, hai già commentato questo post.");
                return;
            }
        }
        Comment userComment = new Comment(session.getUsername(), comment);
        post.addComment(userComment);
        invia(output, "[SERV] - Commento inserito correttamente.");
    }

    private void getWallet() {
        if (!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        User clientUser = null;
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "[SERV] - Errore, non è stato possibile fornire il servizio.");
            return;
        }
        Wallet clientWallet = clientUser.getWallet();
        StringBuilder out = new StringBuilder();
        out.append("[SERV] - Analisi dello Wallet (@"+clientUser.getUsername()+"):$");
        out.append("* Bilancio: "+clientWallet.balance()+"$");
        if (clientWallet.getTransactions().size() != 0) {
            out.append("* Transazioni:$");
            for(Transaction t: clientWallet.getTransactions()) {
                out.append("---------------------------------------------$");
                out.append(" |-Valore: "+t.getValue()+"$");
                out.append(" |-Motivo: "+t.getMsg()+"$");
                out.append(" |-Data: "+t.getTimestamp()+"$");
            }
        } else {
            out.append(" |-Non sono presenti transazioni.$");
        }
        invia(output, out.toString());
    }

    private void getWalletInBitcoin() {
        if (!clientLogged()) {
            invia(output, "[SERV] - Errore, non sei loggato.");
            return;
        }
        User clientUser = null;
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "[SERV] - Errore, non è stato possibile fornire il servizio.");
            return;
        }
        Wallet clientWallet = clientUser.getWallet();
        double tasso = WinsomeUtils.generaRandom();
        double bitcoinBalance = clientWallet.balance() * tasso;
        StringBuilder out = new StringBuilder();
        out.append("[SERV] - Analisi dello Wallet (@"+clientUser.getUsername()+"):$");
        out.append("|-Bilancio: "+clientWallet.balance()+"$");
        out.append("|-Tasso di conversione: "+tasso+"$");
        out.append("|-Bilancio in Bitcoin: "+bitcoinBalance+"$");

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
