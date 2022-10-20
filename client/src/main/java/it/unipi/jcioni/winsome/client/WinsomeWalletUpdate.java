package it.unipi.jcioni.winsome.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class WinsomeWalletUpdate implements Runnable {

    private InetAddress multicastAddress;
    private final int multicastPort;
    private MulticastSocket multicastSocket;
    private volatile boolean exit = false;

    public WinsomeWalletUpdate(String multicastAddress, int multicastPort) {
        try {
            this.multicastAddress = InetAddress.getByName(multicastAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.multicastPort = multicastPort;
    }

    public void run() {
        try {
            multicastSocket = new MulticastSocket(multicastPort);
            multicastSocket.joinGroup(multicastAddress);

            byte[] buffer;
            DatagramPacket pacchetto;

            while(!exit) {
                try {
                    ByteBuffer temp = ByteBuffer.allocate(Integer.BYTES);
                    pacchetto = new DatagramPacket(temp.array(), temp.limit());
                    // questo pacchetto contiene la dimensione della stringa successiva
                    multicastSocket.receive(pacchetto);
                    // Ottengo la dimensione
                    int dim = ByteBuffer.wrap(pacchetto.getData()).getInt();

                    // Allocazione dinamica
                    buffer = new byte[dim];
                    pacchetto = new DatagramPacket(buffer, dim);
                    // ricevo il secondo pacchetto che contiene dati
                    multicastSocket.receive(pacchetto);
                    String guadagno = new String(pacchetto.getData(), 0, pacchetto.getLength());
                    System.out.println("[SERV] - Distribuzione di "+guadagno+" WINCOIN.");
                    System.out.println("[SERV] - Controllare il bilancio del proprio Wallet.");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        exit = true;
        multicastSocket.close();
    }
}
