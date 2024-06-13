package utils;

import java.io.*;
import java.net.Socket;

/**
 * Classe RequestManager.
 * Cette classe est responsable de la gestion des requêtes.
 */
public class RequestManager {

    private final ConfigManager configManager;
    private final LogManager logManager;

    /**
     * Constructeur de la classe RequestManager.
     * @param configManager Le manager de configuration.
     * @param logManager Le manager de logs.
     */
    public RequestManager(ConfigManager configManager, LogManager logManager) {
        this.configManager = configManager;
        this.logManager = logManager;
    }

    /**
     * Méthode pour gérer une requête.
     * @param clientSocket Le socket client.
     * @param responseManager Le manager de réponses.
     * @param fileManager Le manager de fichiers.
     * @throws Exception Si une erreur se produit lors de la gestion de la requête.
     */
    public void handleRequest(Socket clientSocket, ResponseManager responseManager, FileManager fileManager) throws Exception {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream, true);

            // Lire la première ligne de la requête
            String requestLine = in.readLine();

            if (requestLine == null) {
                return;
            }

            String filePath = getFilePath(requestLine);
            String askedFile = filePath;

            if (!fileManager.fileExists(filePath)) {
                filePath = configManager.getConfigValue("/myweb/error") + "/404.html";
            }


            // Lire le contenu du fichier
            byte[] content;
            String status;
            try {
                content = fileManager.readFile(filePath);
                status = filePath.endsWith("/404.html") ? "HTTP/1.1 404 Not Found" : "HTTP/1.1 200 OK";
            } catch (Exception e) {
                content = fileManager.readFile(configManager.getConfigValue("/myweb/error") + "/500.html");
                status = "HTTP/1.1 500 Internal Server Error";
            }

            // Déterminer le type de contenu en fonction de l'extension du fichier
            String contentType = getContentType(filePath);

            // Envoi de la réponse au client
            responseManager.sendResponse(printWriter, outputStream, status, contentType, content);

            logManager.print(askedFile + " a été demandé par le client " + clientSocket.getInetAddress() + " " + status, LogManager.INFO);
        } catch (Exception e) {
            logManager.print("Erreur lors de la gestion de la requête : " + e.getMessage(), LogManager.SEVERE);
            throw e;
        }
    }

    private String getFilePath(String requestLine) throws Exception {
        String url = requestLine.split(" ")[1];

        if (url.equals("/")) {
            url = "/index.html";
        }

        // Lire le fichier correspondant à l'URL
        String filePathBase = configManager.getConfigValue("/myweb/root");

        return filePathBase + url;
    }

    /**
     * Renvoie le type de contenu en fonction de l'extension du fichier.
     *
     * @param filePath le chemin d'accès au fichier
     * @return le type de contenu
     */
    private String getContentType(String filePath) {
        String extension = "";

        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            extension = filePath.substring(i + 1);
        }

        return switch (extension) {
            case "html", "htm" -> "text/html";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "png" -> "image/png";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            default -> "application/octet-stream";
        };
    }
}