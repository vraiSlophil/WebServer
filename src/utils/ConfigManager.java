package utils;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Properties;

/**
 * Classe ConfigManager.
 * Cette classe est responsable de la gestion de la configuration.
 */
public class ConfigManager {

    private static final String INI_FILE_PATH = "resources/myweb.ini";
    private final LogManager logManager;
    private final FileManager fileManager;
    private Document configDoc;

    /**
     * Constructeur de la classe ConfigManager.
     * @param logManager Le manager de logs.
     * @param fileManager Le manager de fichiers.
     */
    public ConfigManager(LogManager logManager, FileManager fileManager) throws Exception {
        this.logManager = logManager;
        this.fileManager = fileManager;
        loadConfigFile();
    }

    /**
     * Charge le fichier de configuration.
     * Cette méthode vérifie d'abord si le fichier INI existe et n'est pas un répertoire.
     * Si c'est le cas, elle lit le contenu du fichier INI pour obtenir le chemin du fichier de configuration.
     * Elle vérifie ensuite si le fichier de configuration existe et n'est pas un répertoire.
     * Si c'est le cas, elle lit le contenu du fichier de configuration, le parse en XML et stocke le Document XML pour une utilisation ultérieure.
     * Si une erreur se produit à n'importe quelle étape, un message d'erreur est imprimé et la méthode retourne.
     * @throws Exception Si une erreur se produit lors de la lecture ou du parsing des fichiers.
     */
    private void loadConfigFile() throws Exception {
        if (!fileManager.fileExists(INI_FILE_PATH)) {
            logManager.print("Le fichier de configuration n'existe pas ou n'est pas un fichier valide : " + INI_FILE_PATH, LogManager.SEVERE);
            return;
        }

        byte[] iniFileContent = fileManager.readFile(INI_FILE_PATH);
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(iniFileContent));
        String configFilePath = properties.getProperty("cfgfile");

        if (!fileManager.fileExists(configFilePath)) {
            logManager.print("Le fichier de configuration n'existe pas ou n'est pas un fichier valide : " + configFilePath, LogManager.SEVERE);
            return;
        }

        byte[] configContent = fileManager.readFile(configFilePath);
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        configDoc = builder.parse(new InputSource(new StringReader(new String(configContent))));
    }

    /**
     * Récupère une valeur du fichier de configuration.
     *
     * @param xPathExpression l'expression XPath de l'élément à récupérer
     * @return la valeur de l'élément
     */
    public String getConfigValue(String xPathExpression) throws Exception {
        if (configDoc == null) {
            logManager.print("Erreur lors de la récupération de la valeur de configuration : le document de configuration n'est pas chargé", LogManager.SEVERE);
            return null;
        }

        XPath xpath = XPathFactory.newInstance().newXPath();
        return (String) xpath.evaluate(xPathExpression, configDoc, XPathConstants.STRING);
    }
}