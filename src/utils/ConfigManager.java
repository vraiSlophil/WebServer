package utils;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Classe ConfigManager.
 * Cette classe est responsable de la gestion de la configuration.
 */
public class ConfigManager {

    private static final String INI_FILE_PATH = "/myweb.ini";
    private final FileManager fileManager;
    private LogManager logManager;
    private Document configDoc;

    /**
     * Constructeur de la classe ConfigManager.
     *
     * @param fileManager Le manager de fichiers.
     */
    public ConfigManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    /**
     * Charge le fichier de configuration.
     * Cette méthode vérifie d'abord si le fichier INI existe et n'est pas un répertoire.
     * Si c'est le cas, elle lit le contenu du fichier INI pour obtenir le chemin du fichier de configuration.
     * Elle vérifie ensuite si le fichier de configuration existe et n'est pas un répertoire.
     * Si c'est le cas, elle lit le contenu du fichier de configuration, le parse en XML et stocke le Document XML pour une utilisation ultérieure.
     * Si une erreur se produit à n'importe quelle étape, un message d'erreur est imprimé et la méthode retourne.
     *
     * @throws Exception Si une erreur se produit lors de la lecture ou du parsing des fichiers.
     */
    public void loadConfigFile() throws Exception {
        logManager.print("Chargement du fichier de configuration...", LogManager.INFO);
//        System.out.println("Chargement du fichier de configuration...");
        InputStream in = getClass().getResourceAsStream(INI_FILE_PATH);
        Properties properties = new Properties();
        properties.load(in);
        String configFilePath = properties.getProperty("cfgfile");
        in.close();

        // Vérifier si le dossier parent existe
        File configFile = new File(configFilePath);
        File parentDir = configFile.getParentFile();
        if (!parentDir.exists()) {
            // Si le dossier parent n'existe pas, le créer
            if (!parentDir.mkdirs()) {
                throw new Exception("Impossible de créer le dossier parent : " + parentDir.getAbsolutePath());
            }
        }

        // Vérifier si le fichier de configuration existe
        if (!configFile.exists()) {
            // Si le fichier de configuration n'existe pas, le créer en copiant le fichier de ressource
            InputStream resourceConfigStream = getClass().getResourceAsStream("/config/myweb.conf");
            fileManager.copyStreamIfNotExists(resourceConfigStream, configFilePath);
        }

        byte[] configContent = fileManager.readFile(configFilePath);
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        configDoc = builder.parse(new InputSource(new StringReader(new String(configContent))));
        logManager.print("Fichier de configuration chargé avec succès.", LogManager.INFO);
    }

    /**
     * Récupère une valeur du fichier de configuration.
     *
     * @param xPathExpression l'expression XPath de l'élément à récupérer
     * @return la valeur de l'élément
     */
    public String getConfigValue(String xPathExpression) throws Exception {
        if (configDoc == null) {
            System.out.println("Erreur lors de la récupération de la valeur de configuration : le document de configuration n'est pas chargé");
            return null;
        }

        XPath xpath = XPathFactory.newInstance().newXPath();
        return (String) xpath.evaluate(xPathExpression, configDoc, XPathConstants.STRING);
    }

    /**
     * Récupère une valeur du fichier de configuration.
     *
     * @param xPathExpression l'expression XPath de l'élément à récupérer
     * @return la valeur de l'élément
     */
    public List<String> getIPList(String xPathExpression) throws Exception {
        if (configDoc == null) {
            System.out.println("Erreur lors de la récupération de la liste d'IP : le document de configuration n'est pas chargé");
            return null;
        }

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList) xpath.evaluate(xPathExpression, configDoc, XPathConstants.NODESET);
        List<String> ipList = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            ipList.add(nodes.item(i).getTextContent());
        }
        return ipList;
    }

    /**
     * Définit le manager de logs.
     *
     * @param logManager Le manager de logs.
     */
    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }
}