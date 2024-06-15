package utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Classe DeploymentAutomation.
 * Cette classe est responsable de l'automatisation du déploiement du serveur web.
 */
public class DeploymentAutomation {

    private static final String WEB_SERVER_JAR_PATH = System.getProperty("user.dir") + "/WebServer.jar";
    private static final String SYSTEMD_SERVICE_PATH = "/etc/systemd/system/myweb.service";
    private static final String DEBIAN_PACKAGE_PATH = "myweb/usr/local/sbin/myweb";
    private static final String SYSTEMD_DESTINATION_PATH = "myweb/etc/systemd/system";

    private final FileManager fileManager;
    private final LogManager logManager;

    /**
     * Constructeur de la classe DeploymentAutomation.
     * @param fileManager Le gestionnaire de fichiers.
     * @param logManager Le gestionnaire de logs.
     */
    public DeploymentAutomation(FileManager fileManager, LogManager logManager) {
        this.fileManager = fileManager;
        this.logManager = logManager;
    }

    /**
     * Méthode pour automatiser le déploiement du serveur web.
     * @throws Exception Si une erreur survient lors de l'automatisation du déploiement.
     */
    public void setup() throws Exception {
        logManager.print("Setting up...", LogManager.INFO);
        String os = System.getProperty("os.name").toLowerCase();

        // Si le système d'exploitation n'est pas Linux, retourner immédiatement
        if (!os.contains("nix") && !os.contains("nux")) {
            logManager.print("Le système d'exploitation n'est pas compatible, setup ingoré.", LogManager.WARN);
            return;
        }
        createSystemdService();
        createPIDFile();
        createDebianPackage();
    }

    /**
     * Méthode pour créer le service systemd.
     * @throws Exception Si une erreur survient lors de la création du service systemd.
     */
    private void createSystemdService() throws Exception {
        logManager.print("Création du service systemd...", LogManager.INFO);
        // Vérifier si le service systemd existe déjà
        if (fileManager.fileExists(SYSTEMD_SERVICE_PATH)) {
            logManager.print("Le service systemd existe déjà.", LogManager.WARN);
            return;
        }
        // Charger le fichier en tant que ressource du classpath
        InputStream sourceStream = getClass().getResourceAsStream("/service/mywebserver.service");
        if (sourceStream == null) {
            throw new FileNotFoundException("Le fichier 'mywebserver.service' n'a pas été trouvé dans le classpath");
        }

        // Utiliser FileManager pour copier le fichier
        String targetFilePath = "/etc/systemd/system/myweb.service";
        fileManager.copyStreamIfNotExists(sourceStream, targetFilePath);

        executeCommand("sudo systemctl enable myweb.service");
    }

    /**
     * Méthode pour créer le package Debian.
     * @throws Exception Si une erreur survient lors de la création du package Debian.
     */
    private void createDebianPackage() throws Exception {
        logManager.print("Création du package Debian...", LogManager.INFO);

        // Si le paquet Debian existe déjà, retourner immédiatement
        if (fileManager.fileExists("myweb.deb")) {
            logManager.print("Le package debian existe déjà.", LogManager.WARN);
            return;
        }

        // Créer la structure de répertoires pour le paquet Debian
        createDirectory("myweb/DEBIAN");
        createDirectory(DEBIAN_PACKAGE_PATH);

        // Générer le fichier de contrôle pour le paqbian
        String controlContent = "Package: myweb\n" + "Version: 1.0.0\n" + "Section: web\n" + "Priority: optional\n" + "Architecture: all\n" + "Maintainer: Nathan OUDER <nathan.ouder@gmail.com>, Nicolas SANJUAN <nicolas.sanjuan6@etu.univ-lorraine.fr>\n" + "Description: Serveur Web en Java";
        executeCommand("echo \"" + controlContent + "\" | tee myweb/DEBIAN/control");

        // Vérifier si les fichiers source existent avant de les copier
        if (fileManager.fileExists(WEB_SERVER_JAR_PATH) && fileManager.fileExists(SYSTEMD_SERVICE_PATH)) {
            logManager.print("Les fichiers sources existent, copie des fichiers...", LogManager.INFO);
        } else {
            logManager.print("Les fichiers sources n'existent pas, échec de la création du package debian.", LogManager.WARN);
            return;
        }

        // Copier le fichier jar du serveur web et le fichier de service systemd dans les répertoires appropriés
        executeCommand("cp " + WEB_SERVER_JAR_PATH + " " + DEBIAN_PACKAGE_PATH);
        executeCommand("cp " + SYSTEMD_SERVICE_PATH + " " + SYSTEMD_DESTINATION_PATH);

        // Construire le paquet Debian
        executeCommand("dpkg-deb --build myweb");

        // Supprimer les répertoires après la création du paquet Debian
        executeCommand("rm -rf myweb");
    }

    /**
     * Méthode pour créer le fichier PID.
     * @throws Exception Si une erreur survient lors de la création du fichier PID.
     */
    private void createPIDFile() throws Exception {
        logManager.print("Création du fichier PID...", LogManager.INFO);
        executeCommand("echo $$ > /var/run/myweb.pid");
    }

    /**
     * Méthode pour exécuter une commande système.
     * @param command La commande à exécuter.
     * @return Le code de retour de la commande.
     * @throws Exception Si une erreur survient lors de l'exécution de la commande.
     */
    private int executeCommand(String command) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();

        ProcessBuilder processBuilder = new ProcessBuilder();

        if (os.contains("win")) {
            // Windows command execution
            processBuilder.command("cmd.exe", "/c", command);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            // Unix/Linux/Mac command execution
            processBuilder.command("bash", "-c", command);
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + os);
        }

        Process process = processBuilder.start();
        return process.waitFor();
    }

    /**
     * Méthode pour créer un répertoire.
     * @param directoryPath Le chemin du répertoire à créer.
     * @throws Exception Si une erreur survient lors de la création du répertoire.
     */
    private void createDirectory(String directoryPath) throws Exception {
        logManager.print("Création du dossier parent : " + directoryPath, LogManager.INFO);
        if (executeCommand("mkdir -p " + directoryPath) != 0) {
            throw new IOException("Erreur lors de la création du répertoire " + directoryPath);
        }
    }
}