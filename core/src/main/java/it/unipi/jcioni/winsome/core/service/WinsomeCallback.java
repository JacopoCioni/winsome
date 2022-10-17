package it.unipi.jcioni.winsome.core.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WinsomeCallback extends Remote {
    void registerForCallback(String username, WinsomeNotifyEvent clientInterface) throws RemoteException;
    void unregisterForCallback(String username) throws RemoteException;

}
