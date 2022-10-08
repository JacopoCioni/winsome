package it.unipi.jcioni.winsome.core.model;

import java.net.Socket;

public class Session {
    private String username;
    private Socket clientSocket;

    public Session (Socket clientSocket, String username) {
        this.clientSocket = clientSocket;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}
