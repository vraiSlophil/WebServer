import utils.*;

public class Main {

    private static ServerManager serverManager;
    private static RequestManager requestManager;
    private static ResponseManager responseManager;
    private static ConfigManager configManager;
    private static LogManager logManager;
    private static FileManager fileManager;

    public static void main(String[] args) throws Exception {
        // Initialisation des managers
        fileManager = new FileManager();
        configManager = new ConfigManager(logManager, fileManager);
        logManager = new LogManager(configManager);
        serverManager = new ServerManager(configManager, logManager);
        requestManager = new RequestManager(configManager, logManager, fileManager);
        responseManager = new ResponseManager();

        // Démarrage du serveur
        serverManager.startServer(args, requestManager, responseManager);
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static ResponseManager getResponseManager() {
        return responseManager;
    }

    public static RequestManager getRequestManager() {
        return requestManager;
    }

    public static ServerManager getServerManager() {
        return serverManager;
    }

    public static LogManager getLogManager() {
        return logManager;
    }

    public static FileManager getFileManager() {
        return fileManager;
    }


}
