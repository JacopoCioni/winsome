package it.unipi.jcioni.winsome.client;

import it.unipi.jcioni.winsome.core.service.WinsomeService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Scanner read = new Scanner(System.in);
        Socket socket;
        while (true) {
            try {
                socket = new Socket(WinsomeService.SERVER_ADDRESS, WinsomeService.SERVER_TCP_PORT);
            } catch (IOException e) {
                System.err.println("Impossibile connettersi al server, riprovare più tardi.");
                break;
            }

            System.out.println("Connessione TCP stabilita.");

            Registry reg;
            WinsomeService stub;
            try {
                reg = LocateRegistry.getRegistry(WinsomeService.SERVER_ADDRESS, WinsomeService.SERVER_RMI_PORT);
                stub = (WinsomeService) reg.lookup(WinsomeService.RMI_SERVER_REGISTRY_NAME);
            } catch (AccessException e) {
                System.err.println("Il registro non è raggiungibile.");
                e.printStackTrace();
                return;
            } catch (NotBoundException | RemoteException e) {
                System.err.println("Errore RMI");
                e.printStackTrace();
                return;
            }

            try {
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String command;
                while (true) {
                    try {
                        command = read.nextLine();
                    } catch (NoSuchElementException e) {
                        continue;
                    }
                    command = command.trim();
                    String[] arguments = command.split("\\s+");
                    command = arguments[0];
                    arguments = (String[]) Arrays.stream(arguments).collect(Collectors.toList()).subList(1, arguments.length).toArray();
                    switch (command) {
                        case "register":
                            try {
                                if (arguments.length < 3) throw new ArrayIndexOutOfBoundsException();
                                String username = arguments[0];
                                String password = arguments[1];
                                String tags = "";
                                for (int i = 2; i < arguments.length; i++) {
                                    tags += " " + arguments[i];
                                }
                                tags = tags.trim();
                                boolean response = stub.register(username, password, tags);
                                if (response) {
                                    System.out.println("Registrazione dell'utente " + username + " effettuata con successo.");
                                } else {
                                    System.out.println("Registrazione dell'utente " + username + " fallita.");
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                System.err.println("Richiesti almeno 3 argomenti, massimo 7.\n<username> <password> [tags]");
                            } catch (RemoteException ignored) { }
                            break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}