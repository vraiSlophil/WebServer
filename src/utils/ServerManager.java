package utils;

import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Enumeration;

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
     */
    public void startServer(String[] args, RequestManager requestManager, ResponseManager responseManager) throws Exception {
        int serverPort = 80;

        // Vérifier si un port est spécifié dans la ligne de commande
        try {
            serverPort = Integer.parseInt(args[0]);
        } catch (Exception e) {
            logManager.print("Numéro de port invalide, lecture du fichier de configuration...", LogManager.WARN);
            try {
                serverPort = Integer.parseInt(configManager.getConfigValue("/myweb/port"));
            } catch (Exception ex) {
                logManager.print("Fichier de configuration invalide, utilisation du port par défaut...", LogManager.WARN);
            }
        }

        if (serverPort < 0 || serverPort > 65535) {
            logManager.print("Numéro de port invalide, utilisation du port par défaut...", LogManager.WARN);
            serverPort = 80;
        }

        // Récupérer l'adresse IP non locale de la machine hôte
        String serverIp = null;
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            // filtre les interfaces de bouclage et les interfaces désactivées
            if (iface.isLoopback() || !iface.isUp())
                continue;

            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while(addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                // Vérifie si l'adresse est une instance de Inet4Address (c'est-à-dire une adresse IPv4)
                if (addr instanceof java.net.Inet4Address) {
                    serverIp = addr.getHostAddress();
                    break;
                }
            }
            if (serverIp != null) {
                break;
            }
        }

        // Démarrage du serveur
        logManager.print("Démarrage du serveur sur : http://" + serverIp + ":" + serverPort + "/", LogManager.INFO);
        try (ServerSocket serverSocket = new ServerSocket(serverPort, 0, InetAddress.getByName(serverIp))) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> {
                        try {
                            handleClient(clientSocket, requestManager, responseManager);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                } catch (Exception e) {
                    logManager.print("Erreur lors de la connexion d'un client : " + e.getMessage(), LogManager.ERROR);
                }
            }
        } catch (Exception e) {
            logManager.print("Erreur du serveur : " + e.getMessage(), LogManager.ERROR);
        }
    }

    /**
     * Méthode pour gérer un client.
     * Cette méthode est appelée chaque fois qu'un client se connecte au serveur.
     * Elle est responsable de la gestion de la connexion client.
     * @param clientSocket Le socket client.
     * @throws Exception Si une erreur se produit lors de la gestion du client.
     */
    private void handleClient(Socket clientSocket, RequestManager requestManager, ResponseManager responseManager) throws Exception {
        try {
            requestManager.handleRequest(clientSocket, responseManager);
        } catch (Exception e) {
            logManager.print("Erreur lors de la gestion de la requête : " + e.getMessage(), LogManager.ERROR);
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                logManager.print("Erreur lors de la fermeture du socket client : " + e.getMessage(), LogManager.ERROR);
            }
        }
    }
}