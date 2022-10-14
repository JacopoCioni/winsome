package it.unipi.jcioni.winsome.server.service.impl;

import it.unipi.jcioni.winsome.client.service.WinsomeNotifyEvent;
import it.unipi.jcioni.winsome.server.service.WinsomeCallback;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.HashMap;

public class WinsomeCallbackImpl extends RemoteObject implements WinsomeCallback {

    private static final HashMap<String, WinsomeNotifyEvent> clients = new HashMap<>();

    public synchronized void registerForCallback(String username, WinsomeNotifyEvent clientInterface) throws RemoteException {
        if (!clients.containsKey(username)) {
            clients.put(username, clientInterface);
        }
    }

    public synchronized void unregisterForCallback(String username) throws RemoteException {
        if (clients.containsKey(username)) {
            clients.remove(username);
        }
    }
    // TODO: Da capire come fare aggiungi e rimuovi

    public void addUpdate(String username, String value) throws RemoteException {
        if (clients.containsKey(username)) {
            clients.get(username).addNotifyEvent(value);
        }
    }

    public void removeUpdate(String username, String value) throws RemoteException {
        if (clients.containsKey(username)) {
            clients.get(username).removeNotifyEvent(value);
        }
    }

}
