package it.unipi.jcioni.winsome.client.service.impl;

import it.unipi.jcioni.winsome.client.Main;
import it.unipi.jcioni.winsome.core.service.WinsomeNotifyEvent;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public class WinsomeNotifyEventImpl extends RemoteObject implements WinsomeNotifyEvent {

    public void addNotifyEvent(String value) throws RemoteException {
        if (value != null && !Main.followers.contains(value)) {
            Main.followers.add(value);
            System.out.println("[RMI] - L'utente '"+value+"' ha iniziato a seguirti.");
            return;
        }
        System.out.println("[RMI] - Errore nella ricezione della notifica.");
    }

    public void removeNotifyEvent(String value) throws RemoteException {
        if (value != null && Main.followers.contains(value)) {
            Main.followers.remove(value);
            System.out.println("[RMI] - L'utente '"+value+"' non ti segue più.");
            return;
        }
        System.out.println("[RMI] - Errore nella ricezione della notifica.");
    }
}
