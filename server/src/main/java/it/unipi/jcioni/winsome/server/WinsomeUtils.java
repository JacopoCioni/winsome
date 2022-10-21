package it.unipi.jcioni.winsome.server;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class WinsomeUtils {
    // TODO: Inserire in file di properties/ file yaml
    public static final String address = "https://www.random.org/decimal-fractions/?num=1&dec=20&col=1&format=plain&rnd=new";

    // Generatore di numeri pseudo casuali utilizzando un servizio esterno
    // Per generare il numero faccio uso del servizio RANDOM.ORG
    // Recupero i valori utilizzando questo indirizzo: 'address'
    /*
        SPIEGAZIONE DELL'INDIRIZZO:
        * num -> The number of decimal-fraction requested.
        * dec -> Decimal places (max.20)
        * col -> Format in n columns
        * format = plain -> If plain is specified, the server produces
          as minimalistic document of type plain text (MIME type text/plain) document,
          which is easy to parse.
        * rnd = new -> If new is specified, then a new randomization will be created from
          the truly random bitstream at RANDOM.ORG.
     */
    public static double generaRandom() {
        try {
            URL url = new URL(address);
            URLConnection urlConnection = url.openConnection();
            // Apro lo stream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String result = bufferedReader.readLine();
            bufferedReader.close();
            return Double.parseDouble(result);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Errore nella generazione del numero casuale.");
        }
        // In caso di errore
        return 0;
    }

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
