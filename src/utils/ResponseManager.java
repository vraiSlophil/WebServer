package utils;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Classe ResponseManager.
 * Cette classe est responsable de l'envoi des réponses.
 */
public class ResponseManager {

    /**
     * Constructeur de la classe ResponseManager.
     */
    public ResponseManager() {
    }

    /**
     * Méthode pour envoyer une réponse.
     *
     * @param printWriter  Le PrintWriter pour écrire la réponse.
     * @param outputStream L'OutputStream pour envoyer le contenu du fichier.
     * @param status       Le statut de la réponse.
     * @param contentType  Le type de contenu.
     * @param content      Le contenu du fichier.
     * @throws Exception Si une erreur se produit lors de l'envoi de la réponse.
     */
    public void sendResponse(PrintWriter printWriter, OutputStream outputStream, String status, String contentType, byte[] content, boolean isBase64Encoding) throws Exception {
        printWriter.println(status);
        printWriter.println("Content-Type: " + contentType);
        if (isBase64Encoding) {
            printWriter.println("Content-Encoding: base64");
        }
        printWriter.println("Connection: close");

        printWriter.println();

        outputStream.write(content);
        outputStream.flush();
    }
}