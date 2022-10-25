package it.unipi.jcioni.winsome.core.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;


public interface WinsomeService extends Remote {
    boolean register(String username, String password, String tags) throws RemoteException;
    HashMap<String, List<String>> startFollowers(String username, String password) throws RemoteException;
}
