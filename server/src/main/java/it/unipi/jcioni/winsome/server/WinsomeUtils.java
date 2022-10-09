package it.unipi.jcioni.winsome.server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class WinsomeUtils {

    // Legge il contenuto di un file e lo converte in stringa
    public static String readFile(File file) throws IOException {
        // Istanzio un file reader
        FileReader fileReader = new FileReader(file);
        int charText;
        // Creo una stringa vuota
        String text = "";
        // Leggo il contenuto del file finché non rimane più niente da leggere
        while ((charText = fileReader.read()) != -1) {
            // Aggiungo il carattere letto nella stringa text
            text += (char) charText;
        }
        // Una volta finito di leggere il file, chiudo il file reader
        fileReader.close();

        return text;
    }

    // Scrivo il contenuto di una stringa su disco
    public static void writeFile(String text, String path) throws IOException {
        // Istanzio un file writer e gli passo il percorso passato per parametro
        FileWriter writer = new FileWriter(path);
        // Scrivo il file sul disco
        writer.write(text);
        // Una volta terminato chiudo il file writer
        writer.close();
    }
}
