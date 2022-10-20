package it.unipi.jcioni.winsome.server;

import it.unipi.jcioni.winsome.core.model.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static it.unipi.jcioni.winsome.core.service.WinsomeService.MULTICAST_ADDRESS;
import static it.unipi.jcioni.winsome.core.service.WinsomeService.MULTICAST_PORT;

public class WinsomeRewards implements Runnable{

    private WinsomeData winsomeData;
    private static int multicastPort = MULTICAST_PORT;
    private static String multicastAddress = MULTICAST_ADDRESS;
    public long timeCheck = 0;
    // Mi serve per mantenere il calcolo delle ricompense fino a quando il server non si interrompe
    private volatile boolean exit = false;

    // Costruttore, devo passargli la struttura dati condivisa
    public WinsomeRewards (WinsomeData winsomeData) {
        this.winsomeData = winsomeData;
    }

    public void run() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket(null);
            InetAddress inetAddress = InetAddress.getLocalHost();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, multicastPort);
            // Settando questa a true permetto di associare più istanze di socket multicast allo stesso
            // socketAddress. Deve essere fatto prima del binding.
            datagramSocket.setReuseAddress(true);
            datagramSocket.bind(inetSocketAddress);

            DatagramPacket pacchetto;
            byte[] buffer;
            ConcurrentLinkedDeque<Post> posts = null;

            while(!exit) {
                // Ottengo tutti i post pubblicati sulla piattaforma.
                for(User user: winsomeData.getUsers()) {
                    for (Post post: user.getBlog().getPosts()) {
                        posts.add(post);
                    }
                }
                if (posts.size() != 0) {
                    double guadagno = 0;
                    double saldo = 0;
                    for (Post p: posts) {
                        guadagno = calculateRew(p);
                        saldo = saldo + guadagno;
                    }


                    if (saldo != 0) {
                        String out = String.format("%.3f", round(saldo,3));
                        buffer = out.getBytes(StandardCharsets.UTF_8);

                        // Invio la lunghezza della stringa
                        ByteBuffer temp = ByteBuffer.allocate(Integer.BYTES).putInt(buffer.length);
                        pacchetto = new DatagramPacket(temp.array(), temp.limit(), InetAddress.getByName(multicastAddress), multicastPort);
                        datagramSocket.send(pacchetto);

                        // Invio la stringa
                        pacchetto = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(multicastAddress), multicastPort);
                        datagramSocket.send(pacchetto);
                        System.out.println("[REWARD] - Invio della notifica su "+multicastAddress+":"+multicastPort+" per il premio "+out);
                    }
                    timeCheck = System.currentTimeMillis();
                }
                // Aspetto prima di fare il prossimo controllo sui rewards
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("[REWARD] - Chiuso.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double calculateRew(Post p) {
        double guadagno;
        p.addIterazioni();
        int iterazioniRewards = p.getIterazioniRewards();
        double logUno = 0;
        double logDue = 0;

        // Calcolo del primo logaritmo
        ConcurrentHashMap<String, Vote> votes = p.getVotes(timeCheck);
        for (Vote v: votes.values()) {
            logUno = logUno + v.getValue();
        }
        if (logUno <= 0) {
            logUno = 0;
        }
        logUno++;
        logUno = Math.log(logUno);

        // Calcolo del secondo logaritmo
        // Recupero gli autori di tutti i commenti
        ConcurrentLinkedDeque<String> commentsCreator = null;
        for (Comment c: p.getComments(timeCheck)) {
            if (!commentsCreator.contains(c.getCreator())) {
                commentsCreator.add(c.getCreator());
            }
        }
        for (String u: commentsCreator) {
            int commentsPerUser =p.getNumberOfUserComments(u);
            logDue = logDue + (2/(1+Math.pow(Math.E, -(commentsPerUser-1))));
        }
        logDue++;
        logDue = Math.log(logDue);

        guadagno = (logUno + logDue)/iterazioniRewards;
        if (guadagno != 0) {
            // aggiornamento Wallet dello user.
            updateRew(p.getIdPost(), guadagno, votes, commentsCreator);
        }
        return guadagno;
    }

    private void updateRew (String idPost, double guadagno, ConcurrentHashMap<String, Vote> votes, ConcurrentLinkedDeque<String> commentsCreator) {
        // Curatori: Collezione dati non ordinata dove non possono stare elementi duplicati
        Set<String> curatori = new LinkedHashSet<>();
        String reason = "Ricompensa curatore per il post: "+idPost;
        // Aggiungo votatori (solo positivi) e commentatori al Set di curatori
        for(String user: votes.keySet()) {
            if (votes.get(user).equals(Vote.UP)) {
                curatori.add(user);
            }
        }
        curatori.addAll(commentsCreator);
        if (curatori.size() == 0) {
            return;
        }
        // Il guadagno del curatore è un 30% diviso per il numero di tutti i curatori
        double guadagnoCuratore = (guadagno*0.3)/curatori.size();
        // Aggiorno il bilancio dei curatori
        for (String curatore: curatori) {
            for (User u: winsomeData.getUsers()) {
                if (u.getUsername().equals(curatore)) {
                    u.getWallet().addTransaction(guadagnoCuratore, reason);
                }
            }
        }
        reason = "Ricompensa autore per il post: "+idPost;
        double guadagnoAutore = guadagno*0.7;
        // Aggiorno il bilancio dell'autore
        for (User u: winsomeData.getUsers()) {
            for (Post p: u.getBlog().getPosts()) {
                if (p.getIdPost().equals(idPost)) {
                    u.getWallet().addTransaction(guadagnoAutore, reason);
                    return;
                }
            }
        }
    }

    public void stop() {
        exit = true;
    }

    private double round(double in, int precision) {
        double multi = Math.pow(10, precision);
        return Math.round(in*multi)/multi;
    }

}
