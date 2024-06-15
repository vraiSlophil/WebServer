package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe ConfigFileGenerator.
 * Cette classe est responsable de la génération du fichier de configuration.
 */
public class ConfigFileGenerator {

    private final String sourceConfPath = "/config/myweb.conf";
    private final String destinationPath;
    private final FileManager fileManager;

    /**
     * Constructeur de la classe ConfigFileGenerator.
     * @param fileManager Le gestionnaire de fichiers.
     * @throws IOException Si une erreur se produit lors de la lecture du fichier .ini.
     */
    public ConfigFileGenerator(FileManager fileManager) throws IOException {
        this.fileManager = fileManager;
        // Lire le fichier .ini pour obtenir le chemin du fichier de configuration
        Properties properties = new Properties();
        InputStream in = getClass().getResourceAsStream("/myweb.ini");
        properties.load(in);
        destinationPath = properties.getProperty("cfgfile");
        in.close();
    }

    /**
     * Méthode pour générer le fichier de configuration.
     * @throws Exception Si une erreur survient lors de la génération du fichier.
     */
    public void generateConfigFiles() throws Exception {
        InputStream sourceConfStream = getClass().getResourceAsStream(sourceConfPath);
        fileManager.copyStreamIfNotExists(sourceConfStream, destinationPath);
    }
}