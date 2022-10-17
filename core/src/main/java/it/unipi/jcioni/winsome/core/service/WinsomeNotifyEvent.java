package it.unipi.jcioni.winsome.client.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WinsomeNotifyEvent extends Remote {
    void addNotifyEvent (String value) throws RemoteException;
    void removeNotifyEvent (String value) throws RemoteException;
}
