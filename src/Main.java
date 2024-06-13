import utils.*;

public class Main {

    public static void main(String[] args) throws Exception {
        // Initialisation des managers
        FileManager fileManager = new FileManager();
        ConfigManager configManager = new ConfigManager(fileManager);
        LogManager logManager = new LogManager();

        // Injection des dépendances
        configManager.setLogManager(logManager);
        logManager.setConfigManager(configManager);

        ServerManager serverManager = new ServerManager(configManager, logManager);
        RequestManager requestManager = new RequestManager(configManager, logManager, fileManager);
        ResponseManager responseManager = new ResponseManager();

        // Démarrage du serveur
        serverManager.startServer(args, requestManager, responseManager);
    }
}
