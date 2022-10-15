package it.unipi.jcioni.winsome.server.service;

import it.unipi.jcioni.winsome.client.service.WinsomeNotifyEvent;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WinsomeCallback extends Remote {
    void registerForCallback(String username, WinsomeNotifyEvent clientInterface) throws RemoteException;
    void unregisterForCallback(String username) throws RemoteException;

    void addUpdate(String username, String value) throws RemoteException;

    void removeUpdate(String username, String value) throws RemoteException;
}
