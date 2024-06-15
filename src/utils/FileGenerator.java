package utils;

import java.io.InputStream;

/**
 * Classe FileGenerator.
 * Cette classe est responsable de la génération des fichiers nécessaires au serveur web.
 */
public class FileGenerator {

    private final ConfigManager configManager;
    private final FileManager fileManager;

    /**
     * Constructeur de la classe FileGenerator.
     * @param configManager Le gestionnaire de configuration.
     * @param fileManager Le gestionnaire de fichiers.
     */
    public FileGenerator(ConfigManager configManager, FileManager fileManager) {
        this.configManager = configManager;
        this.fileManager = fileManager;
    }

    /**
     * Méthode pour générer les fichiers nécessaires au serveur web.
     * @throws Exception Si une erreur survient lors de la génération des fichiers.
     */
    public void generateFiles() throws Exception {
        String rootPath = configManager.getConfigValue("/myweb/root");
        String errorPath = configManager.getConfigValue("/myweb/error");

        // Copier les fichiers d'erreur s'ils n'existent pas déjà
        InputStream error403Stream = getClass().getResourceAsStream("/error/403.html");
        fileManager.copyStreamIfNotExists(error403Stream, errorPath + "/403.html");

        InputStream error404Stream = getClass().getResourceAsStream("/error/404.html");
        fileManager.copyStreamIfNotExists(error404Stream, errorPath + "/404.html");

        InputStream error500Stream = getClass().getResourceAsStream("/error/500.html");
        fileManager.copyStreamIfNotExists(error500Stream, errorPath + "/500.html");

        // Copier le fichier 'serverStatus.html' s'il n'existe pas déjà
        InputStream serverStatusStream = getClass().getResourceAsStream("/html/serverStatus.html");
        fileManager.copyStreamIfNotExists(serverStatusStream, rootPath + "/serverStatus.html");
    }
}