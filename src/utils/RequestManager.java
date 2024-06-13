package utils;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.net.Socket;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe RequestManager.
 * Cette classe est responsable de la gestion des requêtes.
 */
public class RequestManager {

    private final ConfigManager configManager;
    private final LogManager logManager;
    private final FileManager fileManager;

    /**
     * Constructeur de la classe RequestManager.
     * @param configManager Le manager de configuration.
     * @param logManager Le manager de logs.
     */
    public RequestManager(ConfigManager configManager, LogManager logManager, FileManager fileManager) {
        this.configManager = configManager;
        this.logManager = logManager;
        this.fileManager = fileManager;
    }

    private Map<String, String> getSystemStatus() throws Exception {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        int processors = osBean.getAvailableProcessors();

        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long maxMB = heapMemoryUsage.getMax() / (1024 * 1024);
        long usedMB = heapMemoryUsage.getUsed() / (1024 * 1024);

        double systemLoad = osBean.getSystemLoadAverage();

        long total = 0;
        long used = 0;
        long avail = 0;
        for (FileStore store : FileSystems.getDefault().getFileStores()) {
            total += store.getTotalSpace();
            used += store.getTotalSpace() - store.getUsableSpace();
            avail += store.getUsableSpace();
        }

        Map<String, String> json = new HashMap<>();
        json.put("processors", "" + processors);
        json.put("systemLoad", "" + systemLoad);
        json.put("totalDiskSpace", "" + total);
        json.put("usedDiskSpace", "" + used);
        json.put("availableDiskSpace", "" + avail);
        json.put("maxHeapMemory", "" + maxMB);
        json.put("usedHeapMemory", "" + usedMB);

        return json;
    }

    private void writeStatusToFile() throws Exception {
        Map<String, String> status = getSystemStatus();
        String json = "{\n";
        for (Map.Entry<String, String> entry : status.entrySet()) {
            json += "  \"" + entry.getKey() + "\": \"" + entry.getValue() + "\",\n";
        }
        json = json.substring(0, json.length() - 2); // Enlève la dérnière virgule
        json += "\n}";

        // Ecris le json et le met dans le fichier system_status.json
        try (FileWriter file = new FileWriter(configManager.getConfigValue("/myweb/root") + "/system_status.json")) {
            file.write(json);
        }
    }


    /**
     * Méthode pour gérer une requête.
     * @param clientSocket Le socket client.
     * @param responseManager Le manager de réponses.
     * @throws Exception Si une erreur se produit lors de la gestion de la requête.
     */
    public void handleRequest(Socket clientSocket, ResponseManager responseManager) throws Exception {
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

            // Générer et écrire le statut du système dans un fichier JSON
            if (filePath.endsWith("serverStatus.html")) {
                writeStatusToFile();
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

        if (url.equals("/status")) {
            url = "/serverStatus.html";
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