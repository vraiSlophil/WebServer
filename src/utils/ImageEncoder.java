package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageEncoder {
    public static final String IMAGE_DIRECTORY = "resources/html/";


    public static String encodeImageFile(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }

    public static String generateImgTag(String base64Image, String mimeType) {
        return "<img src=\"data:" + mimeType + ";base64," + base64Image + "\" />";
    }

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
