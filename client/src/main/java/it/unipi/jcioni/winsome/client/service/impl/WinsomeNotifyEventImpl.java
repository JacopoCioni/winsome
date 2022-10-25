package it.unipi.jcioni.winsome.client.service.impl;

import it.unipi.jcioni.winsome.client.Main;
import it.unipi.jcioni.winsome.core.service.WinsomeNotifyEvent;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public class WinsomeNotifyEventImpl extends RemoteObject implements WinsomeNotifyEvent {

    //MainUser è l'utente in sessione, value è il valore da aggiungere alla lista
    public void addNotifyEvent(String mainUser, String value) throws RemoteException {
        boolean result = false;
        // Controllo se è già presente
        for (String s: Main.followers.keySet()) {
            if (s.equals(mainUser)) {
                for (String f: Main.followers.get(s)) {
                    if (f.equals(value)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        if (value != null && !result) {
            Main.followers.get(mainUser).add(value);
            System.out.println("[RMI] - L'utente '"+value+"' ha iniziato a seguirti.");
            return;
        }
        System.out.println("[RMI] - Errore nella ricezione della notifica.");
    }

    public void removeNotifyEvent(String mainUser, String value) throws RemoteException {
        boolean result = false;
        // Controllo se è presente
        for (String s: Main.followers.keySet()) {
            if (s.equals(mainUser)) {
                for (String f: Main.followers.get(s)) {
                    if (f.equals(value)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        if (value != null && result) {
            Main.followers.get(mainUser).remove(value);
            System.out.println("[RMI] - L'utente '"+value+"' non ti segue più.");
            return;
        }
        System.out.println("[RMI] - Errore nella ricezione della notifica.");
    }
}
