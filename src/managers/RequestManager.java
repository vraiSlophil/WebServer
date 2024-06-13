package managers;

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

    /**
     * Constructeur de la classe RequestManager.
     *
     * @param configManager Le manager de configuration.
     * @param logManager    Le manager de logs.
     */
    public RequestManager(ConfigManager configManager, LogManager logManager) {
        this.configManager = configManager;
        this.logManager = logManager;
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
        try (FileWriter file = new FileWriter("resources/html/system_status.json")) {
            file.write(json);
        }
    }


    /**
     * Méthode pour gérer une requête.
     *
     * @param clientSocket    Le socket client.
     * @param responseManager Le manager de réponses.
     * @param fileManager     Le manager de fichiers.
     * @throws Exception Si une erreur se produit lors de la gestion de la requête.
     */
    public void handleRequest(Socket clientSocket, ResponseManager responseManager, FileManager fileManager) throws Exception {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream, true);


            // Lire la première ligne de la requête
            String requestLine = in.readLine();

            // Extract the URL from the request
            String filePath = getFilePath(requestLine);

            // Générer et écrire le statut du système dans un fichier JSON
            if (filePath.endsWith("serverStatus.html")) {
                writeStatusToFile();
            }

            // Lire le contenu du fichier
            byte[] content = fileManager.readFile(filePath);

            // Déterminer le type de contenu en fonction de l'extension du fichier
            String contentType = getContentType(filePath);

            // Envoi de la réponse au client
            responseManager.sendResponse(printWriter, outputStream, "HTTP/1.1 200 OK", contentType, content);


            logManager.print(filePath + " a été demandé par le client " + clientSocket.getInetAddress(), LogManager.INFO);
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
        String filePath = filePathBase + url;
        File file = new File(filePath);

        // Si le fichier n'existe pas, renvoyer une erreur 404
        if (!file.exists()) {
            filePath = configManager.getConfigValue("/myweb/error") + "/404.html";
        }
        return filePath;
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