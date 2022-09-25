package it.unipi.jcioni.winsome.core.exception;

public class LoginException extends Exception {
    public LoginException() {
        super("User already logged");
    }
}
