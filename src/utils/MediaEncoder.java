package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe MediaEncoder.
 * Cette classe est responsable de l'encodage des images, des audios et des vidéos en base64.
 */
public class MediaEncoder {

    /**
     * Méthode pour encoder un fichier en base64.
     *
     * @param filePath Le chemin du fichier.
     * @return Le fichier encodé en base64.
     * @throws IOException Si une erreur se produit lors de la lecture du fichier.
     */
    public static String encodeFile(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }

    /**
     * Méthode pour générer une balise img HTML à partir d'une image encodée en base64.
     *
     * @param base64Content Le contenu encodé en base64.
     * @param mimeType      Le type MIME du fichier.
     * @param attributes    Les attributs supplémentaires à inclure dans la balise.
     * @return La balise img HTML.
     */
    public static String generateImgTag(String base64Content, String mimeType, String attributes) {
        return "<img src=\"data:" + mimeType + ";base64," + base64Content + "\" " + attributes + " />";
    }

    /**
     * Méthode pour générer une balise audio HTML à partir d'un fichier audio encodé en base64.
     *
     * @param base64Content Le contenu encodé en base64.
     * @param mimeType      Le type MIME du fichier.
     * @param attributes    Les attributs supplémentaires à inclure dans la balise.
     * @return La balise audio HTML.
     */
    public static String generateAudioTag(String base64Content, String mimeType, String attributes) {
        return "<audio " + attributes + "><source src=\"data:" + mimeType + ";base64," + base64Content + "\" type=\"" + mimeType + "\" />Votre navigateur ne supporte pas la balise audio.</audio>";
    }

    /**
     * Méthode pour générer une balise vidéo HTML à partir d'un fichier vidéo encodé en base64.
     *
     * @param base64Content Le contenu encodé en base64.
     * @param mimeType      Le type MIME du fichier.
     * @param attributes    Les attributs supplémentaires à inclure dans la balise.
     * @return La balise vidéo HTML.
     */
    public static String generateVideoTag(String base64Content, String mimeType, String attributes) {
        return "<video controls " + attributes + "><source src=\"data:" + mimeType + ";base64," + base64Content + "\" type=\"" + mimeType + "\" />Votre navigateur ne supporte pas la balise vidéo.</video>";
    }

    /**
     * Méthode pour traiter les balises img, audio et vidéo dans le contenu HTML.
     *
     * @param content       Le contenu HTML.
     * @param fileManager   Le gestionnaire de fichiers.
     * @param configManager Le gestionnaire de configuration.
     * @return Le contenu HTML avec les balises traitées.
     * @throws Exception Si une erreur se produit lors du traitement des balises.
     */
    public static String processMediaTags(String content, FileManager fileManager, ConfigManager configManager) throws Exception {
        //content = processImageTag(content, fileManager, configManager);
        content = processAudioTag(content, fileManager, configManager);
        content = processVideoTag(content, fileManager, configManager);
        return content;
    }

    /**
     * Méthode pour traiter les balises img dans le contenu HTML.
     * @param content Le contenu HTML.
     * @param fileManager Le gestionnaire de fichiers.
     * @param configManager Le gestionnaire de configuration.
     * @return Le contenu HTML avec les balises img traitées.
     * @throws Exception Si une erreur se produit lors du traitement des balises.
     */
    private static String processImageTag(String content, FileManager fileManager, ConfigManager configManager) throws Exception {
        Pattern pattern = Pattern.compile("<img\\s+([^>]*?)src=\"(.*?)\"([^>]*)>");
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String attributes = matcher.group(1) + matcher.group(3);
            String filePath = configManager.getConfigValue("/myweb/root") + "/" + matcher.group(2);
            if (fileManager.fileExists(filePath)) {
                String base64Content = encodeFile(filePath);
                String mimeType = getMimeType(filePath);
                String imgTag = generateImgTag(base64Content, mimeType, attributes);
                matcher.appendReplacement(result, Matcher.quoteReplacement(imgTag));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement("<img src=\"" + filePath + "\"" + attributes + ">"));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Méthode pour traiter les balises audio dans le contenu HTML.
     * @param content Le contenu HTML.
     * @param fileManager Le gestionnaire de fichiers.
     * @param configManager Le gestionnaire de configuration.
     * @return Le contenu HTML avec les balises audio traitées.
     * @throws Exception Si une erreur se produit lors du traitement des balises.
     */
    private static String processAudioTag(String content, FileManager fileManager, ConfigManager configManager) throws Exception {
        final String regex = "<audio([^>]*)>\\s*<source[\\s]+src=\"(.*?)\"[\\s\\S]*<\\/audio>";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String attributes = matcher.group(1);
            String filePath = configManager.getConfigValue("/myweb/root") + "/" + matcher.group(2);


            if (fileManager.fileExists(filePath)) {
                String base64Content = encodeFile(filePath);
                String mimeType = getMimeType(filePath);
                String audioTag = generateAudioTag(base64Content, mimeType, attributes);
                matcher.appendReplacement(result, Matcher.quoteReplacement(audioTag));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement("<audio" + attributes + "><source src=\"" + filePath + "\">Votre navigateur ne supporte pas la balise audio.</source></audio>"));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Méthode pour traiter les balises vidéo dans le contenu HTML.
     * @param content Le contenu HTML.
     * @param fileManager Le gestionnaire de fichiers.
     * @param configManager Le gestionnaire de configuration.
     * @return Le contenu HTML avec les balises vidéo traitées.
     * @throws Exception Si une erreur se produit lors du traitement des balises.
     */
    private static String processVideoTag(String content, FileManager fileManager, ConfigManager configManager) throws Exception {
        final String regex = "<video([^>]*)>\\s*<source[\\s]+src=\"(.*?)\"[\\s\\S]*<\\/video>";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String attributes = matcher.group(1);
            String filePath = configManager.getConfigValue("/myweb/root") + "/" + matcher.group(2);


            if (fileManager.fileExists(filePath)) {
                String base64Content = encodeFile(filePath);
                String mimeType = getMimeType(filePath);
                String audioTag = generateVideoTag(base64Content, mimeType, attributes);
                matcher.appendReplacement(result, Matcher.quoteReplacement(audioTag));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement("<audio" + attributes + "><source src=\"" + filePath + "\">Votre navigateur ne supporte pas la balise audio.</source></audio>"));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Méthode pour obtenir le type MIME d'un fichier.
     *
     * @param filePath Le chemin du fichier.
     * @return Le type MIME du fichier.
     */
    private static String getMimeType(String filePath) {
        String extension = "";

        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            extension = filePath.substring(i + 1);
        }

        return switch (extension) {
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "bmp" -> "image/bmp";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "png" -> "image/png";
            default -> "application/octet-stream";
        };
    }
}
