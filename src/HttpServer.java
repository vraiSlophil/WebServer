import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;

import static java.util.logging.Level.*;

/**
 * Classe HttpServer.
 */
public class HttpServer {

    private static final String INI_FILE_PATH = "ressources/myweb.ini";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * La méthode principale.
     *
     * @param args les arguments de la ligne de commande
     */
    public static void main(String[] args) throws UnknownHostException, Exception {
        print("Démarrage du serveur", INFO);
        /// Port du serveur, par défaut 80
        int serverPort = 80;

        // Vérifier si un port est spécifié dans la ligne de commande
        if (args.length == 1) {
            try {
                serverPort = Integer.parseInt(args[0]);
            } catch (Exception _) {
                print("Numéro de port invalide, lecture du fichier de configuration...", WARNING);
            }
        } else {
            print("Numéro de port non précisé, lecture du fichier de configuration...", WARNING);
            serverPort = Integer.parseInt(Objects.requireNonNull(getConfigValue("/myweb/port")));
        }

        // Démarrage du serveur
        print("Démarrage du serveur sur : http://" + InetAddress.getLocalHost().getHostAddress() + ":" + serverPort + "/", INFO);
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> {
                        try {
                            handleClient(clientSocket);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                } catch (Exception e) {
                    print("Erreur lors de la gestion de la connexion client : " + e.getMessage(), SEVERE);
                }
            }
        } catch (Exception e) {
            print("Erreur du serveur : " + e.getMessage(), SEVERE);
        }
    }

    /**
     * Gère la connexion client.
     *
     * @param clientSocket le socket client
     */
    private static void handleClient(Socket clientSocket) throws Exception {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream, true);

            // Lire la première ligne de la requête
            String requestLine = in.readLine();

            // Extraire l'URL de la requête
            String filePath = getFilePath(requestLine);

            // Lire le contenu du fichier
            byte[] content = Files.readAllBytes(Paths.get(filePath));

            // Déterminer le type de contenu en fonction de l'extension du fichier
            String contentType = getContentType(filePath);

            // Envoi de la réponse au client
            printWriter.println("HTTP/1.1 200 OK");
            printWriter.println("Content-Type: " + contentType);
            printWriter.println("Connection: close");
            printWriter.println();
            outputStream.write(content);
            outputStream.flush();

            print(filePath + " a été demandé par le client " + clientSocket.getInetAddress(), INFO);
        } catch (Exception e) {
            print("Erreur lors de la gestion de la connexion client : " + e.getMessage(), SEVERE);
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                print("Erreur lors de la fermeture du socket client : " + e.getMessage(), SEVERE);
            }
        }
    }

    private static String getFilePath(String requestLine) throws Exception {
        String url = requestLine.split(" ")[1];

        if (url.equals("/")) {
            url = "/index.html";
        }

        // Lire le fichier correspondant à l'URL
        String filePathBase = getConfigValue("/myweb/root");
        String filePath = filePathBase + url;
        File file = new File(filePath);

        // Si le fichier n'existe pas, renvoyer une erreur 404
        if (!file.exists()) {
            filePath = getConfigValue("/myweb/error") + "/404.html";
        }
        return filePath;
    }

    /**
     * Renvoie le type de contenu en fonction de l'extension du fichier.
     *
     * @param filePath le chemin d'accès au fichier
     * @return le type de contenu
     */
    private static String getContentType(String filePath) {
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

    /**
     * Récupère une valeur du fichier de configuration.
     *
     * @param xPathExpression l'expression XPath de l'élément à récupérer
     * @return la valeur de l'élément
     */
    public static String getConfigValue(String xPathExpression) throws Exception {
        File iniFile = new File(INI_FILE_PATH);
        String configFilePath = "";
        try {
            if (iniFile.exists() && !iniFile.isDirectory()) {
                try {
                    Properties properties = new Properties();
                    properties.load(new FileInputStream(INI_FILE_PATH));
                    configFilePath = properties.getProperty("cfgfile");
                } catch (Exception e) {
                    print("Erreur lors de la lecture du fichier de configuration : " + e.getMessage(), SEVERE);
                }
            } else {
                return null;
            }

            File configFile = new File(configFilePath);
            if (!configFile.exists() || configFile.isDirectory()) {
                print("Le fichier de configuration n'existe pas ou n'est pas un fichier valide : " + configFilePath, SEVERE);
                return null;
            }

            String configContent = new String(Files.readAllBytes(Paths.get(configFilePath)));
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(configContent)));
            XPath xpath = XPathFactory.newInstance().newXPath();
            return (String) xpath.evaluate(xPathExpression, doc, XPathConstants.STRING);
        } catch (Exception e) {
            print("Erreur lors de la récupération de la valeur de configuration : " + e.getMessage(), SEVERE);
            return null;
        }
    }

    public static void print(String string, Level level) throws Exception {
        String timestamp = DATE_FORMAT.format(new Date());
        String message = timestamp + " - " + string;

        message = switch (level.getName()) {
            case "WARNING" -> "WARN : " + message;
            case "SEVERE" -> {
                message = "ERROR : " + message;
                throw new Exception(message);
            }
            default -> "INFO : " + message;
        };

        System.out.println(message);

        String logFilePath = getConfigValue("/myweb/log");
        if (logFilePath == null) {
            System.out.println("Erreur lors de la récupération du chemin du fichier de log");
            return;
        }

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)))) {
            out.println(message);
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture dans le fichier de log : " + e.getMessage());
        }
    }
}