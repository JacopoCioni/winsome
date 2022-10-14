package it.unipi.jcioni.winsome.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unipi.jcioni.winsome.core.exception.InvalidOperationException;
import it.unipi.jcioni.winsome.core.model.*;
import it.unipi.jcioni.winsome.core.model.WinsomeData;

import java.io.*;
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
    private final Gson gson;

    public Handler(Socket clientSocket, WinsomeData winsomeData) {
        this.clientSocket = clientSocket;
        this.winsomeData = winsomeData;
        this.session = null;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
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
                            System.out.println("Utenti registrati: ");
                            for (User u: winsomeData.getUsers())
                                System.out.println(u.getUsername()+" ");
                            // ------------------
                            if (arguments.length != 2) {
                                // Invio risposta di errore comando al client
                                invia(output, "Errore, utilizzare: login <username> <password>");
                                break;
                            }
                            login(arguments[0], arguments[1]);
                            break;
                        case "logout":
                            if (arguments.length != 1) {
                                // Invio risposta di errore comando al client
                                invia(output, "Errore, utilizzare: logout <username>");
                                break;
                            }
                            logout(session.getUsername());
                            break;
                        case "listusers":
                            if (arguments.length != 0) {
                                invia(output, "Errore, utilizzare: listusers");
                                break;
                            }
                            listUsers();
                            break;
                        case "listfollowing":
                            if (arguments.length != 0) {
                                invia(output, "Errore, utilizzare: listfollowing");
                                break;
                            }
                            listFollowing();
                            break;
                        case "follow":
                            if (arguments.length != 1) {
                                invia(output, "Errore, utilizzare: follow <username>");
                                break;
                            }
                            followUser(arguments[0]);
                            break;
                        case "unfollow":
                            if (arguments.length != 1) {
                                invia(output, "Errore, utilizzare: unfollow <username>");
                                break;
                            }
                            unfollowUser(arguments[0]);
                            break;
                        case "blog":
                            if (arguments.length != 0) {
                                invia(output, "Errore, utilizzare: blog");
                                break;
                            }
                            viewBlog();
                            break;
                        case "post":
                            if (arguments.length != 2) {
                                invia(output, "Errore, utilizzare: post <title> <content>");
                                break;
                            }
                            createPost(arguments[0], arguments[1]);
                            break;
                        case "showfeed":
                            if (arguments.length != 0) {
                                invia(output, "Errore, utilizzare: showfeed");
                                break;
                            }
                            showFeed();
                            break;
                        case "showpost":
                            if (arguments.length != 1) {
                                invia(output, "Errore, utilizzare: showpost <idPost>");
                                break;
                            }
                            showPost(arguments[0]);
                            break;
                        case "rewin":
                            if (arguments.length != 1) {
                                invia(output, "Errore, utilizzare: rewinpost <idPost>");
                                break;
                            }
                            rewinPost(arguments[0]);
                            break;
                        case "rate":
                            if (arguments.length != 2) {
                                invia(output, "Errore, utilizzare: rate <idPost> <voto>");
                                break;
                            }
                            ratePost(arguments[0], Integer.parseInt(arguments[1]));
                            break;
                        case "comment":
                            if (arguments.length != 2) {
                                invia(output, "Errore, utilizzare: comment <idPost> <comment>");
                                break;
                            }
                            addComment(arguments[0], arguments[1]);
                            break;
                        case "wallet":
                            if (arguments.length != 0) {
                                invia(output, "Errore, utilizzare: wallet");
                                break;
                            }
                            getWallet();
                            break;
                        case "walletbtc":
                            if (arguments.length != 0) {
                                invia(output, "Errore, utilizzare: wallet");
                                break;
                            }
                            getWalletInBitcoin();
                            break;
                        default:
                            invia(output, "Errore, comando non riconosciuto.");
                            break;
                    }
                } else {
                    break;
                }
            } catch (IOException e) {
                break;
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
        if(!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
            return;
        }
        System.out.println("User logout " + username + ": START");
        // Verifico prima di tutto che l'utente sia registrato cercandolo nella lista utenti registrati
        User user = winsomeData.getUsers().stream()
                .filter(u ->
                        u.getUsername().equals(username))
                .findFirst().orElse(null);
        if (user == null) {
            System.err.println("Errore, utente non trovato.");
            invia(output, "Errore, utente non trovato.");
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
                System.out.println("User logout " + username + ": END");
                invia(output, "logout ok");
            } else {
                invia(output, "Errore, non hai effettuato la login.");
            }
        } else {
            invia(output, "Errore, non hai effettuato la login.");
        }
    }

    private void listUsers () {
        if(!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
            return;
        }
        List<Tag> sessionUserTag = null;
        // Ricerco la lista dei tag dell'utente che si trova attualmente in sessione
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
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
        if(!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
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
            invia(output, "Errore, non è stato possibile fornire il servizio.");
            return;
        }
        List<User> sessionUserFollowing = new ArrayList<>();
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
        if(!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
            return;
        }
        if (username.equals(session.getUsername())) {
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
                        f.getUsername().equals(session.getUsername()))
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
        if(!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
            return;
        }
        if (username.equals(session.getUsername())) {
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
                        f.getUsername().equals(session.getUsername()))
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
        if(!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
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
            invia(output, "Errore, non è stato possibile fornire il servizio.");
            return;
        }
        List<Post> sessionUserBlog = clientUser.getBlog().getPosts();
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
        if(!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
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
            invia(output, "Errore, non è stato possibile fornire il servizio.");
            return;
        }
        if (title.length() > Post.MAX_TITLE_LENGHT || text.length() > Post.MAX_TEXT_LENGHT) {
            invia(output, "Errore, il titolo o il contenuto contengono troppi caratteri.");
            return;
        }
        clientUser.getBlog().getPosts().add(new Post(session.getUsername(), title, text));
        invia(output, "Il post intitolato '"+title+"' è stato creato correttamente.");
    }

    private void showFeed() {
        if(!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
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
            invia(output, "Errore, non è stato possibile fornire il servizio.");
            return;
        }
        List<Post> sessionUserFeed = new ArrayList<>();
        // Aggiungo tutti i post presenti nel mio blog
        clientUser.getFollows().stream().forEach(user -> sessionUserFeed.addAll(user.getBlog().getPosts()));
        // Controllo che il feed non sia vuoto
        if (sessionUserFeed.size() == 0) {
            invia(output, "Il feed è vuoto.");
            return;
        }
        // Ordino la lista feed
        sessionUserFeed.stream().sorted((o1, o2) ->
                (int) (o2.getTimestamp().getTime() - o1.getTimestamp().getTime()))
                .collect(Collectors.toList());

        StringBuilder out = new StringBuilder();
        out.append("Lista dei post presenti nel feed: \n");
        for (Post p: sessionUserFeed) {
            out.append("-> Post: "+p.getIdPost()+" - Autore: "+p.getCreator()+" - Titolo: "+p.getTitle()+"\n");
        }
        out.append("\n");
        // Composto il messaggio, lo inoltro
        invia(output, out.toString());
    }

    private void showPost(String idPost) {
        if (!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
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
            invia(output, "Errore, post non trovato.");
        } else {
            StringBuilder out = new StringBuilder();
            out.append("Informazioni sul post ricercato: \n");
            out.append("- Autore: "+post.getCreator()+"\n");
            out.append("- Titolo: "+post.getTitle()+"\n");
            out.append("- Contenuto: "+post.getText()+"\n");
            out.append("- Voti positivi: "+post.getNumberOfUpVotes()+"\n");
            out.append("- Voti negativi: "+post.getNumberOfDownVotes()+"\n");
            out.append("- Commenti: \n");
            for (Comment c: post.getComments()) {
                out.append("   * (Autore: "+c.getCreator()+")\n");
                out.append("     "+c.getValue()+"\n");
            }
            invia(output, out.toString());
        }
    }

    private void rewinPost(String idPost) {
        if (!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
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
            invia(output, "Errore, non è stato possibile fornire il servizio.");
            return;
        }
        List<Post> sessionUserFeed = new ArrayList<>();
        // Aggiungo tutti i post presenti nel mio blog
        clientUser.getFollows().stream().forEach(user -> sessionUserFeed.addAll(user.getBlog().getPosts()));
        // Controllo che il feed non sia vuoto
        if (sessionUserFeed.size() == 0) {
            invia(output, "Errore, il feed è vuoto.");
            return;
        }
        // Recupero il post
        Post rPost = sessionUserFeed.stream()
                .filter(post -> post.getIdPost().equals(idPost))
                .findFirst().orElse(null);
        Post p = new Post(clientUser.getUsername(),rPost.getTitle()+" (Rewin)",rPost.getText());
        clientUser.getBlog().getPosts().add(p);
        invia(output, "Rewin del post "+p.getIdPost()+" effettuato correttamente.");
    }

    private void ratePost(String idPost, int vote) {
        if (!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
            return;
        }
        if (vote != 1 && vote != -1) {
            invia(output, "Errore, voto non valido.");
            return;
        }
        // Ricerco il creatore del post da votare
        User creatore = winsomeData.getUsers().stream()
                .filter(user -> user.getBlog().getPosts().stream()
                        .filter(post -> post.getIdPost().equals(idPost))
                        .findFirst().orElse(null) != null)
                .findFirst().orElse(null);
        if (creatore == null) {
            invia(output, "Errore, utente non trovato.");
            return;
        }
        // Ricerco il post da votare
        Post post = creatore.getBlog().getPosts().stream()
                .filter(p -> p.getIdPost().equals(idPost))
                .findFirst().orElse(null);
        if (post == null) {
            invia(output, "Errore, post non trovato.");
            return;
        }
        if (session.getUsername().equals(creatore.getUsername())) {
            invia(output, "Errore, non puoi votare il tuo post.");
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
            invia(output, "Errore, non è stato possibile fornire il servizio.");
            return;
        }
        List<Post> sessionUserFeed = new ArrayList<>();
        // Aggiungo tutti i post presenti nel mio blog
        clientUser.getFollows().stream().forEach(user -> sessionUserFeed.addAll(user.getBlog().getPosts()));
        // Controllo che il feed non sia vuoto
        if (sessionUserFeed.size() == 0) {
            invia(output, "Errore, il feed è vuoto.");
            return;
        }
        // Controllo che il post faccia parte del feed
        if (!sessionUserFeed.contains(post)) {
            invia(output, "Errore, il post non fa parte del tuo feed.");
            return;
        }
        boolean result;
        if (vote == 1) {
            result = post.addVote(session.getUsername(), Vote.UP);
        } else {
            result = post.addVote(session.getUsername(), Vote.DOWN);
        }
        if (result) {
            invia(output, "Voto inserito correttamente.");
        } else {
            invia(output, "Errore, hai già votato questo post.");
        }
    }

    private void addComment(String idPost, String comment) {
        if (!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
            return;
        }
        // Ricerco il creatore del post da commentare
        User creatore = winsomeData.getUsers().stream()
                .filter(user -> user.getBlog().getPosts().stream()
                        .filter(post -> post.getIdPost().equals(idPost))
                        .findFirst().orElse(null) != null)
                .findFirst().orElse(null);
        if (creatore == null) {
            invia(output, "Errore, utente non trovato.");
            return;
        }
        // Ricerco il post da commentare
        Post post = creatore.getBlog().getPosts().stream()
                .filter(p -> p.getIdPost().equals(idPost))
                .findFirst().orElse(null);
        if (post == null) {
            invia(output, "Errore, post non trovato.");
            return;
        }
        // Controllo che il commento non sia rivolto ad un post creato dall'utente
        if (session.getUsername().equals(creatore.getUsername())) {
            invia(output, "Errore, non puoi commentare il tuo post.");
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
            invia(output, "Errore, non è stato possibile fornire il servizio.");
            return;
        }
        List<Post> sessionUserFeed = new ArrayList<>();
        // Aggiungo tutti i post presenti nel mio blog
        clientUser.getFollows().stream().forEach(user -> sessionUserFeed.addAll(user.getBlog().getPosts()));
        // Controllo che il feed non sia vuoto
        if (sessionUserFeed.size() == 0) {
            invia(output, "Errore, il feed è vuoto.");
            return;
        }
        // Controllo che il post faccia parte del feed
        if (!sessionUserFeed.contains(post)) {
            invia(output, "Errore, il post non fa parte del tuo feed.");
            return;
        }
        // Controllo che l'utente non abbia già commentato il post
        for(Comment c: post.getComments()) {
            if (c.getCreator().equals(session.getUsername())) {
                invia(output,"Errore, hai già commentato questo post.");
                return;
            }
        }
        Comment userComment = new Comment(session.getUsername(), comment);
        post.addComment(userComment);
        invia(output, "Commento inserito correttamente.");
    }

    private void getWallet() {
        if (!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
            return;
        }
        User clientUser = null;
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "Errore, non è stato possibile fornire il servizio.");
            return;
        }
        Wallet clientWallet = clientUser.getWallet();
        StringBuilder out = new StringBuilder();
        out.append("Analisi dello Wallet (@"+clientUser.getUsername()+"): \n");
        out.append("- Bilancio: "+clientWallet.balance()+"\n");
        if (clientWallet.getTransactions().size() != 0) {
            out.append("- Transazioni:\n");
            for(Transaction t: clientWallet.getTransactions()) {
                out.append("    * Valore: "+t.getValue()+"\n");
                out.append("    * Motivo: "+t.getMsg()+"\n");
                out.append("    * Data: "+t.getTimestamp()+"\n");
            }
        } else {
            out.append("  Non sono presenti transazioni.\n");
        }
        invia(output, out.toString());
    }

    private void getWalletInBitcoin() {
        if (!clientLogged()) {
            invia(output, "Errore, non sei loggato.");
            return;
        }
        User clientUser = null;
        for (User u: winsomeData.getUsers()) {
            if (u.getUsername().equals(session.getUsername())) {
                clientUser = u;
            }
        }
        if (clientUser == null) {
            invia(output, "Errore, non è stato possibile fornire il servizio.");
            return;
        }
        Wallet clientWallet = clientUser.getWallet();
        double tasso = WinsomeUtils.generaRandom();
        double bitcoinBalance = clientWallet.balance() * tasso;
        StringBuilder out = new StringBuilder();
        out.append("Analisi dello Wallet (@"+clientUser.getUsername()+"): \n");
        out.append("- Bilancio: "+clientWallet.balance()+"\n");
        out.append("- Tasso di conversione: "+tasso+"\n");
        out.append("- Bilancio in Bitcoin: "+bitcoinBalance+"\n");

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

    // Prende il contenuto della lista di utenti e la salva su disco come file .json
    public void serialize() {
        // Questa è una prova della serializzazione, di fatto non deve funzionare così
        List<User> utenti = new ArrayList<>();

        List<Tag> tags  = new ArrayList<>();
        tags.add(new Tag("Tennis"));
        tags.add(new Tag("Calcio"));
        tags.add(new Tag("Nuoto"));

        List<Post> posts = new ArrayList<>();
        posts.add(new Post("Jacopo", "Sedia", "Questa è una sedia."));
        posts.add(new Post("Jacopo", "Sedia", "Questa è una sedia."));
        for (Post p: posts) {
            p.addVote("Samuele", Vote.UP);
            p.addComment(new Comment("Samuele", "Bella sedia!"));
        }

        utenti.add(new User("Samuele", "prova", tags));
        utenti.add(new User("Jacopo", "ciao", tags));
        for (User u: utenti) {
            u.getBlog().setPosts(posts);
        }

        String json = gson.toJson(utenti);
        System.out.println("File JSon: " + json);
        try {
            File serverFolder = new File("WinsomeServer");
            if (!serverFolder.exists()) {
                serverFolder.mkdir();
            }
            File userFile = new File("WinsomeServer"+ File.separator+"Users.json");
            if (!userFile.exists()) {
                userFile.createNewFile();
            }
            WinsomeUtils.writeFile(json, "WinsomeServer"+ File.separator+"Users.json");
        } catch (IOException e){
            e.printStackTrace();
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
