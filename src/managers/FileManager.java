package managers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Classe FileManager.
 * Cette classe est responsable de la gestion des fichiers.
 */
public class FileManager {

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
}