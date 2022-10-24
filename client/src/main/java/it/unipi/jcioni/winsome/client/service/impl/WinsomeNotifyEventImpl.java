package it.unipi.jcioni.winsome.client.service.impl;

import it.unipi.jcioni.winsome.client.Main;
import it.unipi.jcioni.winsome.core.service.WinsomeNotifyEvent;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public class WinsomeNotifyEventImpl extends RemoteObject implements WinsomeNotifyEvent {

    // Value è la chiave e mainUser è il valore
    public void addNotifyEvent(String mainUser, String value) throws RemoteException {
        boolean result = false;
        for (String s: Main.followers.keySet()) {
            if (s.equals(value)) {
                result = true;
                break;
            }
        }
        if (value != null && !result) {
            Main.followers.put(value, mainUser);
            System.out.println("[RMI] - L'utente '"+value+"' ha iniziato a seguirti.");
            return;
        }
        System.out.println("[RMI] - Errore nella ricezione della notifica.");
    }

    public void removeNotifyEvent(String value) throws RemoteException {
        boolean result = false;
        for (String s: Main.followers.keySet()) {
            if (s.equals(value)) {
                result = true;
                break;
            }
        }
        if (value != null && result) {
            Main.followers.remove(value);
            System.out.println("[RMI] - L'utente '"+value+"' non ti segue più.");
            return;
        }
        System.out.println("[RMI] - Errore nella ricezione della notifica.");
    }
}
