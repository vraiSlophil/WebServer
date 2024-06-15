package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigFileGenerator {

    private final String sourceConfPath = "/config/myweb.conf";
    private final String destinationPath;
    private final FileManager fileManager;

    public ConfigFileGenerator(FileManager fileManager) throws IOException {
        this.fileManager = fileManager;
        // Lire le fichier .ini pour obtenir le chemin du fichier de configuration
        Properties properties = new Properties();
        InputStream in = getClass().getResourceAsStream("/myweb.ini");
        properties.load(in);
        destinationPath = properties.getProperty("cfgfile");
        in.close();
    }

    public void generateConfigFiles() throws Exception {
        InputStream sourceConfStream = getClass().getResourceAsStream(sourceConfPath);
        fileManager.copyStreamIfNotExists(sourceConfStream, destinationPath);
    }
}