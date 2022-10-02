package it.unipi.jcioni.winsome.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Handler implements Runnable {
    private final Socket clientSocket;
    private PrintWriter output = null;
    private BufferedReader input = null;

    public Handler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (output == null || input == null) {
            System.out.println("Error, something wrong happened.");
            return;
        }

        String request;
        while (true) {
            try {
                request = receive(input);
                if (request != null) {
                    String[] temp = request.split(" ");
                    String r  = temp[0];
                    String[] arguments = new String[temp.length-1];
                    System.arraycopy(temp, 1, arguments, 0, temp.length-1);

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String receive(BufferedReader input) throws IOException {
        StringBuilder string = new StringBuilder();
        String data = input.readLine();
        if (data == null) throw new IOException();
        int j;
        try {
            j = Integer.parseInt(data);
        } catch (NumberFormatException e) {
            return data;
        }
        int i=0;
        while (i<j) {
            string.append((char) input.read());
            i++;
        }
        return string.toString();
    }

}
