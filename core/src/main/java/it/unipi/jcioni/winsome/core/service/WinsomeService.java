package it.unipi.jcioni.winsome.core.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;


public interface WinsomeService extends Remote {
    int SERVER_TCP_PORT = 8080;
    int SERVER_RMI_PORT = 6969;
    String RMI_SERVER_REGISTRY_NAME = "winsome-server";
    int RMI_CALLBACK_CLIENT_PORT = 6970;
    String RMI_CALLBACK_CLIENT_REGISTRY_NAME = "winsome-server-callback";
    String SERVER_ADDRESS = "localhost";
    int MULTICAST_PORT = 6799;
    String MULTICAST_ADDRESS = "224.0.0.1";

    boolean register(String username, String password, String tags) throws RemoteException;
    HashMap<String, List<String>> startFollowers(String username, String password) throws RemoteException;
}
