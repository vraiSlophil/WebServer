package utils;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;

/**
 * Classe ServerManager.
 * Cette classe est responsable de la gestion du serveur.
 */
public class ServerManager {

    private final ConfigManager configManager;
    private final LogManager logManager;

    /**
     * Constructeur de la classe ServerManager.
     * @param configManager Le manager de configuration.
     * @param logManager Le manager de logs.
     */
    public ServerManager(ConfigManager configManager, LogManager logManager) {
        this.configManager = configManager;
        this.logManager = logManager;
    }

    /**
     * Méthode pour démarrer le serveur.
     * Cette méthode initialise le serveur et attend les connexions des clients.
     * @param args Les arguments de la ligne de commande.
     * @param requestManager Le manager de requêtes.
     * @param responseManager Le manager de réponses.
     * @param fileManager Le manager de fichiers.
     * @throws Exception Si une erreur se produit lors du démarrage du serveur.
     */
    public void startServer(String[] args, RequestManager requestManager, ResponseManager responseManager, FileManager fileManager) throws Exception {
        int serverPort = 80;

        // Vérifier si un port est spécifié dans la ligne de commande
        try {
            serverPort = Integer.parseInt(args[0]);
        } catch (Exception _) {
            logManager.print("Numéro de port invalide, lecture du fichier de configuration...", LogManager.WARNING);
            try {
                serverPort = Integer.parseInt(configManager.getConfigValue("/myweb/port"));
            } catch (Exception _) {
                logManager.print("Fichier de configuration invalide, utilisation du port par défaut...", LogManager.WARNING);
            }
        }

        if (serverPort < 0 || serverPort > 65535) {
            logManager.print("Numéro de port invalide, utilisation du port par défaut...", LogManager.WARNING);
            serverPort = 80;
        }

        // Démarrage du serveur
        logManager.print("Démarrage du serveur sur : http://" + InetAddress.getLocalHost().getHostAddress() + ":" + serverPort + "/", LogManager.INFO);
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> {
                        try {
                            handleClient(clientSocket, requestManager, responseManager, fileManager);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                } catch (Exception e) {
                    logManager.print("Erreur lors de la gestion de la connexion client : " + e.getMessage(), LogManager.SEVERE);
                }
            }
        } catch (Exception e) {
            logManager.print("Erreur du serveur : " + e.getMessage(), LogManager.SEVERE);
        }
    }

    /**
     * Méthode pour gérer un client.
     * Cette méthode est appelée chaque fois qu'un client se connecte au serveur.
     * Elle est responsable de la gestion de la connexion client.
     * @param clientSocket Le socket client.
     * @throws Exception Si une erreur se produit lors de la gestion du client.
     */
    private void handleClient(Socket clientSocket, RequestManager requestManager, ResponseManager responseManager, FileManager fileManager) throws Exception {
        try {
            requestManager.handleRequest(clientSocket, responseManager, fileManager);
        } catch (Exception e) {
            logManager.print("Erreur lors de la gestion de la connexion client : " + e.getMessage(), LogManager.SEVERE);
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                logManager.print("Erreur lors de la fermeture du socket client : " + e.getMessage(), LogManager.SEVERE);
            }
        }
    }
}