package utils;

import java.io.InputStream;

public class FileGenerator {

    private final ConfigManager configManager;
    private final FileManager fileManager;

    public FileGenerator(ConfigManager configManager, FileManager fileManager) {
        this.configManager = configManager;
        this.fileManager = fileManager;
//        try {
//            this.configManager.loadConfigFile();
//        } catch (Exception e) {
//            System.out.println("Erreur lors du chargement du fichier de configuration : " + e.getMessage());
//        }
    }

    public void generateFiles() throws Exception {
        String rootPath = configManager.getConfigValue("/myweb/root");
//        System.out.println("Root path: " + rootPath);
        String errorPath = configManager.getConfigValue("/myweb/error");
//        System.out.println("Error path: " + errorPath);

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