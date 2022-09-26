package it.unipi.jcioni.winsome.core.exception;

public class UserNotFoundException extends Exception {
    public UserNotFoundException() {
        super("User not found");
    }
}
