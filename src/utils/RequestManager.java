package utils;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.net.Socket;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe RequestManager.
 * Cette classe est responsable de la gestion des requêtes.
 */
public class RequestManager {

    private static final String HTTP_200_OK = "HTTP/1.1 200 OK";
    private static final String HTTP_403_FORBIDDEN = "HTTP/1.1 403 Forbidden";
    private static final String HTTP_404_NOT_FOUND = "HTTP/1.1 404 Not Found";
    private static final String HTTP_500_INTERNAL_SERVER_ERROR = "HTTP/1.1 500 Internal Server Error";

    private final ConfigManager configManager;
    private final LogManager logManager;
    private final FileManager fileManager;

    /**
     * Constructeur de la classe RequestManager.
     *
     * @param configManager Le manager de configuration.
     * @param logManager    Le manager de logs.
     */
    public RequestManager(ConfigManager configManager, LogManager logManager, FileManager fileManager) {
        this.configManager = configManager;
        this.logManager = logManager;
        this.fileManager = fileManager;
    }

    /**
     * Récupère les informations du système.
     *
     * @return Les informations du système.
     * @throws Exception Si une erreur se produit lors de la récupération des informations.
     */
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

    /**
     * Ecrit le statut du système dans un fichier JSON.
     *
     * @throws Exception Si une erreur se produit lors de l'écriture du statut du système.
     */
    private void writeStatusToFile() throws Exception {
        Map<String, String> status = getSystemStatus();
        StringBuilder json = new StringBuilder("{\n");
        for (Map.Entry<String, String> entry : status.entrySet()) {
            json.append("  \"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\",\n");
        }
        json = new StringBuilder(json.substring(0, json.length() - 2)); // Enlève la dérnière virgule
        json.append("\n}");

        // Ecris le json et le met dans le fichier system_status.json
        try (FileWriter file = new FileWriter(configManager.getConfigValue("/myweb/root") + "/system_status.json")) {
            file.write(json.toString());
        }
    }

    /**
     * Méthode pour gérer une requête.
     *
     * @param clientSocket    Le socket client.
     * @param responseManager Le manager de réponses.
     * @throws Exception Si une erreur se produit lors de la gestion de la requête.
     */
    public void handleRequest(Socket clientSocket, ResponseManager responseManager) throws Exception {
        String clientIP = clientSocket.getInetAddress().getHostAddress();
        List<String> acceptIPs = configManager.getIPList("/myweb/accept");
        List<String> rejectIPs = configManager.getIPList("/myweb/reject");

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String requestLine = in.readLine();
        if (requestLine == null) {
            return;
        }
        String filePath = getFilePath(requestLine);
        String askedFile = filePath;

        // Vérifier si l'adresse IP du client est autorisée ou non
        if (!isIPAuthorized(clientIP, acceptIPs, rejectIPs)) {
            // Refuser la requête et définir le chemin du fichier à la page d'erreur 403
            filePath = configManager.getConfigValue("/myweb/error") + "/403.html";
        }

        byte[] content;
        String status;
        try {
            if (!fileManager.fileExists(filePath)) {
                filePath = configManager.getConfigValue("/myweb/error") + "/404.html";
                status = HTTP_404_NOT_FOUND;
            } else {
                status = filePath.endsWith("/403.html") ? HTTP_403_FORBIDDEN : HTTP_200_OK;
                if (filePath.endsWith("serverStatus.html")) {
                    writeStatusToFile();
                }
            }
        } catch (Exception e) {
            filePath = configManager.getConfigValue("/myweb/error") + "/500.html";
            status = HTTP_500_INTERNAL_SERVER_ERROR;
        }

        content = fileManager.readFile(filePath);
        String contentType = getContentType(filePath);
        responseManager.sendResponse(new PrintWriter(clientSocket.getOutputStream(), true), clientSocket.getOutputStream(), status, contentType, content);
        logManager.print(askedFile + " a été demandé par le client " + clientSocket.getInetAddress() + " " + status, (!status.equals(HTTP_200_OK) ? ((status.equals(HTTP_403_FORBIDDEN) || status.equals(HTTP_404_NOT_FOUND)) ? LogManager.WARN : LogManager.ERROR) : LogManager.INFO));

    }


    /**
     * Vérifie si l'adresse IP du client est autorisée.
     *
     * @param clientIP  l'adresse IP du client au format xxx.xxx.xxx.xxx
     * @param acceptIPs la liste des adresses IP autorisées au format xxx.xxx.xxx.xxx/xxx où /xxx est le masque de sous-réseau
     * @param rejectIPs la liste des adresses IP rejetées au format xxx.xxx.xxx.xxx/xxx où /xxx est le masque de sous-réseau
     * @return true si l'adresse IP du client est autorisée, false sinon
     */
    private boolean isIPAuthorized(String clientIP, List<String> acceptIPs, List<String> rejectIPs) {
        if (!rejectIPs.isEmpty() && isIPInSubnet(clientIP, rejectIPs)) {
            return false;
        }

        return acceptIPs.isEmpty() || isIPInSubnet(clientIP, acceptIPs);
    }

    /**
     * Vérifie si l'adresse IP du client est dans un sous-réseau donné.
     *
     * @param clientIP l'adresse IP du client au format xxx.xxx.xxx.xxx
     * @param ipList  la liste des adresses IP au format xxx.xxx.xxx.xxx/xxx où /xxx est le masque de sous-réseau
     * @return true si l'adresse IP du client est dans un sous-réseau donné, false sinon
     */
    private boolean isIPInSubnet(String clientIP, List<String> ipList) {
    int[] clientIPArray = new int[4];
    String[] clientIPSplit = clientIP.split("\\.");
    for (int i = 0; i < 4; i++) {
        clientIPArray[i] = Integer.parseInt(clientIPSplit[i]);
    }

    for (String ip : ipList) {
        String[] ipSplit = ip.split("/");
        int[] ipArray = new int[4];
        String[] ipSplitArray = ipSplit[0].split("\\.");
        for (int i = 0; i < 4; i++) {
            ipArray[i] = Integer.parseInt(ipSplitArray[i]);
        }
        int masqueReseau = Integer.parseInt(ipSplit[1]);

        int masqueReseauOctet = masqueReseau / 8;
        int masqueReseauBit = masqueReseau % 8;

        for (int i = 0; i < masqueReseauOctet; i++) {
            if (clientIPArray[i] != ipArray[i]) {
                return false;
            }
        }

        if (masqueReseauBit != 0) {
            int mask = 0xFF << (8 - masqueReseauBit);
            if ((clientIPArray[masqueReseauOctet] & mask) != (ipArray[masqueReseauOctet] & mask)) {
                return false;
            }
        }

        return true;
    }
    return false;
}

    /**
     * Récupère le chemin d'accès au fichier demandé.
     *
     * @param requestLine la première ligne de la requête
     * @return le chemin d'accès au fichier
     * @throws Exception si une erreur se produit lors de la récupération du chemin d'accès au fichier
     */
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