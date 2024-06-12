package managers;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Classe ResponseManager.
 * Cette classe est responsable de l'envoi des réponses.
 */
public class ResponseManager {

    private final LogManager logManager;

    /**
     * Constructeur de la classe ResponseManager.
     * @param logManager Le manager de logs.
     */
    public ResponseManager(LogManager logManager) {
        this.logManager = logManager;
    }

    /**
     * Méthode pour envoyer une réponse.
     * @param printWriter Le PrintWriter pour écrire la réponse.
     * @param outputStream L'OutputStream pour envoyer le contenu du fichier.
     * @param status Le statut de la réponse.
     * @param contentType Le type de contenu.
     * @param content Le contenu du fichier.
     * @throws Exception Si une erreur se produit lors de l'envoi de la réponse.
     */
    public void sendResponse(PrintWriter printWriter, OutputStream outputStream, String status, String contentType, byte[] content) throws Exception {
        try {
            printWriter.println(status);
            printWriter.println("Content-Type: " + contentType);
            printWriter.println("Connection: close");
            printWriter.println();
            outputStream.write(content);
            outputStream.flush();
        } catch (Exception e) {
            logManager.print("Erreur lors de l'envoi de la réponse : " + e.getMessage(), LogManager.SEVERE);
            throw e;
        }
    }
}