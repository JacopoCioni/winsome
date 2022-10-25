package it.unipi.jcioni.winsome.core.model;

import java.io.*;
import java.util.Properties;

public class WinsomeConfig {

    private final Properties properties;
    private final String path;

    public WinsomeConfig(boolean isServer) throws IOException {

        if (isServer) {
            this.path = "serverConfig.properties";
        } else {
            this.path = "clientConfig.properties";
        }
        File file = new File("WinsomeServer"+File.separator+path);

        if (!file.exists()) {
            OutputStream outputStream = new FileOutputStream("WinsomeServer"+File.separator+path);
            properties = new Properties();
            if (isServer) {
                properties.setProperty("SERVER_TCP_PORT", "8080");
                properties.setProperty("SERVER_RMI_PORT", "6969");
                properties.setProperty("MULTICAST_PORT", "6799");
                properties.setProperty("RMI_CALLBACK_CLIENT_PORT", "6970");
                properties.setProperty("MULTICAST_ADDRESS", "224.0.0.1");
                properties.setProperty("RMI_SERVER_REGISTRY_NAME", "winsome-server");
                properties.setProperty("RMI_CALLBACK_CLIENT_REGISTRY_NAME", "winsome-server-callback");
                properties.setProperty("REWARDS_TIMEOUT", "60000");
            } else {
                properties.setProperty("SERVER_TCP_PORT", "8080");
                properties.setProperty("SERVER_RMI_PORT", "6969");
                properties.setProperty("MULTICAST_PORT", "6799");
                properties.setProperty("RMI_CALLBACK_CLIENT_PORT", "6970");
                properties.setProperty("MULTICAST_ADDRESS", "224.0.0.1");
                properties.setProperty("RMI_SERVER_REGISTRY_NAME", "winsome-server");
                properties.setProperty("RMI_CALLBACK_CLIENT_REGISTRY_NAME", "winsome-server-callback");
                properties.setProperty("SERVER_ADDRESS", "localhost");
            }
            properties.store(outputStream, "#########################################################################\n File di configurazione di WINSOME.\n#########################################################################");
            outputStream.close();
        } else {
            InputStream inputStream = new FileInputStream("WinsomeServer"+File.separator+path);
            properties = new Properties();
            properties.load(inputStream);
            inputStream.close();
        }
    }

    public String getProperties (String key) {
        if (properties != null) {
            return properties.getProperty(key, null);
        }
        return null;
    }
}
