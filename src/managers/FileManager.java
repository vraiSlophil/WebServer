package managers;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Classe FileManager.
 * Cette classe est responsable de la gestion des fichiers.
 */
public class FileManager {

    private ConfigManager configManager;
    private final LogManager logManager;

    /**
     * Constructeur de la classe FileManager.
     * @param configManager Le manager de configuration.
     * @param logManager Le manager de logs.
     */
    public FileManager(ConfigManager configManager, LogManager logManager) {
        this.configManager = configManager;
        this.logManager = logManager;
    }

    /**
     * Méthode pour lire le contenu d'un fichier.
     * @param filePath Le chemin d'accès au fichier.
     * @return Le contenu du fichier sous forme de tableau de bytes.
     * @throws Exception Si une erreur se produit lors de la lecture du fichier.
     */
    public byte[] readFile(String filePath) throws Exception {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (Exception e) {
            logManager.print("Erreur lors de la lecture du fichier : " + e.getMessage(), LogManager.SEVERE);
            throw e;
        }
    }
}