package managers;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Classe ConfigManager.
 * Cette classe est responsable de la gestion de la configuration.
 */
public class ConfigManager {

    private static final String INI_FILE_PATH = "resources/myweb.ini";
    private final LogManager logManager;

    /**
     * Constructeur de la classe ConfigManager.
     * @param logManager Le manager de logs.
     */
    public ConfigManager(LogManager logManager) {
        this.logManager = logManager;
    }

    /**
     * Récupère une valeur du fichier de configuration.
     *
     * @param xPathExpression l'expression XPath de l'élément à récupérer
     * @return la valeur de l'élément
     */
    public String getConfigValue(String xPathExpression) throws Exception {
        File iniFile = new File(INI_FILE_PATH);
        String configFilePath = "";
        try {
            if (iniFile.exists() && !iniFile.isDirectory()) {
                try {
                    Properties properties = new Properties();
                    properties.load(new FileInputStream(INI_FILE_PATH));
                    configFilePath = properties.getProperty("cfgfile");
                } catch (Exception e) {
                    logManager.print("Erreur lors de la lecture du fichier de configuration : " + e.getMessage(), LogManager.SEVERE);
                }
            } else {
                return null;
            }

            File configFile = new File(configFilePath);
            if (!configFile.exists() || configFile.isDirectory()) {
                logManager.print("Le fichier de configuration n'existe pas ou n'est pas un fichier valide : " + configFilePath, LogManager.SEVERE);
                return null;
            }

            String configContent = new String(Files.readAllBytes(Paths.get(configFilePath)));
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(configContent)));
            XPath xpath = XPathFactory.newInstance().newXPath();
            return (String) xpath.evaluate(xPathExpression, doc, XPathConstants.STRING);
        } catch (Exception e) {
            logManager.print("Erreur lors de la récupération de la valeur de configuration : " + e.getMessage(), LogManager.SEVERE);
            return null;
        }
    }
}