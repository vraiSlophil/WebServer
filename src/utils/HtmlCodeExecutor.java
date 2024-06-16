package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe HtmlCodeExecutor.
 * Cette classe est responsable de l'exécution du code HTML dynamique.
 */
public class HtmlCodeExecutor {

    /**
     * Méthode pour traiter le code dynamique dans le contenu HTML.
     * @param content Le contenu HTML à traiter.
     * @return Le contenu HTML avec le code dynamique exécuté.
     * @throws IOException Si une erreur d'entrée/sortie se produit.
     * @throws InterruptedException Si une erreur d'interruption se produit.
     */
    public static String processDynamicCode(String content) throws IOException, InterruptedException {
        Pattern pattern = Pattern.compile("<code interpreteur=\"([^\"]+)\">(.*?)</code>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String interpreter = matcher.group(1);
            String code = matcher.group(2).trim();

            String output = null;
            try {
                output = executeCode(interpreter, code);
                matcher.appendReplacement(result, "<pre>" + Matcher.quoteReplacement(output) + "</pre>");
            } catch (Exception e) {
                matcher.appendReplacement(result, "<pre>Erreur lors de l'exécution du code pour le langage " + interpreter + "</pre>");
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Méthode pour exécuter le code avec l'interpréteur spécifié.
     * @param interpreter L'interpréteur à utiliser.
     * @param code Le code à exécuter.
     * @return La sortie de l'exécution du code.
     * @throws IOException Si une erreur d'entrée/sortie se produit.
     * @throws InterruptedException Si une erreur d'interruption se produit.
     */
    private static String executeCode(String interpreter, String code) throws InterruptedException, IOException {
        // Adaptation pour Windows
        if (System.getProperty("os.name").toLowerCase().contains("win") && interpreter.equals("/usr/bin/python")) {
            interpreter = "python";
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        if (interpreter.contains("powershell")) {
            processBuilder.command("powershell.exe", "-Command", code);
        } else {
            processBuilder.command(interpreter, "-c", code);
        }

        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        process.waitFor();
        return output.toString();
    }
}
