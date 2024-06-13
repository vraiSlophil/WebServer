package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
        this.configManager = configManager;
    }

    /**
     * Méthode pour imprimer un message dans le fichier de log.
     * @param message Le message à imprimer.
     * @param level Le niveau du message (INFO, WARNING, SEVERE).
     * @throws Exception Si une erreur se produit lors de l'écriture dans le fichier de log.
     */
    public void print(String message, String level) throws Exception {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = timestamp + " [" + level + "] " + message;

        System.out.println(logMessage);

        String logFilePath = (level.equals(ERROR)) ? configManager.getConfigValue("/myweb/errorlog") : configManager.getConfigValue("/myweb/log");
        if (logFilePath == null) {
            System.out.println("Erreur lors de la récupération du chemin du fichier de log" + (level.equals(ERROR) ? " d'erreur" : ""));
            return;
        }

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)))) {
            out.println(logMessage);
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture dans le fichier de log : " + e.getMessage());
        }
    }

    /**
     * Setter pour le ConfigManager.
     * @param configManager Le ConfigManager.
     */
    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }
}