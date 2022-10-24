package it.unipi.jcioni.winsome.core.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WinsomeNotifyEvent extends Remote {
    void addNotifyEvent (String mainUser, String value) throws RemoteException;
    void removeNotifyEvent (String value) throws RemoteException;
}
