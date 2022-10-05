package it.unipi.jcioni.winsome.core.exception;

public class LogoutException extends Exception{
    public LogoutException() {
        super("User already unlogged!");
    }
}
