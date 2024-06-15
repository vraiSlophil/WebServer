import utils.*;

/**
 * Classe Main.
 * Cette classe est responsable de l'exécution du serveur web.
 */
public class Main {

    /**
     * Méthode principale.
     * @param args Les arguments de la ligne de commande.
     * @throws Exception Si une erreur survient lors de l'exécution du serveur.
     */
    public static void main(String[] args) throws Exception {


        // Initialisation des managers
        FileManager fileManager = new FileManager();
        LogManager logManager = new LogManager();

        // Injection des dépendances
        ConfigManager configManager = new ConfigManager(fileManager);
        configManager.setLogManager(logManager);
        logManager.setConfigManager(configManager);
        fileManager.setLogManager(logManager);

        configManager.loadConfigFile();

        DeploymentAutomation deploymentAutomation = new DeploymentAutomation(fileManager, logManager);
        deploymentAutomation.setup();

        // Création et exécution des générateurs de fichiers
        ConfigFileGenerator configFileGenerator = new ConfigFileGenerator(fileManager);
        configFileGenerator.generateConfigFiles();

        FileGenerator fileGenerator = new FileGenerator(configManager, fileManager);
        fileGenerator.generateFiles();


        ServerManager serverManager = new ServerManager(configManager, logManager);
        RequestManager requestManager = new RequestManager(configManager, logManager, fileManager);
        ResponseManager responseManager = new ResponseManager();

        // Démarrage du serveur
        serverManager.startServer(args, requestManager, responseManager);
    }
}