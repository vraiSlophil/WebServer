package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Classe FileManager.
 * Cette classe est responsable de la gestion des fichiers.
 */
public class FileManager {

    private LogManager logManager;

    /**
     * Constructeur de la classe FileManager.
     */
    public FileManager() {
    }

    /**
     * Méthode pour vérifier si un fichier existe.
     * @param filePath Le chemin d'accès au fichier.
     * @return true si le fichier existe, false sinon.
     */
    public boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && !file.isDirectory();
    }

    /**
     * Méthode pour lire le contenu d'un fichier.
     * @param filePath Le chemin d'accès au fichier.
     * @return Le contenu du fichier sous forme de tableau de bytes.
     * @throws Exception Si une erreur se produit lors de la lecture du fichier.
     */
    public byte[] readFile(String filePath) throws Exception {
        if (!fileExists(filePath)) {
            throw new Exception("Le fichier " + filePath + " n'existe pas ou est un répertoire.");
        }
        return Files.readAllBytes(Paths.get(filePath));
    }

    /**
     * Méthode pour copier un flux d'entrée s'il n'existe pas déjà.
     * @param sourceStream Le flux d'entrée source.
     * @param destinationPath Le chemin d'accès au fichier de destination.
     * @throws IOException Si une erreur se produit lors de la copie du flux.
     */
    public void copyStreamIfNotExists(InputStream sourceStream, String destinationPath) throws Exception {
//        System.out.println("Attempting to copy stream to: " + destinationPath);
        File destinationFile = new File(destinationPath);
        if (!destinationFile.exists()) {
            // Créer le répertoire parent si nécessaire
            File parentDir = destinationFile.getParentFile();
            if (!parentDir.exists()) {
                boolean dirsCreated = parentDir.mkdirs();
                if (dirsCreated) {
                    logManager.print("Dossier parent créé : " + destinationPath, LogManager.INFO);
                } else {
                    logManager.print("Impossible de créer un dossier parent : " + destinationPath, LogManager.ERROR);
                }
            }
            // Copier le fichier
            Files.copy(sourceStream, Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
            logManager.print("Fichier copié à : " + destinationPath, LogManager.INFO);
        } else {
            logManager.print("Le fichier existe déjà à : " + destinationPath, LogManager.INFO);
        }
    }

    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }
}