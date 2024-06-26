package utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Classe LogManager.
 * Cette classe est responsable de la gestion des logs.
 */
public class LogManager {

    public static final String INFO = "INFO";
    public static final String WARN = "WARN";
    public static final String ERROR = "ERROR";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private ConfigManager configManager;

    /**
     * Constructeur de la classe LogManager.
     */
    public LogManager() {
    }

    /**
     * Méthode pour imprimer un message dans le fichier de log.
     *
     * @param message Le message à imprimer.
     * @param level   Le niveau du message (INFO, WARN, ERROR).
     * @throws Exception Si une erreur se produit lors de l'écriture dans le fichier de log.
     */
    public void print(String message, String level) throws Exception {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = timestamp + " [" + level + "] " + message;

        System.out.println(logMessage);

        String logFilePath = configManager.getConfigValue("/myweb/accesslog");
        if (logFilePath == null) {
            System.out.println("Erreur lors de la récupération du chemin du fichier de log.");
            return;
        }

        // Créer le fichier de log si nécessaire
        File logFile = new File(logFilePath);
        if (!logFile.exists()) {
            File parentDir = logFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            logFile.createNewFile();
        }

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)))) {
            out.println(logMessage);
        } catch (IOException e) {
            throw new Exception("Erreur lors de l'écriture dans le fichier de log : " + e.getMessage());
        }

        if (level.equals(ERROR)) {
            logFilePath = configManager.getConfigValue("/myweb/errorlog");
            if (logFilePath == null) {
                System.out.println("Erreur lors de la récupération du chemin du fichier de log d'erreur.");
                return;
            }

            // Créer le fichier de log d'erreur si nécessaire
            logFile = new File(logFilePath);
            if (!logFile.exists()) {
                File parentDir = logFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
                logFile.createNewFile();
            }

            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)))) {
                out.println(logMessage);
                try {
                    throw new Exception(message);
                } catch (Exception e) {
                    e.printStackTrace(out);
                    e.printStackTrace();
                }
            } catch (IOException e) {
                throw new Exception("Erreur lors de l'écriture dans le fichier de log d'erreur : " + e.getMessage());
            }
        }
    }

    /**
     * Setter pour le ConfigManager.
     *
     * @param configManager Le ConfigManager.
     */
    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }
}