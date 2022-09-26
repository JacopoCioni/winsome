package it.unipi.jcioni.winsome.core.exception;

public class SameUserException extends Exception {
    public SameUserException() {
        super("This is the same user");
    }
}
