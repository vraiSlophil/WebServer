package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlCodeExecutor {
    public static String processDynamicCode(String content) throws IOException, InterruptedException {
        Pattern pattern = Pattern.compile("<code interpreteur=\"([^\"]+)\">(.*?)</code>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String interpreter = matcher.group(1);
            String code = matcher.group(2).trim();

            String output = executeCode(interpreter, code);
            matcher.appendReplacement(result, "<pre>" + Matcher.quoteReplacement(output) + "</pre>");
        }
        matcher.appendTail(result);
        return result.toString();
    }

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
