package it.unipi.jcioni.winsome.server;

import com.sun.jdi.IntegerValue;
import it.unipi.jcioni.winsome.core.service.WinsomeService;
import it.unipi.jcioni.winsome.core.service.impl.WinsomeServiceImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {
    public static void main(String[] args) {
        try {
            /* Creazione di un'istanza dell'oggetto EUStatsService */
            WinsomeServiceImpl statsService = new WinsomeServiceImpl();
            /* Esportazione dell'Oggetto */
            WinsomeService stub = (WinsomeService) UnicastRemoteObject.exportObject(statsService, 0);
            /* Creazione di un registry sulla porta args[0] */
            int port = args.length > 0 && args[0] != null && args[0].length() > 0 ? Integer.parseInt(args[0]) : 8080;
            LocateRegistry.createRegistry(port);
            /*Pubblicazione dello stub nel registry */
            Registry r = LocateRegistry.getRegistry(port);
            r.rebind("WINSOME-SERVER", stub);
            System.out.println("Server ready");
        }
        /* If any communication failures occur... */
        catch (RemoteException e) {
            System.out.println("Communication error " + e.toString());
        }
    }
}