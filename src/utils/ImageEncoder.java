package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe ImageEncoder.
 * Cette classe est responsable de l'encodage des images en base64.
 */
public class ImageEncoder {
    public static final String IMAGE_DIRECTORY = "resources/html/";

    /**
     * Méthode pour encoder un fichier image en base64.
     * @param filePath Le chemin du fichier image.
     * @return L'image encodée en base64.
     * @throws IOException Si une erreur se produit lors de la lecture du fichier.
     */
    public static String encodeImageFile(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }

    /**
     * Méthode pour générer une balise img HTML à partir d'une image encodée en base64.
     * @param base64Image L'image encodée en base64.
     * @param mimeType Le type MIME de l'image.
     * @return La balise img HTML.
     */
    public static String generateImgTag(String base64Image, String mimeType) {
        return "<img src=\"data:" + mimeType + ";base64," + base64Image + "\" />";
    }

    /**
     * Méthode pour traiter les balises img dans le contenu HTML.
     * @param content Le contenu HTML.
     * @param fileManager Le gestionnaire de fichiers.
     * @return Le contenu HTML avec les balises img traitées.
     * @throws Exception Si une erreur se produit lors du traitement des balises img.
     */
    public static String processImageTag(String content, FileManager fileManager) throws Exception {
        Pattern pattern = Pattern.compile("<img src=\"(.*?)\"(.*?)>");
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String imgPath = IMAGE_DIRECTORY+ matcher.group(1);
            if (fileManager.fileExists(imgPath)) {
                String imgBase64 = encodeImageFile(imgPath);
                String mimeType = getMimeType(imgPath);
                String imgTag = generateImgTag(imgBase64, mimeType);
                matcher.appendReplacement(result, Matcher.quoteReplacement(imgTag));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement("<img src=\"" + imgPath + "\">"));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Méthode pour obtenir le type MIME d'un fichier.
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
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "png" -> "image/png";
            default -> "application/octet-stream";
        };
    }
}
