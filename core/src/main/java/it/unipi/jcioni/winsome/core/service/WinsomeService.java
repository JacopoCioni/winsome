package it.unipi.jcioni.winsome.core.service;

import java.rmi.Remote;

public interface WinsomeService extends Remote {
    boolean login(String username, String password);
}
