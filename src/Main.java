import utils.*;

public class Main {

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